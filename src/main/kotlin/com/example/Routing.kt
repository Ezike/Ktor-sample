@file:OptIn(KtorExperimentalLocationsAPI::class)

package com.example

import com.example.api.about
import com.example.api.home
import com.example.api.login
import com.example.api.phrase
import com.example.api.phraseApi
import com.example.api.phrases
import com.example.api.signIn
import com.example.api.signOut
import com.example.api.signUp
import com.example.model.EpSession
import com.example.model.User
import com.example.repository.EmojiPhraseRepository
import com.example.repository.Repository
import freemarker.cache.ClassTemplateLoader
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.freemarker.FreeMarker
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.locations.KtorExperimentalLocationsAPI
import io.ktor.server.locations.Locations
import io.ktor.server.locations.locations
import io.ktor.server.plugins.ContentNegotiation
import io.ktor.server.plugins.DefaultHeaders
import io.ktor.server.plugins.StatusPages
import io.ktor.server.request.header
import io.ktor.server.request.host
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import io.ktor.server.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import java.net.URI
import java.util.concurrent.TimeUnit

const val API_VERSION = "/api/v1"

fun Application.module() {
    install(DefaultHeaders)
    install(StatusPages) {
        exception<Throwable> { call, e ->
            call.respondText(
                e.localizedMessage,
                ContentType.Text.Plain,
                HttpStatusCode.InternalServerError
            )
        }
    }
    install(ContentNegotiation) { json() }
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    install(Locations)
    install(Sessions) {
        cookie<EpSession>("SESSION") {
            transform(SessionTransportTransformerMessageAuthentication(hashKey))
        }
    }

    DatabaseFactory.init()
    val db = EmojiPhraseRepository()
    val jwtService = JwtService()

    install(Authentication) {
        jwt("jwt") {
            verifier(jwtService.verifier)
            realm = "enojiphrases app"
            validate {
                val payload = it.payload
                val claim = payload.getClaim("id").asString()
                val user = db.userById(claim)
                user
            }
        }
    }
    configureRouting(db, jwtService)
}

fun Application.configureRouting(db: Repository, jwtService: JwtService) {
    val hashFunction = { s: String -> hash(s) }
    routing {
        static("/static") {
            resources("images")
        }
        home(db)
        about(db)
        phrase(db)
        phraseApi(db)
        phrases(db, hashFunction)
        login(db, jwtService)
        signUp(db, hashFunction)
        signIn(db, hashFunction)
        signOut()
    }
}

suspend fun ApplicationCall.redirect(location: Any) {
    respondRedirect(application.locations.href(location))
}

fun ApplicationCall.referrerHost() = request.header(HttpHeaders.Referrer)?.let {
    URI.create(it).host
}

fun ApplicationCall.securityCode(date: Long, user: User, hashfxn: (String) -> String) =
    hashfxn("$date:{${user.userId}:${request.host()}:${referrerHost()}")

fun ApplicationCall.verifyCode(date: Long, user: User, code: String, hashfxn: (String) -> String) =
    securityCode(date, user, hashfxn) == code &&
        (System.currentTimeMillis() - date).let { it > 0 && it < TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS) }

val ApplicationCall.apiUser
    get() = authentication.principal<User>()
