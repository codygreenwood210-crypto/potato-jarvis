package com.potato.jarvis.core

import java.net.URI

object BackendUrlPolicy {
    fun normalize(raw: String, allowHttp: Boolean): String {
        val clean = raw.trim().trimEnd('/')
        require(clean.isNotBlank()) { "Backend URL is not configured. Open Settings to connect POTATO." }
        val uri = runCatching { URI(clean) }.getOrElse { throw IllegalArgumentException("Backend URL is invalid.") }
        require(uri.scheme == "http" || uri.scheme == "https") { "Backend URL must use http:// or https://." }
        require(uri.host?.isNotBlank() == true) { "Backend URL must include a host." }
        require(uri.userInfo == null) { "Backend URL must not contain embedded credentials." }
        require(uri.query == null && uri.fragment == null) { "Backend URL must not contain a query or fragment." }
        require(uri.path.isNullOrBlank() || uri.path == "/") { "Backend URL must not contain a path." }
        if (!allowHttp) require(uri.scheme == "https") { "Release builds require an HTTPS backend URL." }
        return clean
    }
}
