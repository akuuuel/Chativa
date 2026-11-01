package com.chativa.utils

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.chativa.data.remote.SupabaseConfig
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

data class FileUploadResult(
    val url: String,
    val fileName: String,
    val fileSizeFormatted: String
)

object MediaStorageHelper {

    private const val BUCKET_NAME = "chativa-media"

    suspend fun uploadImage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val fileName = "images/img_${System.currentTimeMillis()}_${(100..999).random()}.jpg"
            val bucket = SupabaseConfig.storage[BUCKET_NAME]

            bucket.upload(fileName, bytes) {
                upsert = true
            }
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            Log.e("CHATIVA_MEDIA", "Error uploading image to Supabase", e)
            null
        }
    }

    suspend fun uploadAudio(file: File): String? = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) return@withContext null
            val bytes = file.readBytes()
            val fileName = "audio/audio_${System.currentTimeMillis()}_${(100..999).random()}.m4a"
            val bucket = SupabaseConfig.storage[BUCKET_NAME]

            bucket.upload(fileName, bytes) {
                upsert = true
            }
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            Log.e("CHATIVA_MEDIA", "Error uploading audio to Supabase", e)
            null
        }
    }

    suspend fun uploadFile(context: Context, uri: Uri): FileUploadResult? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            var displayName = "Berkas_${System.currentTimeMillis()}"
            var sizeBytes: Long = 0

            try {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) {
                            cursor.getString(nameIndex)?.let { displayName = it }
                        }
                        if (sizeIndex != -1) {
                            sizeBytes = cursor.getLong(sizeIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("CHATIVA_MEDIA", "Could not query file metadata", e)
            }

            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val formattedSize = formatFileSize(if (sizeBytes > 0) sizeBytes else bytes.size.toLong())
            val cleanName = displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val storagePath = "files/${System.currentTimeMillis()}_$cleanName"
            val bucket = SupabaseConfig.storage[BUCKET_NAME]

            bucket.upload(storagePath, bytes) {
                upsert = true
            }
            val publicUrl = bucket.publicUrl(storagePath)

            FileUploadResult(
                url = publicUrl,
                fileName = displayName,
                fileSizeFormatted = formattedSize
            )
        } catch (e: Exception) {
            Log.e("CHATIVA_MEDIA", "Error uploading file to Supabase", e)
            null
        }
    }

    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", size / 1024.0)
            else -> String.format(Locale.getDefault(), "%.1f MB", size / (1024.0 * 1024.0))
        }
    }

    fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            var location: Location? = null
            try {
                if (isGpsEnabled) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                if (location == null && isNetworkEnabled) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
            } catch (_: SecurityException) {}

            if (location != null) {
                Pair(location.latitude, location.longitude)
            } else {
                Pair(-6.2088, 106.8456)
            }
        } catch (e: Exception) {
            Log.e("CHATIVA_LOCATION", "Error getting location", e)
            Pair(-6.2088, 106.8456)
        }
    }
}
