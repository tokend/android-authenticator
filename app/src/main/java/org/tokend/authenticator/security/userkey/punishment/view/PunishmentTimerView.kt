package org.tokend.authenticator.security.userkey.punishment.view

import android.view.View
import kotlinx.android.synthetic.main.include_punishment_timer_holder.*
import org.tokend.authenticator.R
import org.tokend.authenticator.security.userkey.punishment.PunishmentTimer
import org.tokend.authenticator.security.userkey.view.UserKeyActivity
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

class PunishmentTimerView(private val context: UserKeyActivity,
                          private val punishmentTimer: PunishmentTimer) {

    private var timerTask: TimerTask? = null

    fun showTimer(onTimerStart: () -> Unit,
                  onTimerExpired: () -> Unit?) {
        if (!punishmentTimer.isExpired()) {
            onTimerStart.invoke()
            context.timer_holder.visibility = View.VISIBLE
            val timerTemplate = context.getString(R.string.template_timer)

            var timeLeft = punishmentTimer.timeLeft()
            context.timer_text.text = timerTemplate.format(timeLeft.toString())

            timerTask = Timer().scheduleAtFixedRate(DELAY, DELAY) {
                context.runOnUiThread {
                    timeLeft--
                    context.timer_text.text = timerTemplate.format(timeLeft.toString())
                    if (timeLeft == 0) {
                        onTimerExpired.invoke()
                        cancel()
                    }
                }
            }
        } else {
            onTimerExpired.invoke()
        }
    }

    fun cancelTimer() {
        timerTask?.cancel()
    }

    companion object {
        private const val DELAY: Long = 1000
    }
}
