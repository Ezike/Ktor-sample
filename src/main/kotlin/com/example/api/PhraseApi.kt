@file:OptIn(KtorExperimentalLocationsAPI::class)

package com.example.api

import com.example.API_VERSION
import com.example.api.request.PhraseApiRequest
import com.example.apiUser
import com.example.redirect
import com.example.repository.Repository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.locations.KtorExperimentalLocationsAPI
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.locations.post
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route

@Location("/$API_VERSION/phrases")
class PhraseApi

fun Route.phraseApi(db: Repository) {
    authenticate("jwt") {
        get<PhraseApi> {
            val user = call.apiUser ?: return@get call.redirect(SignIn())
            call.respond(db.phrases(user.userId))
        }
        post<PhraseApi> {
            val user = call.apiUser ?: return@post call.redirect(SignIn())
            try {
                val request = call.receive<PhraseApiRequest>()
                val phrase = db.add(user.userId, request.emoji, request.phrase)
                if (phrase != null) call.respond(phrase)
                else call.respondText("Invalid data received", status = HttpStatusCode.InternalServerError)
            } catch (e: Throwable) {
                call.respondText("Invalid data received", status = HttpStatusCode.BadRequest)
            }
        }
    }
}