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
import java.util.UUID

/**
 * TSC Alpha-3R Bluetooth Printer Service
 * Terminal'den Bluetooth ile RAF/Ürün etiketi yazdırma
 * 30mm x 15mm (3cm x 1.5cm) etiket için optimize edilmiş
 * Tarihsiz, sadece barkod içeren minimal tasarım
 * Barkod %30 büyütülmüş ve tam ortalanmış
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
     * RAF etiketi yazdır (Bluetooth) - 30mm x 15mm, TARİHSİZ
     */
    suspend fun printRafEtiketBluetooth(
        device: BluetoothDevice,
        rafNumber: String,
        currentIndex: Int,
        totalCount: Int
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

                // TSPL komutları oluştur - 30mm x 15mm
                val tspl = generateTsplForRafEtiket(rafNumber)

                // Printer'a gönder
                outputStream.write(tspl.toByteArray(Charsets.UTF_8))
                outputStream.flush()

                // Yazdırma için bekle
                Thread.sleep(800)

                Log.d(TAG, "Etiket gönderildi: $rafNumber")
            }

            true

        } catch (e: Exception) {
            Log.e(TAG, "Yazdırma hatası: ${e.message}")
            false
        }
    }

    /**
     * RAF etiketi için TSPL komutları - 30mm x 15mm, TARİHSİZ
     * Basitleştirilmiş ve optimize edilmiş versiyon
     */
    private fun generateTsplForRafEtiket(rafNumber: String): String {
        return buildString {
            // Printer başlangıç ayarları - 30mm x 15mm (3cm x 1.5cm)
            appendLine("SIZE 30 mm, 15 mm")
            appendLine("GAP 2 mm, 0 mm")
            appendLine("SPEED 3")
            appendLine("DENSITY 8")
            appendLine("DIRECTION 0")
            appendLine("REFERENCE 0,0")
            appendLine("CLS")

            // Basit yaklaşım: Sabit ve test edilmiş değerler
            // BARCODE komutunun basit kullanımı
            // X=40 (ortalamak için), Y=20 (yukarı)
            // height=60 (orta boy), narrow=2, wide=2 (normal kalınlık)
            appendLine("BARCODE 30,20,\"128M\",50,2,0,1,1,\"$rafNumber\"")


            // Yazdır
            appendLine("PRINT 1,1")
        }
    }

    /**
     * ÜRÜN etiketi yazdır (Bluetooth) - 30mm x 15mm, TARİHSİZ
     */
    suspend fun printUrunEtiketBluetooth(
        device: BluetoothDevice,
        urunNumber: String,
        currentIndex: Int,
        totalCount: Int
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

                // TSPL komutları oluştur - 30mm x 15mm
                val tspl = generateTsplForUrunEtiket(urunNumber)

                // Printer'a gönder
                outputStream.write(tspl.toByteArray(Charsets.UTF_8))
                outputStream.flush()

                // Yazdırma için bekle
                Thread.sleep(800)

                Log.d(TAG, "Ürün etiketi gönderildi: $urunNumber")
            }

            true

        } catch (e: Exception) {
            Log.e(TAG, "Yazdırma hatası: ${e.message}")
            false
        }
    }

    /**
     * ÜRÜN etiketi için TSPL komutları - 30mm x 15mm, TARİHSİZ
     * Basitleştirilmiş ve optimize edilmiş versiyon
     */
    private fun generateTsplForUrunEtiket(urunNumber: String): String {
        return buildString {
            // Printer başlangıç ayarları - 30mm x 15mm (3cm x 1.5cm)
            appendLine("SIZE 30 mm, 15 mm")
            appendLine("GAP 2 mm, 0 mm")
            appendLine("SPEED 3")
            appendLine("DENSITY 8")
            appendLine("DIRECTION 0")
            appendLine("REFERENCE 0,0")
            appendLine("CLS")

            // Basit yaklaşım: Sabit ve test edilmiş değerler
            // BARCODE komutunun basit kullanımı
            // X=40 (ortalamak için), Y=20 (yukarı)
            // height=60 (orta boy), narrow=2, wide=2 (normal kalınlık)
            appendLine("BARCODE 30,20,\"128M\",50,2,0,1,1,\"$urunNumber\"")

            // Yazdır
            appendLine("PRINT 1,1")
        }
    }
}