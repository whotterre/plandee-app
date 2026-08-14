package com.example.plandee.data.network

import com.example.plandee.data.security.SessionManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(private val sessionManager: SessionManager) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code == 401) {
            sessionManager.clearSession()
        }
        return null
    }
}
