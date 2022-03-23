@file:OptIn(KtorExperimentalLocationsAPI::class)

package com.example.api

import com.example.API_VERSION
import com.example.model.Request
import com.example.repository.Repository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.locations.KtorExperimentalLocationsAPI
import io.ktor.server.locations.Location
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

const val PHRASE_ENDPOINT = "$API_VERSION/phrase"

@Location(PHRASE_ENDPOINT)
class Phrase

fun Route.phrase(db: Repository) {
    post<Phrase> {
        val request = call.receive<Request>()
        val phrase = db.add(
            userId = "",
            emoji = request.emoji,
            phrase = request.phrase
        )
        if (phrase != null) call.respond(phrase)
        else call.respondText(
            "Invalid data received",
            status = HttpStatusCode.InternalServerError
        )
    }
}