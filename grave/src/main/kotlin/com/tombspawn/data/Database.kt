package com.tombspawn.data

import com.tombspawn.models.github.RefType
import io.ktor.auth.Principal
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption


object Users : IntIdTable() {
    val name = varchar("name", 100).nullable()
    val email = varchar("email", 50).nullable()
    val slackId = varchar("slack_id", 50)
    val imId = varchar("im_id", 100).nullable()
    val userType =
        reference("user_type", UserTypes, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.RESTRICT)
    override val primaryKey: PrimaryKey = PrimaryKey(id, slackId)
}

class DBUser(id: EntityID<Int>) : IntEntity(id), Principal {
    companion object : IntEntityClass<DBUser>(Users)

    var name by Users.name
    var email by Users.email
    var slackId by Users.slackId
    var imId by Users.imId
    var userType by UserType referencedOn Users.userType
}

object UserTypes : IntIdTable() {
    val type = varchar("type", 20)
    override val primaryKey = PrimaryKey(id, type)
}

class UserType(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserType>(UserTypes)

    var type by UserTypes.type
}

object Apps : IntIdTable() {
    val name = varchar("app_name", 100).uniqueIndex()
}

class App(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<App>(Apps)

    var name by Apps.name
}

object Refs: IntIdTable() {
    val name = varchar("name", 100)
    val deleted = bool("deleted").default(false)
    val type = enumeration("type", RefType::class).default(RefType.BRANCH)
    val appId = reference("app_id", Apps, ReferenceOption.CASCADE, ReferenceOption.RESTRICT)
    override val primaryKey: PrimaryKey =  PrimaryKey(id, name, appId)
}

class Ref(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<Ref>(Refs)

    var name by Refs.name
    var deleted by Refs.deleted
    var appId by App referencedOn Refs.appId
    var type by Refs.type
}

object Subscriptions : IntIdTable() {
    val userId = reference(
        "user_id",
        Users,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.RESTRICT
    )
    val refId = reference(
        "ref_id",
        Refs,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val appId =
        reference(
            "app_id",
            Apps,
            onDelete = ReferenceOption.CASCADE,
            onUpdate = ReferenceOption.RESTRICT
        )
    val channel = varchar("channel_id", 100)
    override val primaryKey: PrimaryKey =  PrimaryKey(id, userId, refId, appId, channel)
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