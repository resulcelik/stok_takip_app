// NetworkModule.kt
package com.example.stokkontrolveyonetimsistemi.di

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.BuildConfig // ✅ Uygulama BuildConfig
import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import com.example.stokkontrolveyonetimsistemi.data.network.api.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

val networkModule = module {

    // Core
    single<TokenStorage> { TokenStorage(androidContext()) }
    single<Gson> {
        GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setLenient().create()
    }

    // Interceptors
    single<Interceptor>(qualifier = org.koin.core.qualifier.named("auth")) {
        Interceptor { chain ->
            val tokenStorage: TokenStorage = get()
            val b = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
            if (tokenStorage.hasValidToken()) {
                b.addHeader(ApiConstants.HEADER_AUTHORIZATION,
                    "${ApiConstants.BEARER_PREFIX}${tokenStorage.getToken()}")
            }
            chain.proceed(b.build())
        }
    }

    single<Interceptor>(qualifier = org.koin.core.qualifier.named("error")) {
        Interceptor { chain ->
            try {
                val resp = chain.proceed(chain.request())
                Log.d("NetworkModule", "HTTP ${resp.code} - ${resp.request.url}")
                resp
            } catch (e: Exception) {
                Log.e("NetworkModule", "Network error: ${e.localizedMessage}", e)
                throw e
            }
        }
    }

    single<HttpLoggingInterceptor> {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
    }

    // OkHttpClient (ZORLA TRUST-ALL)
    single<OkHttpClient> {
        val auth: Interceptor = get(org.koin.core.qualifier.named("auth"))
        val err: Interceptor = get(org.koin.core.qualifier.named("error"))
        val log: HttpLoggingInterceptor = get()

        // 🔒 Trust-all TrustManager
        val trustAllManagers = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val x509 = trustAllManagers[0] as X509TrustManager

        // 🔒 TLS context
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllManagers, SecureRandom())

        // 🔒 Modern TLS + CLEARTEXT (IP testleri için)
        val tlsSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
            .build()

        val builder = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, x509) // ✅ Zorla
            .hostnameVerifier { hostname, _ ->
                Log.w("NetworkModule", "⚠️ Hostname bypass: $hostname")
                true
            }
            .connectionSpecs(listOf(tlsSpec, ConnectionSpec.CLEARTEXT))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(err)
            .addInterceptor(auth)
            .addInterceptor(log)
            .retryOnConnectionFailure(true)

        Log.w("NetworkModule", "⚠️ TRUST-ALL SSL ENABLED (FOR DEV/TEST)!")

        builder.build()
    }

    // Retrofit
    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL) // https://10.10.10.65:8080/
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    // API services
    single<AuthApiService> { get<Retrofit>().create(AuthApiService::class.java) }
    single<LocationApiService> { get<Retrofit>().create(LocationApiService::class.java) }
    single<UserApiService> { get<Retrofit>().create(UserApiService::class.java) }
    single<RafEtiketApiService> { get<Retrofit>().create(RafEtiketApiService::class.java) }
    single<UrunEtiketApiService> { get<Retrofit>().create(UrunEtiketApiService::class.java) }
}
