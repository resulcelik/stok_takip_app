package com.example.stokkontrolveyonetimsistemi.di

import com.example.stokkontrolveyonetimsistemi.BuildConfig
import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import com.example.stokkontrolveyonetimsistemi.data.network.api.AuthApiService
import com.example.stokkontrolveyonetimsistemi.data.network.api.LocationApiService
import com.example.stokkontrolveyonetimsistemi.data.network.api.RafEtiketApiService
import com.example.stokkontrolveyonetimsistemi.data.network.api.UserApiService
import com.example.stokkontrolveyonetimsistemi.data.network.api.UrunEtiketApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network layer dependency injection module
 * ✅ FIXED: UserApiService, RafEtiketApiService, UrunEtiketApiService eklendi
 */
val networkModule = module {

    // ==========================================
    // CORE DEPENDENCIES
    // ==========================================

    // TokenStorage - Secure JWT storage
    single<TokenStorage> { TokenStorage(androidContext()) }

    // Gson for JSON serialization
    single<Gson> {
        GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .setLenient()
            .create()
    }

    // ==========================================
    // HTTP INTERCEPTORS
    // ==========================================

    // JWT Authentication Interceptor
    single<Interceptor>(qualifier = org.koin.core.qualifier.named("auth")) {
        Interceptor { chain ->
            val tokenStorage: TokenStorage = get()
            val originalRequest = chain.request()

            val newRequest = if (tokenStorage.hasValidToken()) {
                originalRequest.newBuilder()
                    .addHeader(
                        ApiConstants.HEADER_AUTHORIZATION,
                        "${ApiConstants.BEARER_PREFIX}${tokenStorage.getToken()}"
                    )
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
            } else {
                originalRequest.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
            }

            chain.proceed(newRequest)
        }
    }

    // Error handling interceptor
    single<Interceptor>(qualifier = org.koin.core.qualifier.named("error")) {
        Interceptor { chain ->
            try {
                val response = chain.proceed(chain.request())

                // Log network status
                android.util.Log.d(
                    "NetworkModule",
                    "HTTP ${response.code} - ${response.request.url}"
                )

                response
            } catch (e: Exception) {
                android.util.Log.e("NetworkModule", "Network error: ${e.localizedMessage}", e)
                throw e
            }
        }
    }

    // HTTP logging interceptor
    single<HttpLoggingInterceptor> {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    // ==========================================
    // HTTP CLIENT CONFIGURATION
    // ==========================================

    // OkHttpClient with interceptors and timeouts
    single<OkHttpClient> {
        val authInterceptor: Interceptor = get(qualifier = org.koin.core.qualifier.named("auth"))
        val errorInterceptor: Interceptor = get(qualifier = org.koin.core.qualifier.named("error"))
        val loggingInterceptor: HttpLoggingInterceptor = get()

        OkHttpClient.Builder()
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(errorInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .retryOnConnectionFailure(true)
            .build()
    }

    // ==========================================
    // RETROFIT CONFIGURATION
    // ==========================================

    // Main Retrofit instance
    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
            .build()
    }

    // ==========================================
    // API SERVICES
    // ==========================================

    /**
     * Authentication API service
     * Login, password operations
     */
    single<AuthApiService> {
        val retrofit: Retrofit = get()
        retrofit.create(AuthApiService::class.java)
    }

    /**
     * Location API service
     * Cascade dropdown'lar için
     */
    single<LocationApiService> {
        val retrofit: Retrofit = get()
        retrofit.create(LocationApiService::class.java)
    }

    /**
     * ✅ NEW: User API service
     * User session ve lokasyon management
     */
    single<UserApiService> {
        val retrofit: Retrofit = get()
        retrofit.create(UserApiService::class.java)
    }

    /**
     * ✅ NEW: RAF Etiket API service
     * RAF etiket üretimi için
     */
    single<RafEtiketApiService> {
        val retrofit: Retrofit = get()
        retrofit.create(RafEtiketApiService::class.java)
    }

    /**
     * ✅ NEW: Ürün Etiket API service
     * Ürün etiket üretimi için
     */
    single<UrunEtiketApiService> {
        val retrofit: Retrofit = get()
        retrofit.create(UrunEtiketApiService::class.java)
    }
}