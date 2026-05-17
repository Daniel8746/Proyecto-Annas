package com.annas.data.services.interceptors

import com.annas.data.exceptions.NoNetworkException
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

class ConnectVerifierInterceptor @Inject constructor(
    private val networkMonitor: NetworkMonitorService
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            val request = chain.request()

            if (!networkMonitor.isConnected()) {
                throw NoNetworkException("Network Error")
            }

            chain.proceed(request)
        } catch (_: NoNetworkException) {
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .message("No network")
                .body("".toResponseBody("application/json".toMediaTypeOrNull())).code(499).build()
        }
    }
}