@file:OptIn(KtorExperimentalLocationsAPI::class)

package com.example.api

import com.example.model.EpSession
import com.example.redirect
import com.example.repository.Repository
import com.example.securityCode
import com.example.verifyCode
import io.ktor.server.application.call
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.locations.KtorExperimentalLocationsAPI
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.locations.post
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions

const val PHRASES = "/phrases"

@Location(PHRASES)
class Phrases

fun Route.phrases(db: Repository, hashfxn: (String) -> String) {
    get<Phrases> {
        val user = call.sessions.get<EpSession>()?.let { db.user(it.userId) }
        if (user == null) call.redirect(SignIn())
        else {
            val data = db.phrases(user.userId)
            val date = System.currentTimeMillis()
            val code = call.securityCode(date, user, hashfxn)
            call.respond(
                FreeMarkerContent(
                    "phrases.ftl", mapOf(
                        "phrases" to data,
                        "user" to user,
                        "date" to date,
                        "code" to code
                    ), user.userId
                )
            )
        }
    }
    post<Phrases> {
        val user = call.sessions.get<EpSession>()?.let { db.user(it.userId) }

        val params = call.receiveParameters()
        val date = params["date"]?.toLongOrNull() ?: return@post call.redirect(it)
        val code = params["code"] ?: return@post call.redirect(it)

        if (user == null || !call.verifyCode(date, user, code, hashfxn)) return@post call.redirect(SignIn())

        val action = params["action"] ?: throw IllegalArgumentException("Missing parameter: Action")

        when (action) {
            "delete" -> {
                val id = params["id"] ?: throw IllegalArgumentException("Missing parameter: Id")
                db.remove(id)
            }
            "add" -> {
                val emoji = params["emoji"] ?: throw IllegalArgumentException("Missing parameter: Emoji")
                val phrase = params["phrase"] ?: throw IllegalArgumentException("Missing parameter: Phrase")
                db.add(user.userId, emoji, phrase)
            }
        }

        call.redirect(Phrases())
    }
}