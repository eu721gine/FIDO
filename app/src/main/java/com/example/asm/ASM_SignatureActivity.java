
package com.example.asm;

import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

public class ASM_SignatureActivity extends AppCompatActivity {
    private static final String TAG = "SignatureActivity";
    private KeyStore keyStore;
    private PrivateKey privateKey;

    public byte[] signChallenge(String challenge, String userID) {

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            boolean hasAccess = checkPrivateKeyAccess(userID);
            if (hasAccess) {
                // 개인 키에 액세스할 수 있는 권한이 있음
                Log.d(TAG, "개인 키에 액세스할 수 있는 권한이 있음");
            } else {
                // 개인 키에 액세스할 수 있는 권한이 없음
                Log.d(TAG, "개인 키에 액세스할 수 있는 권한이 없음");
            }

            if (keyStore.containsAlias(userID)) {
                Log.d(TAG, "키스토어 저장됨");
            } else {
                // 키 쌍이 존재하지 않음
                Log.d(TAG, "Key pair not found");
            }

            KeyStore.PrivateKeyEntry privateKeyEntry = null;
            try {
                privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(userID, null);
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (UnrecoverableEntryException e) {
                throw new RuntimeException(e);
            }
            privateKey = privateKeyEntry.getPrivateKey();

            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = messageDigest.digest(challenge.getBytes());

            Signature signature = Signature.getInstance("SHA256withRSA"); // SHA256withRSA 알고리즘 사용
            signature.initSign(privateKey);
            signature.update(hashBytes);
            byte[] encodedSignature = signature.sign();

            String signedChallenge = Base64.encodeToString(encodedSignature, Base64.NO_WRAP);

            return encodedSignature;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            Log.d(TAG, "Exception occurred: " + e.getMessage(), e); // Log the exception
            return null;
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkPrivateKeyAccess(String keyAlias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            return keyStore.entryInstanceOf(keyAlias, KeyStore.PrivateKeyEntry.class);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

