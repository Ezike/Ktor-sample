package com.example.repository

import com.example.model.EmojiPhrase
import com.example.model.User

interface Repository {
    suspend fun add(userId: String, emoji: String, phrase: String): EmojiPhrase?
    suspend fun phrase(id: Int): EmojiPhrase?
    suspend fun phrase(id: String): EmojiPhrase?
    suspend fun phrases(userId: String): List<EmojiPhrase>
    suspend fun remove(id: Int): Boolean
    suspend fun remove(id: String): Boolean
    suspend fun clear()

    suspend fun user(userId: String, hash: String? = null): User?
    suspend fun userByEmail(email: String): User?
    suspend fun createUser(user: User)
    suspend fun userById(userId: String): User?
}