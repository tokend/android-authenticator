package org.tokend.authenticator.view

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.view.View
import android.view.animation.AnimationUtils
import org.tokend.authenticator.R
import org.tokend.authenticator.view.util.AnimationUtil
import org.tokend.authenticator.view.util.ToastManager

class FingerprintIndicatorManager(private val context: Context,
                                  private val fingerprintIndicator: AppCompatImageView) {

    private val animation = AnimationUtils.loadAnimation(context, R.anim.shake)

    init {
        fingerprintIndicator.setOnClickListener {
            ToastManager(context).short(R.string.touch_sensor)
        }
    }

    fun hide() {
        fingerprintIndicator.visibility = View.GONE
    }

    fun show() {
        AnimationUtil.fadeInView(fingerprintIndicator)
    }

    fun error() {
        fingerprintIndicator.clearAnimation()
        fingerprintIndicator.startAnimation(animation)
    }
}