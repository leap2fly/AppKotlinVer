package com.example.ccagatedispatch.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_keys_store")

@OptIn(ExperimentalEncodingApi::class)
@Singleton
class KeyProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "cca_master_key_alias"
        private const val PASSPHRASE_KEY = "encrypted_db_passphrase"
        private const val IV_KEY = "encrypted_db_iv"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val PASSPHRASE_BYTE_SIZE = 32 // 256 bits
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private val passphrasePrefKey = stringPreferencesKey(PASSPHRASE_KEY)
    private val ivPrefKey = stringPreferencesKey(IV_KEY)

    fun getOrCreateDatabasePassphrase(): ByteArray = runBlocking {
        val prefs = context.dataStore.data.first()
        val encryptedPassphraseBase64 = prefs[passphrasePrefKey]
        val ivBase64 = prefs[ivPrefKey]

        if (encryptedPassphraseBase64 != null && ivBase64 != null) {
            decryptPassphrase(
                Base64.decode(encryptedPassphraseBase64),
                Base64.decode(ivBase64)
            )
        } else {
            generateAndSavePassphrase()
        }
    }

    private suspend fun generateAndSavePassphrase(): ByteArray {
        val rawPassphrase = ByteArray(PASSPHRASE_BYTE_SIZE)
        SecureRandom().nextBytes(rawPassphrase)

        val masterKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)

        val encryptedBytes = cipher.doFinal(rawPassphrase)
        val iv = cipher.iv

        context.dataStore.edit { preferences ->
            preferences[passphrasePrefKey] = Base64.encode(encryptedBytes)
            preferences[ivPrefKey] = Base64.encode(iv)
        }

        return rawPassphrase
    }

    private fun decryptPassphrase(encryptedPassphrase: ByteArray, iv: ByteArray): ByteArray {
        val masterKey = keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)
        return cipher.doFinal(encryptedPassphrase)
    }

    private fun getOrCreateMasterKey(): SecretKey {
        if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        }
        return keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
    }
}
