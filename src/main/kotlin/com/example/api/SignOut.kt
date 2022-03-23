@file:OptIn(KtorExperimentalLocationsAPI::class)

package com.example.api

import com.example.model.EpSession
import com.example.redirect
import io.ktor.server.application.call
import io.ktor.server.locations.KtorExperimentalLocationsAPI
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.routing.Route
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.sessions

const val SIGNOUT = "/signout"

@Location(SIGNOUT)
class SignOut

fun Route.signOut() {
    get<SignOut> {
        call.sessions.clear<EpSession>()
        call.redirect(SignIn())
    }
}