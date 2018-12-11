package org.tokend.authenticator.security.userkey.logic

import android.content.SharedPreferences
import org.tokend.authenticator.security.userkey.model.UserKeyType

class UserKeyTypeStorage(
        private val preferences: SharedPreferences
) {
    fun load(): UserKeyType? {
        return preferences
                .getString(KEY, "")
                .takeIf { it.isNotEmpty() }
                ?.let { UserKeyType.valueOf(it) }
    }

    fun save(type: UserKeyType) {
        preferences
                .edit()
                .putString(
                        KEY,
                        type.toString()
                )
                .apply()
    }

    private companion object {
        private const val KEY = "user_key_type"
    }
}