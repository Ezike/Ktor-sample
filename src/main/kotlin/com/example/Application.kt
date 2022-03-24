package com.example

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = System.getenv("PORT").toInt()) {
        module()
    }.start(wait = true)
}
