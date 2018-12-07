package org.tokend.authenticator.security.logic

import android.content.Context
import com.scottyab.rootbeer.RootBeer

class DefaultEnvSecurityStatusProvider(
        context: Context
) : EnvSecurityStatusProvider {
    private val status: EnvSecurityStatus by lazy {
        val isRooted = RootBeer(context).isRootedWithoutBusyBoxCheck

        if (isRooted)
            EnvSecurityStatus.COMPROMISED
        else
            EnvSecurityStatus.NORMAL
    }

    override fun getEnvSecurityStatus(): EnvSecurityStatus {
        return status
    }
}