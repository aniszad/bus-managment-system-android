package com.azcode.busmanagmentsystem.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.azcode.busmanagmentsystem.utils.Constants


open class SecuredPreferencesManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securedSharedPreferences = EncryptedSharedPreferences(
        context = context,
        fileName = "secured_preferences",
        masterKey = masterKey,
        prefKeyEncryptionScheme = EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        prefValueEncryptionScheme = EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAccessToken(accessToken: String) : Boolean{
        return securedSharedPreferences.edit()
            .putString(Constants.PREF_ACCESS_TOKEN, accessToken)
            .commit()
    }

    fun saveRefreshToken(refreshToken: String) : Boolean{
        return securedSharedPreferences.edit()
            .putString(Constants.PREF_REFRESH_TOKEN, refreshToken)
            .commit()
    }

    fun getAccessToken() : String? {
        val accessToken =  securedSharedPreferences.getString(Constants.PREF_ACCESS_TOKEN, "")
        return if (accessToken.isNullOrBlank()){
            null
        }else{
            accessToken
        }
    }

    fun getRefreshToken() : String?{
        val refreshToken = securedSharedPreferences.getString(Constants.PREF_REFRESH_TOKEN, "")
        return if (refreshToken.isNullOrBlank()){
            null
        }else{
            refreshToken
        }
    }

    fun clearTokens() {
        securedSharedPreferences.edit()
            .remove(Constants.PREF_ACCESS_TOKEN)
            .remove(Constants.PREF_REFRESH_TOKEN)
            .apply()
    }

}