package com.example.stokkontrolveyonetimsistemi.data.model.dashboard

/**
 * Updated Dashboard user actions
 * Yeni dashboard tasarımı için güncellenmiş aksiyonlar
 */
sealed class DashboardAction {
    // Data loading actions
    object RefreshData : DashboardAction()
    object LoadUserInfo : DashboardAction()
    object LoadStats : DashboardAction()
    object ValidateSession : DashboardAction()

    // UI state actions
    object ShowNotifications : DashboardAction()
    object HideNotifications : DashboardAction()
    object ShowUserMenu : DashboardAction()
    object HideUserMenu : DashboardAction()

    // Navigation actions
    data class NavigateToModule(val module: DashboardModule) : DashboardAction()

    // Business actions
    object GenerateRafNumarasi : DashboardAction()
    object GenerateUrunNumarasi : DashboardAction()
    object AddNewProduct : DashboardAction()
    object StartBarcodeScanning : DashboardAction()

    // Settings actions
    object ChangePassword : DashboardAction()
    object ChangeLocation : DashboardAction()

    // Error handling
    data class ShowError(val message: String) : DashboardAction()
    object ClearError : DashboardAction()

    // Authentication actions
    object Logout : DashboardAction()
}