package com.example.mukormosidopont2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private boolean onMenuItemClick(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == R.id.menu_home) {
            startActivity(new Intent(this, HomeActivity.class));
        } else if (itemID == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (itemID == R.id.menu_booking) {
            startActivity(new Intent(this, BookingActivity.class));
        } else if (itemID == R.id.menu_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (itemID == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Kijelentkezés!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            return false;
        }

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(this, "Kiléptél a Rólunk oldalról.", Toast.LENGTH_SHORT).show();
    }
}