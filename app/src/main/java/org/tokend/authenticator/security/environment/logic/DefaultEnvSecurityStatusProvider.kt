package org.tokend.authenticator.security.environment.logic

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import org.tokend.authenticator.security.environment.model.EnvSecurityStatus

class DefaultEnvSecurityStatusProvider(
        context: Context
) : EnvSecurityStatusProvider {
    private val status: EnvSecurityStatus by lazy {
        val isRooted = RootBeer(context).let {
            it.detectRootManagementApps()
                    || it.detectPotentiallyDangerousApps()
                    || it.checkForBinary("su")
                    || it.checkForRWPaths()
                    || it.detectTestKeys()
                    || it.checkSuExists()
                    || it.checkForRootNative()
        }

        if (isRooted)
            EnvSecurityStatus.COMPROMISED
        else
            EnvSecurityStatus.NORMAL
    }

    override fun getEnvSecurityStatus(): EnvSecurityStatus {
        return status
    }
}