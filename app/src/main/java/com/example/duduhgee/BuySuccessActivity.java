package com.example.duduhgee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.duduhgee.BiometricActivity;
import com.example.duduhgee.MainActivity;
import com.example.duduhgee.R;

public class BuySuccessActivity extends AppCompatActivity {

    private Button btn_info;
    private Button btn_home;
    private ImageView BuysuccessImageView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buysuccess);

        btn_home = findViewById(R.id.btn_home);
        btn_info = findViewById(R.id.btn_info);

        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");
                // 메인 액티비티로 이동하는 인텐트 생성
                intent = new Intent(BuySuccessActivity.this, MainActivity.class);
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

                intent = new Intent(BuySuccessActivity.this, BiometricActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });

        BuysuccessImageView = findViewById(R.id.BuysuccessImageView); // completionImageView 초기화

        Intent intent = getIntent();
        String purchaseItem = intent.getStringExtra("purchase_item");
        byte[] signedChallenge = intent.getByteArrayExtra("signed_challenge");

        // 구매한 항목에 따라 이미지 설정
        if ("tissue".equals(purchaseItem)) {
            BuysuccessImageView.setImageResource(R.drawable.tissue); // tissue 이미지 리소스
        } else if ("toothbrush".equals(purchaseItem)) {
            BuysuccessImageView.setImageResource(R.drawable.toothbrush); // toothbrush 이미지 리소스
        }
    }
}