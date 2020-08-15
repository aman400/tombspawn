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

object UserTypes : IntIdTable() {
    val type = varchar("type", 20).uniqueIndex()
}

class UserType(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserType>(UserTypes)

    var type by UserTypes.type
}

object Apps : IntIdTable() {
    val name = varchar("app_name", 100).uniqueIndex()
//    val appId = varchar("app_id", 100).uniqueIndex()
//    val type = varchar("output_dir", 200)
//    val useCache = bool("use_cache").default(false)
//    val outputDir = varchar("output_dir", 200)
//    val gitUsername = varchar("git_username", 200).nullable()
//    val gitPassword = varchar("git_password", 200).nullable()
//    val tagConfig = reference("tag_config", GitRefConfig, ReferenceOption.CASCADE, ReferenceOption.RESTRICT).nullable()
//    val branchConfig = reference("branch_config", GitRefConfig, ReferenceOption.CASCADE, ReferenceOption.RESTRICT).nullable()
}

class App(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<App>(Apps)

    var name by Apps.name
//    var appId by Apps.appId
//    var type by Apps.type
//    var useCache by Apps.useCache
//    var outputDir by Apps.outputDir
//    var gitUsername by Apps.gitUsername
//    var gitPassword by Apps.gitPassword
//    var tagConfig by Apps.tagConfig
//    var branchConfig by Apps.branchConfig
}
//
//object GitRefConfig: IntIdTable() {
//    val count = integer("count").default(50)
//    val regex = varchar("ref_regex", 200).default("\\p{ASCII}*\$")
//}
//
//object Tasks: IntIdTable() {
//    val taskName = varchar("task_name", 200)
//}
//
//class Task(id: EntityID<Int>): IntEntity(id) {
//    companion object: IntEntityClass<Task>(Tasks)
//
//    var taskName by Tasks.taskName
//}
//
//object TaskMappings: IntIdTable() {
//    val appID = reference("app_id", Apps, ReferenceOption.CASCADE, ReferenceOption.RESTRICT)
//    val taskID = reference("task_id", Tasks, ReferenceOption.CASCADE, ReferenceOption.RESTRICT)
//}
//
//class TaskMapping(id: EntityID<Int>): IntEntity(id) {
//    companion object: IntEntityClass<TaskMapping>(TaskMappings)
//    var appId by App referencedOn TaskMappings.appID
//    var taskId by Task referencedOn TaskMappings.taskID
//}

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
}

class Subscription(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Subscription>(Subscriptions)

    var user by DBUser referencedOn Subscriptions.userId
    var ref by Ref referencedOn Subscriptions.refId
    var app by App referencedOn Subscriptions.appId
    var channel by Subscriptions.channel
}