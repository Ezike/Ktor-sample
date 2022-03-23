package com.example.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

@Serializable
data class EmojiPhrase(
    val userId: String,
    val emoji: String,
    val phrase: String,
    var id: Int? = null
) : java.io.Serializable

object EmojiPhraseTable : IntIdTable() {
    val user = varchar("user_id", 20).index()
    val emoji: Column<String> = varchar("emoji", 255)
    val phrase: Column<String> = varchar("phrase", 255)
}