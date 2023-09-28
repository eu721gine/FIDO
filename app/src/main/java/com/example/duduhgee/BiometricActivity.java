package com.example.duduhgee;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.asm.ASM_SignatureActivity;
import com.example.asm.ASM_checkKeyPairExistence;
import com.example.rp.RP_DeleteAccountRequest;
import com.example.rp.RP_DeleteRequest;
import com.example.rp.RP_FIDORegisterRequest;
import com.example.rp.RP_PayDetailActivity;
import com.example.rp.RP_PaymentDetailRequest;
import com.example.rp.RP_SavePKRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

public class BiometricActivity extends AppCompatActivity {

    private CancellationSignal cancellationSignal = null;
    private BiometricPrompt.AuthenticationCallback authenticationCallback;
    private Button btn_auth;
    private Button btn_del;
    private Button btn_card;
    private Button btn_delAc;

    private TextView tv_product1, tv_amount1, tv_unitprice1, tv_totalprice1;
    private TextView tv_product2, tv_amount2, tv_unitprice2, tv_totalprice2;

    private TextView tv_id;
    private Button btn_info;
    private Button btn_home;
    private boolean start_authenticationIsClicked = false;
    private boolean delete_bioIsClicked = false;

    private boolean delete_AcIsClicked = false;

    private KeyStore keyStore;
    private PublicKey publicKey;
    private static final String TAG = "BiometricActivity";

    public String challenge;

    @SuppressLint("MissingInflatedId")
    @TargetApi(Build.VERSION_CODES.P)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);

        TextView tv_id = findViewById(R.id.tv_id);

        Intent intent = getIntent();
        String userId = intent.getStringExtra("userID");

        tv_id.setText(userId);

        btn_auth = findViewById(R.id.start_authentication);
        btn_del  = findViewById(R.id.delete_bio);
        btn_card = findViewById(R.id.btn_card);
        btn_delAc = findViewById(R.id.delete);
        btn_home = findViewById(R.id.btn_home);
        btn_info = findViewById(R.id.btn_info);

        TextView textPay = findViewById(R.id.text_pay);
        textPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");
                // PayDetail 액티비티로 이동하는 인텐트 생성
                intent = new Intent(BiometricActivity.this, RP_PayDetailActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });

        TextView textMyInfo = findViewById(R.id.text_myinfo);
        textMyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");
                // MyInfo 액티비티로 이동하는 인텐트 생성
                intent = new Intent(BiometricActivity.this, BiometricActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });

        authenticationCallback = new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                notifyUser("Authentication Failed");
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                notifyUser("Authentication Error: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");
                super.onAuthenticationSucceeded(result);
                notifyUser("인증에 성공하였습니다");

                if (start_authenticationIsClicked) {
                    ASM_checkKeyPairExistence checkkp = new ASM_checkKeyPairExistence();
                    boolean iskeyEX = checkkp.ASM_checkkeypairexistence(userID);

                    ASM_SignatureActivity signatureActivity = new ASM_SignatureActivity();
                    byte[] signedChallenge = signatureActivity.signChallenge(challenge, userID);

                    if (signedChallenge != null) {
                        // Method invocation was successful
                        Log.d(TAG, "Signed Challenge: " + Base64.encodeToString(signedChallenge, Base64.NO_WRAP));
                    } else {
                        // Method invocation failed
                        Log.e(TAG, "Failed to sign the challenge");
                    }
                    try {
                        keyStore = KeyStore.getInstance("AndroidKeyStore");
                        keyStore.load(null);
                        KeyStore.PrivateKeyEntry privateKeyEntry = null;
                        privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(userID, null);
                        publicKey = privateKeyEntry.getCertificate().getPublicKey();
                    } catch (KeyStoreException | CertificateException | IOException |
                             NoSuchAlgorithmException | UnrecoverableEntryException e) {
                        throw new RuntimeException(e);
                    }
                    if(iskeyEX){
                        try {
                            RP_sendpublickeytoserver(publicKey, signedChallenge, userID);
                        } catch (CertificateException | IOException | KeyStoreException |
                                 NoSuchAlgorithmException | KeyManagementException e) {
                            throw new RuntimeException(e);
                        }
                    }

                } else if (delete_bioIsClicked) {
                    try {
                        deletebio();
                    } catch (KeyStoreException | CertificateException | IOException |
                             NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                } else if (delete_AcIsClicked) {
                    try {
                        deleteAc();
                    } catch (KeyStoreException | CertificateException | IOException |
                             NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }
                else{
                }
            }
        };


        btn_auth.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.P)
            @Override
            public void onClick(View view) {

                start_authenticationIsClicked = true;
                delete_bioIsClicked = false;
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String username = jsonObject.getString("Username");
                            challenge = jsonObject.getString("Challenge");
                            String policy = jsonObject.getString("Policy");

                            Log.d(TAG,"Username: "+username);
                            Log.d(TAG,"Challenge: "+challenge);
                            Log.d(TAG,"Policy: "+policy);


                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "오류가 발생하였습니다. ", Toast.LENGTH_SHORT).show();
                            throw new RuntimeException(e);
                        }
                    }
                };
                RP_FIDORegisterRequest fidoRegisterRequest = null;
                try {
                    Intent intent = getIntent();
                    String userID = intent.getStringExtra("userID");
                    fidoRegisterRequest = new RP_FIDORegisterRequest(userID, responseListener, BiometricActivity.this);
                } catch (CertificateException | IOException | KeyStoreException |
                         NoSuchAlgorithmException | KeyManagementException e) {
                    throw new RuntimeException(e);
                }
                RequestQueue queue = Volley.newRequestQueue(BiometricActivity.this);
                queue.add(fidoRegisterRequest);

                if (checkBiometricSupport()) {
                    BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(BiometricActivity.this)
                            .setTitle("지문 인증을 시작합니다")
                            .setSubtitle("지문 인증 시작")
                            .setDescription("지문")
                            .setNegativeButton("Cancel", getMainExecutor(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    notifyUser("Authentication Cancelled");
                                }
                            }).build();

                    biometricPrompt.authenticate(getCancellationSignal(), getMainExecutor(), authenticationCallback);
                }
            }
        });


        btn_del.setOnClickListener(new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.P)

            @Override
            public void onClick(View v) {

                delete_bioIsClicked = true;
                start_authenticationIsClicked = false;

                if (checkBiometricSupport()) {
                    BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(BiometricActivity.this)
                            .setTitle("지문 인증을 시작합니다")
                            .setSubtitle("지문 인증 시작")
                            .setDescription("지문")
                            .setNegativeButton("Cancel", getMainExecutor(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    notifyUser("Authentication Cancelled");
                                }
                            }).build();

                    biometricPrompt.authenticate(getCancellationSignal(), getMainExecutor(), authenticationCallback);

                }
            }
        });

        btn_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");

                intent = new Intent(BiometricActivity.this, CardEnrollActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });

        btn_delAc.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.P)
            @Override
            public void onClick(View v) {

                delete_AcIsClicked = true;
                start_authenticationIsClicked = false;

                if (checkBiometricSupport()) {
                    BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(BiometricActivity.this)
                            .setTitle("지문 인증을 시작합니다")
                            .setSubtitle("지문 인증 시작")
                            .setDescription("지문")
                            .setNegativeButton("Cancel", getMainExecutor(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    notifyUser("Authentication Cancelled");
                                }
                            }).build();

                    biometricPrompt.authenticate(getCancellationSignal(), getMainExecutor(), authenticationCallback);

                }
            }
        });

        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");
                // 메인 액티비티로 이동하는 인텐트 생성
                intent = new Intent(BiometricActivity.this, MainActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
                finish(); // 현재 액티비티를 종료하여 이전 액티비티로 돌아갈 수 있도록 함
            }
        });

        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");

                intent = new Intent(BiometricActivity.this, BiometricActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });


    }


    public void RP_sendpublickeytoserver(PublicKey publicKey, byte[] signedChallenge, String userID) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        String publicKeyString = Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    if (success) {
                        Log.d(TAG, "공개키 전송 성공");
                    } else {
                        Log.d(TAG, "공개키 전송 실패");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "공개키 전송 에러: " + error.getMessage());
            }
        };

        RP_SavePKRequest savePKRequest = new RP_SavePKRequest(publicKeyString, Base64.encodeToString(signedChallenge, Base64.NO_WRAP) ,userID, responseListener, errorListener, BiometricActivity.this);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(savePKRequest);
    }

    private CancellationSignal getCancellationSignal() {
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(() -> notifyUser("Authentication was Cancelled by the user"));
        return cancellationSignal;
    }

    @TargetApi(Build.VERSION_CODES.P)
    public Boolean checkBiometricSupport() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isDeviceSecure()) {
            notifyUser("Fingerprint authentication has not been enabled in settings");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            notifyUser("Fingerprint Authentication Permission is not enabled");
            return false;
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            return true;
        } else {
            return false;
        }
    }

    private void notifyUser(String message) {
        Toast.makeText(BiometricActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void deletebio() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");

        if (keyStore.containsAlias(userID)){
            Log.d(TAG, "키스토어에 있습니다.");
            keyStore.deleteEntry(userID);
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");

                        if (success) {
                            Toast.makeText(getApplicationContext(), "생체정보가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "삭제 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            RP_DeleteRequest deleteRequest;
            try {
                deleteRequest = new RP_DeleteRequest(userID, responseListener, BiometricActivity.this);
            } catch (CertificateException | IOException | KeyStoreException |
                     NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
            RequestQueue queue = Volley.newRequestQueue(BiometricActivity.this);
            queue.add(deleteRequest);
            if (keyStore.containsAlias(userID)){
                Log.d(TAG, "키스토어에서 삭제 실패");
            } else {
                Log.d(TAG, "키스토어에서 삭제됨");
            }
        } else {
            Log.d(TAG, "키스토어에 없습니다. ");
        }
    }
    private void deleteAc() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");

        if (keyStore.containsAlias(userID)) {
            Log.d(TAG, "키스토어에 있습니다.");
            keyStore.deleteEntry(userID);
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");

                        if (success) {
                            Toast.makeText(getApplicationContext(), "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show();

                            // 회원 탈퇴 성공 시 로그인 페이지로 이동
                            Intent loginIntent = new Intent(BiometricActivity.this, LoginActivity.class);
                            startActivity(loginIntent);
                            finish();  // 현재 화면 종료
                        } else {
                            Toast.makeText(getApplicationContext(), "탈퇴 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            Response.ErrorListener errorListenerDeleteAc = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "계정 삭제 에러: " + error.getMessage());
                }
            };

            // 여기서 삭제 요청을 서버에 전달하는 코드를 추가하면 됩니다.
            RP_DeleteAccountRequest.deleteAccount(userID, BiometricActivity.this, responseListener, errorListenerDeleteAc);

            if (keyStore.containsAlias(userID)) {
                Log.d(TAG, "키스토어에서 삭제 실패");
            } else {
                Log.d(TAG, "키스토어에서 삭제됨");
            }
        } else {
            Log.d(TAG, "키스토어에 없습니다. ");
        }
    }

}