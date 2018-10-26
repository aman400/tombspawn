package com.ramukaka.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import models.slack.UserProfile
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
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
    }

    suspend fun userExists(userId: String?): Boolean = withContext(dispatcher) {
        if(userId.isNullOrEmpty()) {
            return@withContext false
        }
        return@withContext transaction(connection) {
            addLogger(StdOutSqlLogger)
            val user = User.find { Users.slackId eq userId }
            return@transaction !user.empty()
        }
    }

    suspend fun addUser(user: UserProfile, userId: String) = withContext(dispatcher) {
        transaction(connection) {
            addLogger(StdOutSqlLogger)
            User.new {
                name = user.name!!
                email = user.email!!
                slackId = userId
            }
        }
    }
}

private object Users : IntIdTable() {
    val name = varchar("name", 100)
    val email = varchar("email", 50)
    val slackId = varchar("slack_id", 50).index(isUnique = true)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var email by Users.email
    var slackId by Users.slackId
}