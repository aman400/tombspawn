package com.tombspawn.data

import com.tombspawn.models.Reference
import com.tombspawn.models.github.RefType
import com.tombspawn.utils.Constants
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext


class DatabaseService constructor(dbUrl: String?, dbUsername: String?, dbPass: String?, private val isDebug: Boolean) {
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
            SchemaUtils.createMissingTablesAndColumns(Users, UserTypes, Apps, Subscriptions, Refs)
        }
    }

    fun clear() {
        connectionPool.close()
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

    suspend fun addApps(appNames: List<com.tombspawn.models.config.App>) = withContext(dispatcher) {
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

    suspend fun getRefs(app: String, refType: RefType? = null): List<Ref>? = withContext(dispatcher) {
        return@withContext transaction(connection) {
            if(isDebug) {
                addLogger(StdOutSqlLogger)
            }
            Ref.wrapRows(Refs.leftJoin(Apps, { appId }, { id }).select { Apps.name eq app }.apply {
                if(refType != null) {
                    this.andWhere { Refs.type eq refType }
                }
            }).toList()
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
}