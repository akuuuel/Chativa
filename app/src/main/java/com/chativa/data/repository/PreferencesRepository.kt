package com.chativa.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.chativa.data.model.PrivacyOption
import com.chativa.data.model.PrivacySettings
import com.chativa.data.model.ThemeMode
import com.chativa.data.model.ThemeSettings
import com.chativa.data.remote.SupabaseConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chativa_preferences")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PRIMARY_COLOR = stringPreferencesKey("primary_color")
        val SECONDARY_COLOR = stringPreferencesKey("secondary_color")
        val CHAT_BUBBLE_COLOR = stringPreferencesKey("chat_bubble_color")
        val CHAT_WALLPAPER = stringPreferencesKey("chat_wallpaper")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")

        val PRIVACY_ONLINE = stringPreferencesKey("privacy_online")
        val PRIVACY_LAST_SEEN = stringPreferencesKey("privacy_last_seen")
        val PRIVACY_PROFILE_PIC = stringPreferencesKey("privacy_profile_pic")
        val READ_RECEIPTS = booleanPreferencesKey("read_receipts")

        val SUPABASE_URL = stringPreferencesKey("supabase_url")
        val SUPABASE_ANON_KEY = stringPreferencesKey("supabase_anon_key")
    }

    val themeSettingsFlow: Flow<ThemeSettings> = context.dataStore.data.map { pref ->
        ThemeSettings(
            themeMode = try {
                ThemeMode.valueOf(pref[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
            } catch (_: Exception) { ThemeMode.SYSTEM },
            primaryColorHex = pref[Keys.PRIMARY_COLOR] ?: "#673AB7",
            secondaryColorHex = pref[Keys.SECONDARY_COLOR] ?: "#0288D1",
            chatBubbleColorHex = pref[Keys.CHAT_BUBBLE_COLOR] ?: "#E8EAF6",
            chatWallpaper = pref[Keys.CHAT_WALLPAPER] ?: "default",
            soundEnabled = pref[Keys.SOUND_ENABLED] ?: true,
            vibrationEnabled = pref[Keys.VIBRATION_ENABLED] ?: true
        )
    }

    val privacySettingsFlow: Flow<PrivacySettings> = context.dataStore.data.map { pref ->
        PrivacySettings(
            whoCanSeeOnline = try {
                PrivacyOption.valueOf(pref[Keys.PRIVACY_ONLINE] ?: PrivacyOption.EVERYONE.name)
            } catch (_: Exception) { PrivacyOption.EVERYONE },
            whoCanSeeLastSeen = try {
                PrivacyOption.valueOf(pref[Keys.PRIVACY_LAST_SEEN] ?: PrivacyOption.EVERYONE.name)
            } catch (_: Exception) { PrivacyOption.EVERYONE },
            whoCanSeeProfilePic = try {
                PrivacyOption.valueOf(pref[Keys.PRIVACY_PROFILE_PIC] ?: PrivacyOption.EVERYONE.name)
            } catch (_: Exception) { PrivacyOption.EVERYONE },
            showReadReceipts = pref[Keys.READ_RECEIPTS] ?: true
        )
    }

    val supabaseUrlFlow: Flow<String> = context.dataStore.data.map { pref ->
        pref[Keys.SUPABASE_URL] ?: SupabaseConfig.DEFAULT_SUPABASE_URL
    }

    val supabaseAnonKeyFlow: Flow<String> = context.dataStore.data.map { pref ->
        pref[Keys.SUPABASE_ANON_KEY] ?: SupabaseConfig.DEFAULT_SUPABASE_ANON_KEY
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { pref ->
            pref[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun updatePrimaryColor(hex: String) {
        context.dataStore.edit { pref ->
            pref[Keys.PRIMARY_COLOR] = hex
        }
    }

    suspend fun updateSecondaryColor(hex: String) {
        context.dataStore.edit { pref ->
            pref[Keys.SECONDARY_COLOR] = hex
        }
    }

    suspend fun updateChatBubbleColor(hex: String) {
        context.dataStore.edit { pref ->
            pref[Keys.CHAT_BUBBLE_COLOR] = hex
        }
    }

    suspend fun updateChatWallpaper(wallpaper: String) {
        context.dataStore.edit { pref ->
            pref[Keys.CHAT_WALLPAPER] = wallpaper
        }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { pref ->
            pref[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { pref ->
            pref[Keys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun updatePrivacyOnline(option: PrivacyOption) {
        context.dataStore.edit { pref ->
            pref[Keys.PRIVACY_ONLINE] = option.name
        }
    }

    suspend fun updatePrivacyLastSeen(option: PrivacyOption) {
        context.dataStore.edit { pref ->
            pref[Keys.PRIVACY_LAST_SEEN] = option.name
        }
    }

    suspend fun updatePrivacyProfilePic(option: PrivacyOption) {
        context.dataStore.edit { pref ->
            pref[Keys.PRIVACY_PROFILE_PIC] = option.name
        }
    }

    suspend fun updateReadReceipts(enabled: Boolean) {
        context.dataStore.edit { pref ->
            pref[Keys.READ_RECEIPTS] = enabled
        }
    }

    suspend fun updateSupabaseConfig(url: String, anonKey: String) {
        context.dataStore.edit { pref ->
            pref[Keys.SUPABASE_URL] = url.trim()
            pref[Keys.SUPABASE_ANON_KEY] = anonKey.trim()
        }
        SupabaseConfig.updateConfig(url, anonKey)
    }
}
