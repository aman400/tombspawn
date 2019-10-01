package com.ramukaka.data

import com.ramukaka.models.Reference
import com.ramukaka.models.github.RefType
import com.ramukaka.network.GradleExecutor
import com.ramukaka.utils.Constants
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.auth.Principal
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext


class Database(dbUrl: String, dbUsername: String, dbPass: String, private val isDebug: Boolean) {
    private val dispatcher: CoroutineContext
    private val connectionPool: HikariDataSource
    private val connection: Database

    private val config = HikariConfig()

    init {
        config.jdbcUrl = dbUrl
        config.username = dbUsername
        config.password = dbPass
        config.driverClassName = "com.mysql.cj.jdbc.Driver"
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        config.validate()
        dispatcher = Executors.newFixedThreadPool(20).asCoroutineDispatcher()

        connectionPool = HikariDataSource(config)
        connection = Database.connect(connectionPool)
        transaction(connection) {
            SchemaUtils.createMissingTablesAndColumns(Users, UserTypes, Apps, BuildTypes, Flavours, Subscriptions, Verbs, Apis, Refs)
        }
    }

    suspend fun findSubscriptions(refName: String, appId: String): List<ResultRow>? = withContext(dispatcher) {
        try {
            return@withContext transaction(connection) {
                if(isDebug) {
                    addLogger(StdOutSqlLogger)
                }

                val query = Subscriptions.leftJoin(Refs, {
                    this.refId
                }, {
                    this.id
                }).innerJoin(Users, {
                    Subscriptions.userId
                }, {
                    id
                }).innerJoin(Apps, {
                    Subscriptions.appId
                }, {
                    this.id
                }).slice(Users.slackId, Subscriptions.channel, Refs.name).select {
                    Refs.name eq refName
                }.withDistinct(true)

                return@transaction query.toList()

            }
        } catch (exception: Exception) {
            return@withContext null
        }
    }

    /**
     * Subscribe user to a branch
     */
    suspend fun subscribeUser(userId: String, appName: String, refName: String, channel: String): Boolean =
        withContext(dispatcher) {
            return@withContext transaction(connection) {
                if(isDebug) {
                    addLogger(StdOutSqlLogger)
                }
                val user = DBUser.find { Users.slackId eq userId }.first()
                val app = App.find { Apps.name eq appName }.first()
                val ref = Ref.find { Refs.name eq refName }.first()
                if (runBlocking(coroutineContext) { !isUserSubscribed(user, app, ref, channel) }) {
                    Subscription.new {
                        this.appId = app
                        this.refId = ref
                        this.channel = channel
                        this.userId = user
                    }
                    return@transaction true
                } else {
                    return@transaction false
                }
            }
        }

    /**
     * Checks if user is already subscribed to a branch or not.
     */
    suspend fun isUserSubscribed(user: DBUser, app: App, ref: Ref, channel: String): Boolean =
        withContext(dispatcher) {
            return@withContext transaction(connection) {
                if(isDebug) {
                    addLogger(StdOutSqlLogger)
                }
                return@transaction !Subscription.find {
                    (Subscriptions.userId eq user.id) and (Subscriptions.appId eq app.id) and (Subscriptions.refId eq ref.id) and (Subscriptions.channel eq channel)
                }.empty()
            }
        }

    suspend fun userExists(userId: String?): Boolean = withContext(dispatcher) {
        if (userId.isNullOrEmpty()) {
            return@withContext false
        }
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            val user = DBUser.find { Users.slackId eq userId }
            return@transaction !user.empty()
        }
    }

    suspend fun addApp(appName: String) = withContext(dispatcher) {
        transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            if (App.find { Apps.name eq appName }.limit(1).firstOrNull() == null) {
                App.new {
                    name = appName
                }
            }
        }

    }

    suspend fun addApps(appNames: List<com.ramukaka.models.config.App>) = withContext(dispatcher) {
        transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            appNames.forEach {
                if (App.find { Apps.name eq it.id }.limit(1).firstOrNull() == null) {
                    App.new {
                        this.name = it.id
                    }
                }
            }
        }
    }

    suspend fun addUser(
        userId: String,
        name: String? = null,
        email: String? = null,
        typeString: String = Constants.Database.USER_TYPE_USER,
        imId: String? = null
    ): DBUser? = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            val types = UserType.find { UserTypes.type eq typeString }.limit(1)
            val type = types.firstOrNull() ?: UserType.new {
                this.type = typeString
            }
            try {
                DBUser.find { Users.slackId eq userId }.firstOrNull() ?: return@transaction DBUser.new {
                    this.name = name
                    this.email = email
                    this.slackId = userId
                    this.imId = imId
                    this.userType = type
                }
            } catch (exception: ExposedSQLException) {
                exception.printStackTrace()
                null
            }
        }
    }

    suspend fun updateUser(
        userId: String,
        name: String? = null,
        email: String? = null
    ) = withContext(dispatcher) {
        transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            DBUser.find {
                Users.slackId eq userId
            }.forEach {
                it.name = name
                it.email = email
            }
            commit()
        }
    }

    suspend fun getUser(userType: String): DBUser? = withContext(dispatcher) {
        try {
            return@withContext transaction(connection) {
                if(isDebug) {
                    addLogger(StdOutSqlLogger)
                }
                val query = Users.innerJoin(UserTypes, {
                    Users.userType
                }, {
                    id
                }).slice(Users.columns).select {
                    UserTypes.type eq userType
                }.withDistinct().firstOrNull()
                query?.let {
                    return@transaction DBUser.wrapRow(it)
                }

                return@transaction null
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return@withContext null
        }
    }

    suspend fun findUser(slackId: String): DBUser? = withContext(dispatcher) {
        try {
            return@withContext transaction(connection) {
                if(isDebug) {
                    addLogger(StdOutSqlLogger)
                }
                val query = Users.select {
                    Users.slackId eq slackId
                }.withDistinct().first()
                return@transaction DBUser.wrapRow(query)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return@withContext null
        }
    }

    suspend fun getUsers(type: String): List<DBUser> = withContext(dispatcher) {
        try {
            return@withContext  transaction(connection) {
                if(isDebug) {
                    addLogger(StdOutSqlLogger)
                }
                return@transaction DBUser.wrapRows(Users.leftJoin(UserTypes, { this.userType }, { this.type })
                    .select { UserTypes.type eq type }).toList()
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return@withContext mutableListOf<DBUser>()
        }
    }

    suspend fun getRefs(app: String): List<Ref>? = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            Ref.wrapRows(Refs.leftJoin(Apps, { appId }, { id }).select { Apps.name eq app }).toList()
        }
    }

    suspend fun addRef(app: String, ref: Reference) = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            val application = App.find { Apps.name eq app }.first()
            if (Ref.find { (Refs.name eq ref.name) and (Refs.appId eq application.id) and (Refs.type eq ref.type) }.empty()) {
                Ref.new {
                    this.name = ref.name
                    this.deleted = false
                    this.appId = application
                    this.type = ref.type
                }
            }
        }
    }

    suspend fun deleteRef(appName: String, reference: Reference)= withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            App.find { Apps.name eq appName }.firstOrNull()?.let { application ->
                Refs.deleteWhere { (Refs.name eq reference.name) and (Refs.appId eq application.id) and (Refs.type eq reference.type) }
            }
        }
    }

    suspend fun addRefs(refs: List<Reference>, appName: String) = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            App.find { Apps.name eq appName }.firstOrNull()?.let { application ->
                Ref.wrapRows(Refs.select {Refs.appId eq application.id}).forEach { ref ->
                    if(refs.firstOrNull {
                        ref.name == it.name && ref.type == it.type
                    } == null) {
                        ref.delete()
                    }
                }

                refs.forEach {
                    if(Ref.find { (Refs.name eq it.name) and
                                (Refs.appId eq application.id) and
                                (Refs.type eq it.type)}.firstOrNull() == null) {
                        Ref.new {
                            this.name = it.name
                            this.deleted = false
                            this.type = it.type
                            this.appId = application
                        }
                    }
                }
            }
        }
    }

    suspend fun getFlavours(app: String): List<Flavour>? = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            Flavour.wrapRows(Flavours.leftJoin(Apps, { Flavours.appId }, { id }).select { Apps.name eq app })
                .toList()
        }
    }

    suspend fun getBuildTypes(app: String): List<BuildType>? = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            BuildType.wrapRows(BuildTypes.leftJoin(
                Apps,
                { appId },
                { id }).select { Apps.name eq app })
                .toList()
        }
    }

    suspend fun addFlavours(flavours: List<String>, appName: String) = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            App.find { Apps.name eq appName }.firstOrNull()?.let { application ->

                Flavour.wrapRows(Flavours.select { Flavours.appId eq application.id }).forEach {
                    if (it.name !in flavours) {
                        it.delete()
                    }
                }

                flavours.forEach { flavour ->
                    if (Flavour.find { (Flavours.name eq flavour) and (Flavours.appId eq application.id) }.firstOrNull() == null) {
                        Flavour.new {
                            this.name = flavour
                            this.appId = application
                        }
                    }
                }
            }
        }
    }

    suspend fun addBuildVariants(buildTypes: List<String>, appName: String) = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            App.find { Apps.name eq appName }.firstOrNull()?.let { application ->

                BuildType.wrapRows(BuildTypes.select { BuildTypes.appId eq application.id }).forEach {
                    if (it.name !in buildTypes) {
                        it.delete()
                    }
                }


                buildTypes.forEach { buildType ->
                    if (BuildType.find { (BuildTypes.name eq buildType) and (BuildTypes.appId eq application.id) }.firstOrNull() == null) {

                        BuildType.new {
                            this.name = buildType
                            this.appId = application
                        }
                    }
                }
            }
        }
    }

    suspend fun getVerbs() = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            Verb.all().map {
                it.name
            }
        }
    }

    suspend fun addVerbs(verbs: List<String>) = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            Verb.all().filterNot {
                verbs.contains(it.name)
            }.forEach {
                it.delete()
            }

            verbs.forEach { verb ->
                if (Verb.find { Verbs.name eq verb }.firstOrNull() == null) {
                    Verb.new {
                        this.name = verb
                    }
                }
            }
        }
    }

    suspend fun getVerb(verb: String) = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            Verb.wrapRow(Verbs.select { Verbs.name eq verb }.first())
        }
    }

    suspend fun getApi(apiId: String, verbName: String) = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            val verb = runBlocking {
                getVerb(verbName)
            }
            Apis.select { Apis.apiId eq apiId and (Apis.verb eq verb.id) }.firstOrNull()?.let {
                Api.wrapRow(it)
            }
        }
    }

    suspend fun addApi(apiId: String, verb: String, response: String) = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            Verb.find { Verbs.name eq verb }.firstOrNull()?.let { verb ->
                Api.new {
                    this.response = response
                    this.verb = verb
                    this.apiId = apiId
                }
            }
        }
    }
}

object Users : IntIdTable() {
    val name = varchar("name", 100).nullable()
    val email = varchar("email", 50).nullable()
    val slackId = varchar("slack_id", 50).index(isUnique = true)
    val imId = varchar("im_id", 100).nullable()
    val userType =
        reference("user_type", UserTypes, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.RESTRICT)
}

class DBUser(id: EntityID<Int>) : IntEntity(id), Principal {
    companion object : IntEntityClass<DBUser>(Users)

    var name by Users.name
    var email by Users.email
    var slackId by Users.slackId
    var imId by Users.imId
    var userType by UserType referencedOn Users.userType
}

private object UserTypes : IntIdTable() {
    val type = varchar("type", 20).uniqueIndex()
}

class UserType(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserType>(UserTypes)

    var type by UserTypes.type
}

private object Apps : IntIdTable() {
    val name = varchar("app_name", 100).uniqueIndex()
}

class App(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<App>(Apps)

    var name by Apps.name
}

object Refs: IntIdTable() {
    val name = varchar("name", 100).primaryKey()
    val deleted = bool("deleted").default(false)
    val type = enumeration("type", RefType::class).default(RefType.BRANCH)
    val appId = reference("app_id", Apps, ReferenceOption.CASCADE, ReferenceOption.RESTRICT).primaryKey()
}

class Ref(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<Ref>(Refs)

    var name by Refs.name
    var deleted by Refs.deleted
    var appId by App referencedOn Refs.appId
    var type by Refs.type
}

object BuildTypes : IntIdTable() {
    val name = varchar("name", 100).primaryKey()
    val appId = reference("app_id", Apps, ReferenceOption.CASCADE, ReferenceOption.RESTRICT).primaryKey()
}

class BuildType(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BuildType>(BuildTypes)

    var name by BuildTypes.name
    var appId by App referencedOn BuildTypes.appId
}

object Flavours : IntIdTable() {
    val name = varchar("name", 100).primaryKey()
    val appId = reference("app_id", Apps, ReferenceOption.CASCADE, ReferenceOption.RESTRICT).primaryKey()
}

class Flavour(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Flavour>(Flavours)

    var name by Flavours.name
    var appId by App referencedOn Flavours.appId
}

object Verbs : IntIdTable() {
    val name = varchar("name", 20)
}

class Verb(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Verb>(Verbs)

    var name by Verbs.name
}

object Apis : IntIdTable() {
    val apiId = varchar("api_id", 100).uniqueIndex().primaryKey()
    val verb = reference("verb", Verbs, ReferenceOption.CASCADE, ReferenceOption.RESTRICT).primaryKey()
    val response = text("response")
}

class Api(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Api>(Apis)

    var apiId by Apis.apiId
    var verb by Verb referencedOn Apis.verb
    var response by Apis.response
}

object Subscriptions : IntIdTable() {
    val userId = reference(
        "user_id",
        Users,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.RESTRICT
    ).primaryKey()
    val refId = reference(
        "ref_id",
        Refs,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).primaryKey()
    val appId =
        reference(
            "app_id",
            Apps,
            onDelete = ReferenceOption.CASCADE,
            onUpdate = ReferenceOption.RESTRICT
        ).primaryKey()
    val channel = varchar("channel_id", 100).primaryKey()

    init {
        index(true, userId, refId, appId, channel)
    }
}

class Subscription(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Subscription>(Subscriptions)

    var userId by DBUser referencedOn Subscriptions.userId
    var refId by Ref referencedOn Subscriptions.refId
    var appId by App referencedOn Subscriptions.appId
    var channel by Subscriptions.channel
}