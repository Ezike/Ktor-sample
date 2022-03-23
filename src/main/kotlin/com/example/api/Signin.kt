@file:OptIn(KtorExperimentalLocationsAPI::class)

package com.example.api

import com.example.MIN_PASSWORD_LENGTH
import com.example.MIN_USER_ID_LENGTH
import com.example.model.EpSession
import com.example.redirect
import com.example.repository.Repository
import com.example.userNameValid
import io.ktor.http.Parameters
import io.ktor.server.application.call
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.locations.KtorExperimentalLocationsAPI
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.locations.post
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set

const val SIGNIN = "/signin"

@Location(SIGNIN)
data class SignIn(
    val userId: String = "",
    val error: String = ""
)

fun Route.signIn(db: Repository, hashFunction: (String) -> String) {
    post<SignIn> {
        val signInParameters = call.receive<Parameters>()
        val userId = signInParameters["userId"] ?: return@post call.redirect(it)
        val password = signInParameters["password"] ?: return@post call.redirect(it)

        val signInError = SignIn(userId)

        val signin = when {
            userId.length < MIN_USER_ID_LENGTH -> null
            password.length < MIN_PASSWORD_LENGTH -> null
            !userNameValid(userId) -> null
            else -> db.user(userId, hashFunction(password))
        }

        if (signin == null) {
            call.redirect(signInError.copy(error = "Invalid username or password"))
        } else {
            call.sessions.set(EpSession(signin.userId))
            call.redirect(Phrases())
        }

    }

    get<SignIn> {
        val user = call.sessions.get<EpSession>()?.let { db.user(it.userId) }
        if (user != null) call.redirect(Home())
        else call.respond(FreeMarkerContent("signin.ftl", mapOf("userId" to it.userId, "error" to it.error), ""))
    }
}