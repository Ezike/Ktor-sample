package com.example.repository

import com.example.DatabaseFactory.dbQuery
import com.example.model.EmojiPhrase
import com.example.model.EmojiPhraseTable
import com.example.model.User
import com.example.model.UserTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class EmojiPhraseRepository : Repository {

    override suspend fun add(userId: String, emoji: String, phrase: String): EmojiPhrase? = dbQuery {
        val insert = EmojiPhraseTable.insert {
            it[user] = userId
            it[this.emoji] = emoji
            it[this.phrase] = phrase
        }
        val result = insert.resultedValues?.get(0)
        if (result != null) toEmojiPhrase(result)
        else null
    }

    override suspend fun phrase(id: Int): EmojiPhrase? = dbQuery {
        EmojiPhraseTable.select {
            (EmojiPhraseTable.id eq id)
        }.mapNotNull {
            toEmojiPhrase(it)
        }.singleOrNull()
    }

    override suspend fun phrase(id: String): EmojiPhrase? {
        return phrase(id.toInt())
    }

    override suspend fun phrases(userId: String): List<EmojiPhrase> = dbQuery {
        EmojiPhraseTable.select {
            EmojiPhraseTable.user eq userId
        }.map { toEmojiPhrase(it) }
    }

    override suspend fun remove(id: Int): Boolean {
        if (phrase(id) == null) throw IllegalArgumentException("No phrase found for id: $id")
        return dbQuery {
            EmojiPhraseTable.deleteWhere { EmojiPhraseTable.id eq id } > 0
        }
    }

    override suspend fun remove(id: String): Boolean {
        return remove(id.toInt())
    }

    override suspend fun clear() {
        EmojiPhraseTable.deleteAll()
    }

    override suspend fun user(userId: String, hash: String?): User? {
        val user = dbQuery {
            UserTable.select {
                (UserTable.id eq userId)
            }.mapNotNull {
                toUser(it)
            }
        }.singleOrNull()

        return when {
            user == null -> null
            hash == null -> user
            user.passwordHash == hash -> user
            else -> null
        }
    }

    override suspend fun userByEmail(email: String): User? = dbQuery {
        UserTable.select {
            (UserTable.email eq email)
        }.map { toUser(it) }.singleOrNull()
    }

    override suspend fun createUser(user: User) =
        dbQuery {
            UserTable.insert {
                it[id] = user.userId
                it[displayName] = user.displayName
                it[email] = user.email
                it[passwordHash] = user.passwordHash
            }
            Unit
        }

    override suspend fun userById(userId: String): User? =
        dbQuery {
            UserTable.select {
                (UserTable.id eq userId)
            }.map { toUser(it) }.singleOrNull()
        }

    private fun toEmojiPhrase(row: ResultRow) = EmojiPhrase(
        id = row[EmojiPhraseTable.id].value,
        emoji = row[EmojiPhraseTable.emoji],
        phrase = row[EmojiPhraseTable.phrase],
        userId = row[EmojiPhraseTable.user]
    )

    private fun toUser(row: ResultRow) = User(
        userId = row[UserTable.id],
        email = row[UserTable.email],
        displayName = row[UserTable.displayName],
        passwordHash = row[UserTable.passwordHash]
    )
}