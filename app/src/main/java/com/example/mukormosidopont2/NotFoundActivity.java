package com.example.mukormosidopont2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class NotFoundActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_found);

        // 3 másodperc után visszairányít a HomeActivity-re
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(NotFoundActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}