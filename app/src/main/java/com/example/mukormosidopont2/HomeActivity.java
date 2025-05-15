package com.example.mukormosidopont2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private int currentIndex = 0;
    private final int[] images = {
            R.drawable.mukorom1, R.drawable.mukorom2, R.drawable.mukorom3, R.drawable.mukorom4,
            R.drawable.mukorom5, R.drawable.mukorom6, R.drawable.mukorom7, R.drawable.mukorom8,
            R.drawable.mukorom9, R.drawable.mukorom10, R.drawable.mukorom11, R.drawable.mukorom12
    };

    private ImageView[] imageViews;
    private ImageView selectedImageView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        selectedImageView = findViewById(R.id.selectedImageView);
        imageViews = new ImageView[]{
                findViewById(R.id.mukorom1),
                findViewById(R.id.mukorom2),
        };

        Button leftButton = findViewById(R.id.leftButton);
        Button rightButton = findViewById(R.id.rightButton);

        updateImages();

        for (int i = 0; i < imageViews.length; i++) {
            int index = i;
            imageViews[i].setOnClickListener(v -> {
                selectedImageView.setImageResource(images[currentIndex + index]);
            });
        }

        leftButton.setOnClickListener(v -> {
            currentIndex = (currentIndex - 2 + images.length) % images.length;
            applySlideAnimation(false);
            updateImages();
        });

        rightButton.setOnClickListener(v -> {
            currentIndex = (currentIndex + 2) % images.length;
            applySlideAnimation(true);
            updateImages();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Mindig az első képet mutatjuk, ha visszatérünk az oldalra
        currentIndex = 0;
        updateImages();
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

    private void updateImages() {
        for (int i = 0; i < imageViews.length; i++) {
            imageViews[i].setImageResource(images[(currentIndex + i) % images.length]);
        }
    }

    private void applySlideAnimation(boolean toRight) {
        Animation animation = AnimationUtils.loadAnimation(this, toRight ? R.anim.slide_in_right : R.anim.slide_in_left);
        for (ImageView imageView : imageViews) {
            imageView.startAnimation(animation);
        }
    }
}