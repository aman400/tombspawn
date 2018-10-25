package com.ramukaka.models.database

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Users : IntIdTable() {
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