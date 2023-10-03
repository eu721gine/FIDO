package com.example.asm;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

public class ASM_generateKeyPair extends AppCompatActivity {
    KeyStore keyStore;
    PublicKey publicKey;
    private static final String TAG = "ASM_generateKeyPair";

    public ASM_generateKeyPair() {
    }
    public void generateKeyPair(String userID) {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (!keyStore.containsAlias(userID)) {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(userID, KeyProperties.PURPOSE_SIGN)
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                        .build());

                KeyPair keyPair = keyPairGenerator.generateKeyPair();
                publicKey = keyPair.getPublic();

                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(userID, null);
                PrivateKey privateKey = privateKeyEntry.getPrivateKey();

                // 공개키를 서버로 전송
                Log.d(TAG, "공개키: " + Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP));
            } else {
                // 키 쌍이 이미 존재함
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(userID, null);
                publicKey = privateKeyEntry.getCertificate().getPublicKey();
                // 공개 키와 개인 키 출력
                Log.d(TAG, "Public Key: " + Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP));
                Toast.makeText(getApplicationContext(), "이미 저장된 생체정보입니다. ", Toast.LENGTH_SHORT).show();
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                 InvalidAlgorithmParameterException | KeyStoreException | CertificateException |
                 IOException | UnrecoverableEntryException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "키 생성 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}