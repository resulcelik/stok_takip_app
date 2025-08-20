package com.example.stokkontrolveyonetimsistemi.core.constants

/**
 * API Constants for Backend Integration
 * Backend server endpoint URL'leri ve HTTP status code'ları
 * ✅ FIXED: RAF etiket endpoint'leri backend'e uygun güncellendi
 *
 * Backend Base URL: http://192.168.1.154:8080
 * Authentication: Bearer JWT Token required (except auth endpoints)
 */
object ApiConstants {

    // ==========================================
    // BASE URLS  "https://78.187.40.116:8443/"
    // ==========================================
    const val BASE_URL = "https://172.20.10.3:8080/"
    const val API_BASE = "api/"
    const val AUTH_BASE = "${API_BASE}auth/"
    const val USER_BASE = "${API_BASE}user/"
    const val USER_MAIN_BASE = "${USER_BASE}main/"

    // ==========================================
    // AUTHENTICATION ENDPOINTS
    // ==========================================
    const val AUTH_LOGIN = "${AUTH_BASE}login"
    const val AUTH_SEND_RESET_EMAIL = "${AUTH_BASE}sendpasswordresetemail"
    const val AUTH_RESET_PASSWORD = "${AUTH_BASE}resetpassword"
    const val AUTH_CHANGE_PASSWORD = "${AUTH_BASE}change-password"

    // ==========================================
    // LOCATION CASCADE ENDPOINTS (4-LEVEL)
    // ==========================================
    const val SETTINGS_BASE = "${USER_BASE}settings/"

    // Hierarchical location endpoints
    const val SETTINGS_BOLGE_ALL = "${SETTINGS_BASE}bolge/aktif"             // ✅ Step 1: All regions
    const val SETTINGS_IL_BY_PARENT = "${SETTINGS_BASE}il/by-bolge/"      // ✅ Step 2: Provinces by region
    const val SETTINGS_ILCE_BY_PARENT = "${SETTINGS_BASE}ilce/by-il/"  // ✅ Step 3: Districts by province
    const val SETTINGS_DEPO_BY_PARENT = "${SETTINGS_BASE}depo/by-ilce/"  // ✅ Step 4: Warehouses by district

    // Other dropdown data endpoints
    const val SETTINGS_STOK_BIRIMI_ALL = "${SETTINGS_BASE}stok-birimi/aktif" // ✅ Product form için

    // ==========================================
    // SESSION MANAGEMENT ENDPOINTS
    // ==========================================
    const val SESSION_BASE = "${USER_BASE}session/"
    const val SESSION_SET_LOCATION = "${SESSION_BASE}set-location"          // ✅ Location save
    const val SESSION_CURRENT = "${SESSION_BASE}current"                    // ✅ Get current session
    const val SESSION_PROFILE = "${SESSION_BASE}profile"                    // ✅ Get user profile
    const val SESSION_CLEAR_LOCATION = "${SESSION_BASE}clear-location"      // ✅ Clear location
    const val SESSION_VALIDATE_LOCATION = "${SESSION_BASE}validate-location" // ✅ Validate location

    // ==========================================
    // RAF (SHELF) ENDPOINTS - ✅ GERÇEK BACKEND ENDPOINTS
    // ==========================================
    const val RAF_BASE = "${USER_MAIN_BASE}raf/"

    // Terminal RAF etiket üretimi (HTML response döner)
    const val RAF_GENERATE_LABELS = "${RAF_BASE}generate-labels"           // ✅ POST - Query params: adet, boyut


    // ==========================================
    // ÜRÜN (PRODUCT) ENDPOINTS
    // ==========================================
    const val URUN_BASE = "${USER_MAIN_BASE}urun/"

    // Terminal ÜRÜN etiket üretimi (HTML response döner)
    const val URUN_GENERATE_LABELS = "${URUN_BASE}generate-labels"        // ✅ POST - Query params: adet

    // RAF Mobile İşlemleri
    const val RAF_CREATE_MOBILE = "${RAF_BASE}create-mobile"                // POST - Mobile RAF oluşturma


    // HTTP Status Codes
    const val STATUS_UNAUTHORIZED = 401
    const val STATUS_FORBIDDEN = 403
    const val STATUS_NOT_FOUND = 404
    const val STATUS_INTERNAL_ERROR = 500

    // HTTP Status Codes - Alternative names (for compatibility)
    const val HTTP_UNAUTHORIZED = STATUS_UNAUTHORIZED
    const val HTTP_FORBIDDEN = STATUS_FORBIDDEN
    const val HTTP_NOT_FOUND = STATUS_NOT_FOUND
    const val HTTP_INTERNAL_SERVER_ERROR = STATUS_INTERNAL_ERROR
    // ==========================================
    // HEADERS
    // ==========================================
    const val HEADER_AUTHORIZATION = "Authorization"
    const val BEARER_PREFIX = "Bearer "

}