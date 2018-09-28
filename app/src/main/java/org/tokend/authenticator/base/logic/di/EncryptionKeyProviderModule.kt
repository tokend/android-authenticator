package org.tokend.authenticator.base.logic.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.logic.encryption.TmpEncryptionKeyProvider

@Module
class EncryptionKeyProviderModule {
    @Provides
    fun encryptionKeyProvider(): EncryptionKeyProvider {
        return TmpEncryptionKeyProvider()
    }
}