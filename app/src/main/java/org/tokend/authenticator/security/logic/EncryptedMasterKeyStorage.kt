package org.tokend.authenticator.security.logic

import android.content.SharedPreferences
import org.tokend.sdk.api.models.KeychainData
import org.tokend.sdk.factory.GsonFactory

class EncryptedMasterKeyStorage(
        private val sharedPreferences: SharedPreferences
) {
    fun hasData(): Boolean {
        return getJsonString() != null
    }

    private fun getJsonString(): String? {
        return sharedPreferences
                .getString(KEY, "")
                .takeIf { it.isNotEmpty() }
    }

    fun load(): KeychainData? {
        return getJsonString()
                ?.let {
                    KeychainData.fromJson(it)
                }
    }

    fun save(encryptedMasterKey: KeychainData) {
        sharedPreferences
                .edit()
                .putString(
                        KEY,
                        GsonFactory().getBaseGson().toJson(encryptedMasterKey)
                )
                .apply()
    }

    companion object {
        private const val KEY = "master_key"
    }
}