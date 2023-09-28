package com.example.duduhgee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.rp.RP_CardEnrollRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CardEnrollActivity extends AppCompatActivity {
    private EditText et_card, et_cvc, et_date, et_pw;
    private Button btn_enroll;
    private Button btn_info;
    private Button btn_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardenroll);

        et_card = findViewById(R.id.et_card);
        et_cvc = findViewById(R.id.et_cvc);
        et_date = findViewById(R.id.et_date);
        et_pw = findViewById(R.id.et_pw);

        btn_home = findViewById(R.id.btn_home);
        btn_info = findViewById(R.id.btn_info);
        btn_enroll = findViewById(R.id.cardenroll);
        btn_enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");

                // editText에 현재 입력된 값을 가져옴(get)
                String c_num = et_card.getText().toString();
                String c_cvc = et_cvc.getText().toString();
                String c_date = et_date.getText().toString();
                String c_pw = et_pw.getText().toString();

                // Function to check for special characters
                boolean containsSpecialCharacters = containsOnlyNumbers(c_num) || containsOnlyNumbers(c_cvc) || containsOnlyNumbers(c_date) || containsOnlyNumbers(c_pw);

                if (!containsSpecialCharacters) {
                    Toast.makeText(CardEnrollActivity.this, "숫자 이외에는 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (c_num.length() != 16 || c_cvc.length() != 3 || c_date.length() != 4 || c_pw.length() != 2) {
                    Toast.makeText(CardEnrollActivity.this, "각 항목의 길이가 맞지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }


                // AES 키 생성
                String encryptionKey = generateAESKey();

                // Encrypt the card information
                String encryptedCardNumber = encrypt(c_num, encryptionKey);
                String encryptedCVC = encrypt(c_cvc, encryptionKey);
                String encryptedDate = encrypt(c_date, encryptionKey);
                String encryptedPW = encrypt(c_pw, encryptionKey);

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success"); // 서버통신 잘 됐냐?
                            if (success) {
                                Toast.makeText(getApplicationContext(), "카드 등록에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CardEnrollActivity.this, BiometricActivity.class);
                                intent.putExtra("userID", userID);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "카드 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "카드 등록 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                            throw new RuntimeException(e);
                        }
                    }
                };

                // 서버로 Volley를 이용하여 요청
                RP_CardEnrollRequest rp_cardEnrollRequest = null;
                try {
                    rp_cardEnrollRequest = new RP_CardEnrollRequest(userID, encryptedCardNumber, encryptedCVC, encryptedDate, encryptedPW, responseListener, CardEnrollActivity.this);
                } catch (CertificateException | IOException | KeyStoreException |
                         NoSuchAlgorithmException | KeyManagementException e) {
                    throw new RuntimeException(e);
                }

                RequestQueue queue = Volley.newRequestQueue(CardEnrollActivity.this);
                queue.add(rp_cardEnrollRequest);
            }
        });

        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");

                intent = new Intent(CardEnrollActivity.this, BiometricActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });

        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");

                // 메인 액티비티로 이동하는 인텐트 생성
                intent = new Intent(CardEnrollActivity.this, MainActivity.class);
                intent.putExtra("userID",userID);
                startActivity(intent);
                finish(); // 현재 액티비티를 종료하여 이전 액티비티로 돌아갈 수 있도록 함
            }
        });
    }

    // Function to check for special characters
    public boolean containsOnlyNumbers(String input) {
        return input.matches("^[0-9]+$");
    }

    // Function to generate AES key
    private String generateAESKey() {
        try {
            // AES 키 생성
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); // 256 비트 길이의 AES 키 생성
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Function to encrypt using AES
    private String encrypt(String input, String key) {
        try {
            SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(input.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}