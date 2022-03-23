package com.example.model

import io.ktor.server.auth.Principal
import org.jetbrains.exposed.sql.Table

data class User(
    val userId: String,
    val email: String,
    val passwordHash: String,
    val displayName: String
) : Principal, java.io.Serializable

object UserTable : Table() {
    val id = varchar("id", 20)
    val email = varchar("email", 128).uniqueIndex()
    val displayName = varchar("displayName", 256)
    val passwordHash = varchar("passwordHash", 64)
    override val primaryKey: PrimaryKey = PrimaryKey(columns = arrayOf(id))
}
