package org.tokend.authenticator.security.userkey.punishment

import android.content.SharedPreferences
import android.os.SystemClock
import org.tokend.authenticator.security.userkey.punishment.view.PunishmentTimerView
import org.tokend.authenticator.security.userkey.view.UserKeyActivity

class PunishmentTimer(private val sharedPreferences: SharedPreferences) {

    private var lastPunished = sharedPreferences.getLong(KEY_PUNISH_SAVE_TIME, 0L)
    private var punishTimestamp: Long = sharedPreferences.getLong(KEY_PUNISH_TIME, 0L)
    private var lastFactor = sharedPreferences.getInt(KEY_PUNISH_FACTOR, 0)

    fun punishFor(attempt: Int) {
        if (attempt >= MAX_FAILED_USER_KEY_ATTEMPTS || lastFactor > 0) {
            lastFactor ++
            lastPunished = SystemClock.elapsedRealtime()
            val currentPunish = when(lastFactor >= 18) {
                true -> MAX_PUNISH_TIME
                else -> PUNISH_TIME * lastFactor
            }
            punishTimestamp = currentPunish + lastPunished
            save()
        }
    }

    fun isExpired(): Boolean {
        val current = SystemClock.elapsedRealtime()
        return current < lastPunished || current > punishTimestamp
    }

    fun timeLeft(): Int {
        val millis = punishTimestamp - SystemClock.elapsedRealtime()
        return ((millis + 999) / 1000).toInt()
    }

    fun reset() {
        lastFactor = 0
        punishTimestamp = 0L
        lastPunished = 0L
        save()
    }

    private fun save() {
        sharedPreferences.edit().apply {
            putLong(KEY_PUNISH_SAVE_TIME, lastPunished)
            putLong(KEY_PUNISH_TIME, punishTimestamp)
            putInt(KEY_PUNISH_FACTOR, lastFactor)
        }.apply()
    }

    fun viewFor(activity: UserKeyActivity) = PunishmentTimerView(activity, this)

    companion object {
        private const val PUNISH_TIME: Long = 5000
        private const val MAX_PUNISH_TIME: Long = 90000
        private const val MAX_FAILED_USER_KEY_ATTEMPTS = 3
        private const val KEY_PUNISH_SAVE_TIME = "key_punish_save_time"
        private const val KEY_PUNISH_TIME = "key_punish_time"
        private const val KEY_PUNISH_FACTOR = "key_punish_factor"
    }
}