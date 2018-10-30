package com.ramukaka.data

import com.ramukaka.utils.Constants
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.coroutines.CoroutineContext


class Database(application: Application, dbUrl: String, dbUsername: String, dbPass: String) {
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
        dispatcher = newFixedThreadPoolContext(20, "database-pool")

        connectionPool = HikariDataSource(config)
        connection = Database.connect(connectionPool)
        transaction(connection) {
            SchemaUtils.create(Users, UserTypes, Apps, Branches, Subscriptions)
        }
    }

    suspend fun userExists(userId: String?): Boolean = withContext(dispatcher) {
        if (userId.isNullOrEmpty()) {
            return@withContext false
        }
        return@withContext transaction(connection) {
            addLogger(StdOutSqlLogger)
            val user = User.find { Users.slackId eq userId }
            return@transaction !user.empty()
        }
    }

    suspend fun addApp(appName: String) = withContext(dispatcher) {
        transaction(connection) {
            addLogger(StdOutSqlLogger)
            if (App.find { Apps.name eq appName }.limit(1).firstOrNull() == null) {
                App.new {
                    name = appName
                }
            }
        }

    }

    suspend fun addApps(appNames: List<String>) = withContext(dispatcher) {
        transaction(connection) {
            addLogger(StdOutSqlLogger)
            appNames.forEach {
                if (App.find { Apps.name eq it }.limit(1).firstOrNull() == null) {
                    App.new {
                        this.name = it
                    }
                }
            }
        }
    }

    suspend fun addUser(
        userId: String,
        name: String? = null,
        email: String? = null,
        typeString: String = Constants.Database.USER_TYPE_USER
    ) = withContext(dispatcher) {
        transaction(connection) {
            addLogger(StdOutSqlLogger)
            val types = UserType.find { UserTypes.type eq typeString }.limit(1)
            val type = types.firstOrNull() ?: UserType.new {
                this.type = typeString
            }
            try {
                if (User.find { Users.slackId eq userId }.firstOrNull() == null) {
                    User.new {
                        this.name = name
                        this.email = email
                        this.slackId = userId
                        this.userType = type
                    }
                }
            } catch (exception: ExposedSQLException) {
                exception.printStackTrace()
            }
        }
    }

    suspend fun getUser(userType: String): User? = withContext(dispatcher) {
        try {
            return@withContext transaction(connection) {
                addLogger(StdOutSqlLogger)
                val query = Users.innerJoin(UserTypes, {
                    Users.userType
                }, {
                    UserTypes.id
                }).slice(Users.columns).select {
                    UserTypes.type eq userType
                }.withDistinct().first()
                return@transaction User.wrapRow(query)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return@withContext null
        }
    }

    suspend fun addBranch(branch: String, app: String) = withContext(dispatcher) {
        return@withContext transaction(connection) {
            addLogger(StdOutSqlLogger)
            val application = App.find { Apps.name eq app }.first()
            if (Branch.find { (Branches.name eq branch) and (Branches.appId eq application.id) }.empty()) {
                Branch.new {
                    this.branchName = branch
                    this.deleted = false
                    this.appId = application
                }
            }
        }
    }

    suspend fun deleteBranch(branch: String) = withContext(dispatcher) {
        return@withContext transaction(connection) {
            addLogger(StdOutSqlLogger)
            Branches.deleteWhere { Branches.name eq branch }
        }
    }

    suspend fun addBranches(branches: List<String>, app: String) = withContext(dispatcher) {
        return@withContext transaction(connection) {
            addLogger(StdOutSqlLogger)
            branches.forEach { branch ->
                val application = App.find { Apps.name eq app }.firstOrNull()
                if (Branch.find {(Branches.name eq(branch) and (Branches.appId eq application!!.id))}.empty()) {
                    Branch.new {
                        this.branchName = branch
                        this.deleted = false
                        this.appId = application!!
                    }
                }
            }

        }
    }
}

private object Users : IntIdTable() {
    val name = varchar("name", 100).nullable()
    val email = varchar("email", 50).nullable()
    val slackId = varchar("slack_id", 50).index(isUnique = true)
    val userType =
        reference("user_type", UserTypes, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.RESTRICT)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var email by Users.email
    var slackId by Users.slackId
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

private object Branches : IntIdTable() {
    val name = varchar("branch_name", 100).uniqueIndex().primaryKey()
    val deleted = bool("deleted").default(false)
    val appId = reference("app_id", Apps, ReferenceOption.CASCADE, ReferenceOption.RESTRICT).primaryKey()
}


class Branch(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Branch>(Branches)

    var branchName by Branches.name
    var appId by App referencedOn Branches.appId
    var deleted by Branches.deleted
}

private object Subscriptions : IntIdTable() {
    val userId = reference(
        "user_id",
        Users,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.RESTRICT
    ).primaryKey()
    val branchId = reference(
        "branch_id",
        Branches,
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
    val channel = varchar("channel_id", 100)

    init {
        index(true, userId, branchId, appId)
    }
}

class Subscription(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Subscription>(Subscriptions)

    val userId by User referencedOn Subscriptions.userId
    val branchId by Branch referencedOn Subscriptions.branchId
    val appId by App referencedOn Subscriptions.appId
    val channel by Subscriptions.channel
}