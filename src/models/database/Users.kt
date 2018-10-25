package com.ramukaka.models.database

import models.slack.UserProfile
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object Users : IntIdTable() {
    val name = varchar("name", 100)
    val email = varchar("email", 50)
    val slackId = varchar("slack_id", 50).index(isUnique = true)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users) {
        fun exists(userId: String?): Boolean {
            if(userId.isNullOrEmpty()) {
                return false
            }
            return transaction {
                addLogger(StdOutSqlLogger)
                val user = find { Users.slackId eq userId }
                !user.empty()
            }
        }

        fun add(user: UserProfile, userId: String) {
            transaction {
                addLogger(StdOutSqlLogger)
                User.new {
                    name = user.name!!
                    email = user.email!!
                    slackId = userId
                }
            }
        }
    }

    var name by Users.name
    var email by Users.email
    var slackId by Users.slackId
}