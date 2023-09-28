package com.example.rp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.duduhgee.BiometricActivity;
import com.example.duduhgee.MainActivity;
import com.example.duduhgee.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class RP_PayDetailActivity extends AppCompatActivity {

    private Button btn_info;
    private Button btn_home;
    private TextView tv_id;
    private TextView tv_product1, tv_amount1, tv_unitprice1, tv_totalprice1;
    private TextView tv_product2, tv_amount2, tv_unitprice2, tv_totalprice2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        btn_home = findViewById(R.id.btn_home);
        btn_info = findViewById(R.id.btn_info);

        tv_product1 = findViewById(R.id.tv_product1);
        tv_amount1 = findViewById(R.id.tv_amount1);
        tv_unitprice1 = findViewById(R.id.tv_unitprice1);
        tv_totalprice1 = findViewById(R.id.tv_totalprice1);
        tv_product2 = findViewById(R.id.tv_product2);
        tv_amount2 = findViewById(R.id.tv_amount2);
        tv_unitprice2 = findViewById(R.id.tv_unitprice2);
        tv_totalprice2 = findViewById(R.id.tv_totalprice2);

        tv_id = findViewById(R.id.tv_id);

        Intent intent = getIntent();
        String userId = intent.getStringExtra("userID");

        // 회원정보 버튼
        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");

                intent = new Intent(RP_PayDetailActivity.this, BiometricActivity.class);
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
                intent = new Intent(RP_PayDetailActivity.this, MainActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
                finish(); // 현재 액티비티를 종료하여 이전 액티비티로 돌아갈 수 있도록 함
            }
        });

        tv_id.setText(userId);

        Response.Listener<String> responseListner = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonarray = new JSONArray(response);
                    for(int i=0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        String product       = jsonobject.getString("product");
                        String amount    = jsonobject.getString("amount");
                        String unitPrice  = jsonobject.getString("unitPrice");
                        String totalPrice = jsonobject.getString("totalPrice");
                        if(i==0){
                            tv_product1.setText(product);
                            tv_amount1.setText(amount);
                            tv_unitprice1.setText(unitPrice);
                            tv_totalprice1.setText(totalPrice);
                        }else{
                            tv_product2.setText(product);
                            tv_amount2.setText(amount);
                            tv_unitprice2.setText(unitPrice);
                            tv_totalprice2.setText(totalPrice);
                        }

                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        RP_PaymentDetailRequest paymentDetailRequest = null;
        try {
            paymentDetailRequest = new RP_PaymentDetailRequest(userId,responseListner, RP_PayDetailActivity.this);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
        RequestQueue queue = Volley.newRequestQueue(RP_PayDetailActivity.this);
        queue.add(paymentDetailRequest);


        TextView textPay = findViewById(R.id.text_pay);
        textPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");
                // PayDetail 액티비티로 이동하는 인텐트 생성
                intent = new Intent(RP_PayDetailActivity.this, RP_PayDetailActivity.class);
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
                intent = new Intent(RP_PayDetailActivity.this, BiometricActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });

    }
}
