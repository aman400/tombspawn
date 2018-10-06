package com.ramukaka.network.utils

/**
 * Created by aman on Thu 10/05/18 09:55.
 */
object Headers {
    /**
     * The constant APP_CLIENT.
     */
    private const val APP_CLIENT = "client"
    /**
     * The constant APP_CLIENT_VALUE.
     */
    private const val APP_CLIENT_VALUE = "android"
    /**
     * The constant ACCEPT.
     */
    private const val ACCEPT = "Accept"
    /**
     * The constant CONTENT_TYPE.
     */
    private const val CONTENT_TYPE = "Content-Type"
    /**
     * The constant TYPE_JSON.
     */
    private const val TYPE_JSON = "application/json"
    /**
     * The constant HEADER_ENCODING.
     */
    private const val HEADER_ENCODING = "Content-Encoding"

    val basicHeaders: MutableMap<String, String> = mutableMapOf()
        get() {
            field[Headers.APP_CLIENT] = Headers.APP_CLIENT_VALUE
            field[CONTENT_TYPE] = TYPE_JSON
            field[ACCEPT] = TYPE_JSON
            return field
        }
}