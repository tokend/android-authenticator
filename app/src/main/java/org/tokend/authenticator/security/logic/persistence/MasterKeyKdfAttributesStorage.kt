package org.tokend.authenticator.security.logic.persistence

import android.content.SharedPreferences
import org.tokend.authenticator.base.logic.encryption.KdfAttributesGenerator
import org.tokend.sdk.factory.GsonFactory
import org.tokend.sdk.keyserver.models.KdfAttributes

class MasterKeyKdfAttributesStorage(
        private val sharedPreferences: SharedPreferences
) {
    fun load(): KdfAttributes {
        return sharedPreferences
                .getString(KEY, "")
                .takeIf { it.isNotEmpty() }
                ?.let { kdfAttributesJson ->
                    GsonFactory().getBaseGson()
                            .fromJson(kdfAttributesJson, KdfAttributes::class.java)
                }
                ?: KdfAttributesGenerator().withRandomSalt().also { save(it) }
    }

    fun save(kdfAttributes: KdfAttributes) {
        sharedPreferences
                .edit()
                .putString(
                        KEY,
                        GsonFactory().getBaseGson().toJson(kdfAttributes)
                )
                .apply()
    }

    companion object {
        private const val KEY = "master_key_kdf"
    }
}