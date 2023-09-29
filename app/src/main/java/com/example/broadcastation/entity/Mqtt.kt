package com.example.broadcastation.entity

import java.util.UUID

data class Mqtt (val domain: String, val port: String?, val channel: String, val content: String)