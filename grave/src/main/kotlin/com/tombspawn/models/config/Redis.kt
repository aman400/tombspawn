package com.tombspawn.models.config

import redis.clients.jedis.Protocol

data class Redis(val host: String = Protocol.DEFAULT_HOST,
                 val port: Int = Protocol.DEFAULT_PORT)