package com.example.stokkontrolveyonetimsistemi.printer

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * TSC Alpha-3R Bluetooth Printer Service
 * Terminal'den Bluetooth ile RAF etiketi yazdırma
 * 50mm x 30mm etiket için optimize edilmiş
 */
class TscAlpha3RPrinterService(private val context: Context) {

    companion object {
        private const val TAG = "TscAlpha3RBluetooth"

        // Bluetooth Serial Port Profile UUID
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        // TSC Printer tanımlayıcıları
        private val TSC_DEVICE_NAMES = listOf(
            "TSC",
            "Alpha-3R",
            "Alpha3R",
            "ALPHA",
            "TSC_Alpha",
            "BT-SPP",
            "SPP",
            "BTSPP"
        )
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter = bluetoothManager?.adapter

    /**
     * Bluetooth kullanılabilir mi kontrol et
     */
    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }

    /**
     * Bluetooth izinleri var mı kontrol et
     */
    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Eşleştirilmiş TSC veya SPP printer'ı bul
     */
    fun findPairedTscPrinter(): BluetoothDevice? {
        if (!hasBluetoothPermissions()) {
            Log.e(TAG, "Bluetooth izinleri yok")
            return null
        }

        return try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        Manifest.permission.BLUETOOTH_CONNECT
                    else
                        Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Bluetooth izni reddedildi")
                return null
            }

            val pairedDevices = bluetoothAdapter?.bondedDevices ?: return null

            Log.d(TAG, "Eşleştirilmiş cihazlar aranıyor: ${pairedDevices.size} cihaz")

            // Önce TSC isimli cihazları ara
            for (device in pairedDevices) {
                val deviceName = device.name?.uppercase() ?: ""
                Log.d(TAG, "Kontrol ediliyor: ${device.name} (${device.address})")

                // TSC printer mı kontrol et
                if (TSC_DEVICE_NAMES.any { deviceName.contains(it.uppercase()) }) {
                    Log.d(TAG, "Printer bulundu: ${device.name}")
                    return device
                }
            }

            // TSC bulunamazsa, ilk SPP cihazını kullan
            val firstSppDevice = pairedDevices.firstOrNull { device ->
                val name = device.name?.uppercase() ?: ""
                name.contains("SPP") || name.contains("PRINT")
            }

            if (firstSppDevice != null) {
                Log.d(TAG, "SPP cihaz bulundu: ${firstSppDevice.name}")
                return firstSppDevice
            }

            Log.w(TAG, "TSC printer bulunamadı")
            null

        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth güvenlik hatası: ${e.message}")
            null
        }
    }

    /**
     * Tüm eşleştirilmiş cihazları listele
     */
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!hasBluetoothPermissions()) {
            return emptyList()
        }

        return try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        Manifest.permission.BLUETOOTH_CONNECT
                    else
                        Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Bluetooth izni reddedildi")
                return emptyList()
            }

            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()

        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth güvenlik hatası: ${e.message}")
            emptyList()
        }
    }

    /**
     * Bluetooth cihaza bağlan
     */
    private suspend fun connectToDevice(device: BluetoothDevice): BluetoothSocket? = withContext(Dispatchers.IO) {
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        Manifest.permission.BLUETOOTH_CONNECT
                    else
                        Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Bluetooth bağlantı izni yok")
                return@withContext null
            }

            Log.d(TAG, "Bağlanılıyor: ${device.name} (${device.address})")

            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()

            Log.d(TAG, "Başarıyla bağlandı: ${device.name}")
            socket

        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth güvenlik hatası: ${e.message}")
            null
        } catch (e: IOException) {
            Log.e(TAG, "Bağlantı hatası: ${e.message}")
            null
        }
    }

    /**
     * Tek RAF etiketi yazdır (Bluetooth) - DEBUG DESTEKLİ
     */
    suspend fun printRafEtiketBluetooth(
        device: BluetoothDevice,
        rafNumber: String,
        currentIndex: Int,
        totalCount: Int,
        useBigText: Boolean = true  // Büyük metin kullan
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!hasBluetoothPermissions()) {
                Log.e(TAG, "Bluetooth izinleri eksik")
                return@withContext false
            }

            Log.d(TAG, "RAF etiketi yazdırılıyor: $rafNumber ($currentIndex/$totalCount)")

            val socket = connectToDevice(device) ?: return@withContext false

            socket.use { connection ->
                val outputStream = connection.outputStream

                // TSPL komutları oluştur - Büyük metin tercihine göre
                val tspl = if (useBigText) {
                    generateTsplForRafEtiketWithBigText(rafNumber, currentIndex, totalCount)
                } else {
                    generateTsplForRafEtiket(rafNumber, currentIndex, totalCount)
                }

                // DEBUG: Gönderilen komutları logla
                Log.d(TAG, "=== TSPL KOMUTLARI ===")
                tspl.lines().forEach { line ->
                    Log.d(TAG, line)
                }
                Log.d(TAG, "=== TSPL SON ===")

                // Printer'a gönder
                outputStream.write(tspl.toByteArray(Charsets.UTF_8))
                outputStream.flush()

                // Yazdırma için bekle
                Thread.sleep(1000)

                Log.d(TAG, "Etiket gönderildi: $rafNumber")
            }

            true

        } catch (e: Exception) {
            Log.e(TAG, "Yazdırma hatası: ${e.message}")
            false
        }
    }

    /**
     * RAF etiketi için TSPL komutları oluştur
     * TSC Alpha-3R için optimize edilmiş - 50mm x 30mm
     * FİNAL VERSİYON - Sadece barkod ve tarih
     */
    private fun generateTsplForRafEtiket(
        rafNumber: String,
        currentIndex: Int,
        totalCount: Int
    ): String {
        val currentTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date())

        return buildString {
            // Printer başlangıç ayarları
            appendLine("SIZE 50 mm, 30 mm")
            appendLine("GAP 3 mm, 0 mm")
            appendLine("SPEED 3")
            appendLine("DENSITY 8")
            appendLine("DIRECTION 0")
            appendLine("REFERENCE 0,0")
            appendLine("CLS")

            // SADECE BARKOD VE TARİH - BÜYÜK YAZI YOK!

            // Barkod - Üstte ve ortada (readable text ON yapıldı)
            // Son parametre 1 = barkod altında numara göster
            appendLine("BARCODE 80,120,\"128\",60,1,0,2,2,\"$rafNumber\"")

            // Tarih/Saat - Sağa kaydırıldı (X=180)
            appendLine("TEXT 180,30,\"2\",0,1,1,\"$currentTime\"")

            // Yazdır
            appendLine("PRINT 1,1")
        }
    }

    /**
     * ALTERNATIF: Barkod altı yazısı daha büyük - FİNAL VERSİYON
     */
    private fun generateTsplForRafEtiketWithBigText(
        rafNumber: String,
        currentIndex: Int,
        totalCount: Int
    ): String {
        val currentTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date())

        return buildString {
            appendLine("SIZE 50 mm, 30 mm")
            appendLine("GAP 3 mm, 0 mm")
            appendLine("SPEED 3")
            appendLine("DENSITY 8")
            appendLine("DIRECTION 0")
            appendLine("REFERENCE 0,0")
            appendLine("CLS")

            // RAF numarası - EN ÜSTTE
            appendLine("TEXT 90,160,\"2\",0,1,2,\"$rafNumber\"")

            // Barkod - ORTADA (numaranın altında)
            appendLine("BARCODE 80,90,\"128\",55,0,0,2,2,\"$rafNumber\"")

            // Tarih/Saat - EN ALTTA ve DAHA BÜYÜK
            appendLine("TEXT 140,20,\"2\",0,1,1,\"$currentTime\"")

            appendLine("PRINT 1,1")
        }
    }



    suspend fun printUrunEtiketBluetooth(
        device: BluetoothDevice,
        urunNumber: String,
        currentIndex: Int,
        totalCount: Int,
        useBigText: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!hasBluetoothPermissions()) {
                Log.e(TAG, "Bluetooth izinleri eksik")
                return@withContext false
            }

            Log.d(TAG, "ÜRÜN etiketi yazdırılıyor: $urunNumber ($currentIndex/$totalCount)")

            val socket = connectToDevice(device) ?: return@withContext false

            socket.use { connection ->
                val outputStream = connection.outputStream

                // TSPL komutları oluştur
                val tspl = if (useBigText) {
                    generateTsplForUrunEtiketWithBigText(urunNumber, currentIndex, totalCount)
                } else {
                    generateTsplForUrunEtiket(urunNumber, currentIndex, totalCount)
                }

                // DEBUG: Gönderilen komutları logla
                Log.d(TAG, "=== ÜRÜN TSPL KOMUTLARI ===")
                tspl.lines().forEach { line ->
                    Log.d(TAG, line)
                }
                Log.d(TAG, "=== TSPL SON ===")

                // Printer'a gönder
                outputStream.write(tspl.toByteArray(Charsets.UTF_8))
                outputStream.flush()

                // Yazdırma için bekle
                Thread.sleep(1000)

                Log.d(TAG, "Ürün etiketi gönderildi: $urunNumber")
            }

            true

        } catch (e: Exception) {
            Log.e(TAG, "Yazdırma hatası: ${e.message}")
            false
        }
    }

    /**
     * ÜRÜN etiketi için TSPL komutları oluştur
     * TSC Alpha-3R için optimize edilmiş - 50mm x 30mm
     * Sadece barkod ve tarih - RAF ile aynı format
     */
    private fun generateTsplForUrunEtiket(
        urunNumber: String,
        currentIndex: Int,
        totalCount: Int
    ): String {
        val currentTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date())

        return buildString {
            // Printer başlangıç ayarları
            appendLine("SIZE 50 mm, 30 mm")
            appendLine("GAP 3 mm, 0 mm")
            appendLine("SPEED 3")
            appendLine("DENSITY 8")
            appendLine("DIRECTION 0")
            appendLine("REFERENCE 0,0")
            appendLine("CLS")

            // Barkod - Üstte ve ortada (readable text ON)
            appendLine("BARCODE 80,120,\"128\",60,1,0,2,2,\"$urunNumber\"")

            // Tarih/Saat - Sağa kaydırıldı
            appendLine("TEXT 180,30,\"2\",0,1,1,\"$currentTime\"")

            // Yazdır
            appendLine("PRINT 1,1")
        }
    }

    /**
     * ALTERNATIF: ÜRÜN etiketi - Büyük yazı versiyonu
     */
    private fun generateTsplForUrunEtiketWithBigText(
        urunNumber: String,
        currentIndex: Int,
        totalCount: Int
    ): String {
        val currentTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date())

        return buildString {
            appendLine("SIZE 50 mm, 30 mm")
            appendLine("GAP 3 mm, 0 mm")
            appendLine("SPEED 3")
            appendLine("DENSITY 8")
            appendLine("DIRECTION 0")
            appendLine("REFERENCE 0,0")
            appendLine("CLS")

            // ÜRÜN numarası - EN ÜSTTE
            appendLine("TEXT 90,160,\"2\",0,1,2,\"$urunNumber\"")

            // Barkod - ORTADA (numaranın altında)
            appendLine("BARCODE 80,90,\"128\",55,0,0,2,2,\"$urunNumber\"")

            // Tarih/Saat - EN ALTTA
            appendLine("TEXT 140,20,\"2\",0,1,1,\"$currentTime\"")

            appendLine("PRINT 1,1")
        }
    }

    /**
     * Otomatik olarak TSC printer bul ve ÜRÜN etiketi yazdır
     */
    suspend fun printUrunEtiketAuto(
        urunNumber: String,
        currentIndex: Int,
        totalCount: Int
    ): Boolean {
        val tscPrinter = findPairedTscPrinter()
        if (tscPrinter == null) {
            Log.e(TAG, "TSC printer bulunamadı")
            return false
        }

        return printUrunEtiketBluetooth(tscPrinter, urunNumber, currentIndex, totalCount)
    }

    /**
     * Cihaz bağlantısını test et
     */
    suspend fun testConnection(device: BluetoothDevice): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!hasBluetoothPermissions()) {
                Log.e(TAG, "Bluetooth izinleri eksik")
                return@withContext false
            }

            Log.d(TAG, "Bağlantı testi: ${device.name}")

            val socket = connectToDevice(device) ?: return@withContext false

            socket.use { connection ->
                // Basit bir bağlantı testi - sadece bağlan ve kapat
                Log.d(TAG, "Bağlantı testi başarılı: ${device.name}")
            }

            return@withContext true

        } catch (e: Exception) {
            Log.e(TAG, "Bağlantı testi hatası: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Test etiketi yazdır
     */
    suspend fun printTestLabel(device: BluetoothDevice): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!hasBluetoothPermissions()) {
                Log.e(TAG, "Bluetooth izinleri eksik")
                return@withContext false
            }

            Log.d(TAG, "Test etiketi yazdırılıyor: ${device.name}")

            val socket = connectToDevice(device) ?: return@withContext false

            socket.use { connection ->
                val outputStream = connection.outputStream

                // Test etiketi TSPL komutları
                val testTspl = generateTestLabelTspl()

                // DEBUG: Test komutlarını logla
                Log.d(TAG, "=== TEST TSPL KOMUTLARI ===")
                testTspl.lines().forEach { line ->
                    Log.d(TAG, line)
                }
                Log.d(TAG, "=== TEST TSPL SON ===")

                // Printer'a gönder
                outputStream.write(testTspl.toByteArray(Charsets.UTF_8))
                outputStream.flush()

                // Yazdırma için bekle
                Thread.sleep(1000)

                Log.d(TAG, "Test etiketi gönderildi")
            }

            return@withContext true

        } catch (e: Exception) {
            Log.e(TAG, "Test etiketi yazdırma hatası: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Test etiketi TSPL komutları oluştur
     */
    private fun generateTestLabelTspl(): String {
        val currentTime = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            .format(Date())

        return buildString {
            // Printer başlangıç ayarları
            appendLine("SIZE 50 mm, 30 mm")
            appendLine("GAP 3 mm, 0 mm")
            appendLine("SPEED 3")
            appendLine("DENSITY 8")
            appendLine("DIRECTION 0")
            appendLine("REFERENCE 0,0")
            appendLine("CLS")

            // Test başlığı
            appendLine("TEXT 100,180,\"3\",0,1,1,\"TEST\"")

            // TSC Alpha-3R yazısı
            appendLine("TEXT 60,150,\"2\",0,1,1,\"TSC Alpha-3R\"")

            // Test barkodu
            appendLine("BARCODE 80,90,\"128\",50,0,0,2,2,\"TEST123\"")

            // Tarih/Saat
            appendLine("TEXT 50,40,\"1\",0,1,1,\"$currentTime\"")

            // Alt yazı
            appendLine("TEXT 80,20,\"1\",0,1,1,\"Baglanti Testi\"")

            // Yazdır
            appendLine("PRINT 1,1")
        }
    }

// Bu fonksiyonları TscAlpha3RPrinterService sınıfının sonuna, son fonksiyondan önce ekleyin
}