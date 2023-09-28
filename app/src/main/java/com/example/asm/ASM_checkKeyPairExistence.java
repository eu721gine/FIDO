package com.example.asm;

import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

public class ASM_checkKeyPairExistence extends AppCompatActivity {
    KeyStore keyStore;
    PublicKey publicKey;
    private static final String TAG = "ASM_checkKeyPairExistence";
    public ASM_checkKeyPairExistence(){
    }
    public boolean ASM_checkkeypairexistence(String userID) {

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (keyStore.containsAlias(userID)) {
                // 키 쌍이 존재함
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(userID, null);
                publicKey = privateKeyEntry.getCertificate().getPublicKey();
                // 공개 키와 개인 키 출력
                Log.d(TAG, "Public Key: " + Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP));
                //Toast.makeText(getApplicationContext(), "이미 저장된 생체정보입니다. ", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "이미 저장된 생체정보입니다.");
                return false;
            } else {
                // 키 쌍이 존재하지 않음
                Log.d(TAG, "Key pair not found");

                ASM_generateKeyPair generatekeypair = new ASM_generateKeyPair();
                generatekeypair.generateKeyPair(userID);
                return true;
                // 생성하고 바로 보내기

            }
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException |
                 UnrecoverableEntryException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "키 저장소 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}