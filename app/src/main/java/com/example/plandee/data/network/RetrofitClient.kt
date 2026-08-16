package com.example.plandee.data.network

import android.content.Context
import com.example.plandee.data.security.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 10.0.2.2 is the Android Emulator loopback IP pointing to localhost:8080 on the host machine
    private const val BASE_URL = "http://10.0.2.2:8080/"

    @Volatile
    private var instance: ApiService? = null

    fun getApiService(context: Context): ApiService {
        return instance ?: synchronized(this) {
            instance ?: createApiService(context.applicationContext).also { instance = it }
        }
    }

    private fun createApiService(context: Context): ApiService {
        val sessionManager = SessionManager.getInstance(context)
        val authInterceptor = AuthInterceptor(sessionManager)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
