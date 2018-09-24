package org.tokend.authenticator.base.logic.encryption

import io.reactivex.Single
import org.tokend.sdk.keyserver.models.KdfAttributes

interface EncryptionKeyProvider {
    fun getKey(kdfAttributes: KdfAttributes): Single<ByteArray>
}