@file:OptIn(KtorExperimentalLocationsAPI::class)

package com.example.api

import com.example.JwtService
import com.example.hash
import com.example.redirect
import com.example.repository.Repository
import io.ktor.http.Parameters
import io.ktor.server.application.call
import io.ktor.server.locations.KtorExperimentalLocationsAPI
import io.ktor.server.locations.Location
import io.ktor.server.locations.post
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route

const val LOGIN = "/login"

@Location(LOGIN)
class Login

fun Route.login(db: Repository, jwtService: JwtService) {
    post<Login> {
        val params = call.receive<Parameters>()
        val userId = params["userId"] ?: return@post call.redirect(it)
        val password = params["password"] ?: return@post call.redirect(it)

        val user = db.user(userId, hash(password))
        if (user != null) {
            val token = jwtService.generateToken(user)
            call.respondText(token)
        } else call.respondText("Invalid user")
    }
}