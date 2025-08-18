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
    // BASE URLS
    // ==========================================
    const val BASE_URL = "https://78.187.40.116:8443/"
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
    const val URUN_QUICK_LABELS = "${URUN_BASE}quick-labels"              // ✅ POST - Query params: adet (hızlı üretim)
    const val URUN_BULK_LABELS = "${URUN_BASE}bulk-labels"                // ✅ POST



    // ==========================================
    // MOBILE OPERATIONS ENDPOINTS
    // ==========================================
    const val MOBILE_BASE = "${USER_MAIN_BASE}mobile/"

    // Ürün işlemleri (Future use)
    const val MOBILE_URUN_CREATE = "${MOBILE_BASE}urun/create"              // ✅ Future: Product creation
    const val MOBILE_URUN_BY_RAF_SERI = "${MOBILE_BASE}urun/by-raf-seri/"   // ✅ Future: Product by shelf

    // File upload endpoints
    const val MOBILE_FILE_BASE = "${MOBILE_BASE}file/"
    const val MOBILE_FILE_URUN_UPLOAD = "${MOBILE_FILE_BASE}urun/"          // ✅ Future: Photo upload

    // RAF Mobile İşlemleri
    const val RAF_CREATE_MOBILE = "${RAF_BASE}create-mobile"                // POST - Mobile RAF oluşturma

    // Ürün Preview İşlemleri
    const val URUN_PREVIEW_NEXT = "${URUN_BASE}preview-next"               // GET - Sonraki ürün no önizleme

    // Mobile Fotoğraf İşlemleri
    const val MOBILE_PHOTO_BASE = "${MOBILE_BASE}photo/"
    const val MOBILE_PHOTO_TEMP_UPLOAD = "${MOBILE_PHOTO_BASE}temp-upload"  // POST - Geçici fotoğraf yükleme
    const val MOBILE_PHOTO_TEMP_DELETE = "${MOBILE_PHOTO_BASE}temp/"        // DELETE - Geçici fotoğraf silme /{photoId}
    const val MOBILE_PHOTO_TEMP_LIST = "${MOBILE_PHOTO_BASE}temp-list"      // GET - Geçici fotoğrafları listele

    // Mobile Ana Kayıt
    const val MOBILE_COMPLETE_REGISTRATION = "${MOBILE_BASE}complete-registration" // POST - Toplu kayıt işlemi

    // Dropdown Data - Aktif Listeler (Mevcut SETTINGS_STOK_BIRIMI_ALL yerine)
    const val SETTINGS_STOK_BIRIMI_AKTIF = "${SETTINGS_BASE}stok-birimi/aktif"

    // ==========================================
    // BARCODE GENERATION ENDPOINTS - PRODUCT LABELS
    // ==========================================
    const val BARCODE_BASE = "${USER_MAIN_BASE}barkod/"
    const val BARCODE_GENERATE = "${BARCODE_BASE}generate"                  // ✅ Ürün barcode üretimi
    const val BARCODE_BOYUTLAR = "${BARCODE_BASE}boyutlar"                  // ✅ Etiket boyut listesi

    // HTTP Status Codes
    const val STATUS_OK = 200
    const val STATUS_CREATED = 201
    const val STATUS_BAD_REQUEST = 400
    const val STATUS_UNAUTHORIZED = 401
    const val STATUS_FORBIDDEN = 403
    const val STATUS_NOT_FOUND = 404
    const val STATUS_INTERNAL_ERROR = 500

    // HTTP Status Codes - Alternative names (for compatibility)
    const val HTTP_OK = STATUS_OK
    const val HTTP_CREATED = STATUS_CREATED
    const val HTTP_BAD_REQUEST = STATUS_BAD_REQUEST
    const val HTTP_UNAUTHORIZED = STATUS_UNAUTHORIZED
    const val HTTP_FORBIDDEN = STATUS_FORBIDDEN
    const val HTTP_NOT_FOUND = STATUS_NOT_FOUND
    const val HTTP_INTERNAL_SERVER_ERROR = STATUS_INTERNAL_ERROR
    // ==========================================
    // HEADERS
    // ==========================================
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT = "Accept"
    const val BEARER_PREFIX = "Bearer "
    const val CONTENT_TYPE_JSON = "application/json"

    // ==========================================
    // TIMEOUT VALUES (in seconds)
    // ==========================================
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // ==========================================
    // PAGINATION
    // ==========================================
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
}