package com.example.mukormosidopont2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView emailText, usernameText, bookingsText, locationText;
    private ImageView profileImageView;
    private Button cameraButton, updateUsernameButton, sortBookingsButton, latestBookingsButton, futureBookingsButton;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private ArrayList<DocumentSnapshot> bookingDocs = new ArrayList<>();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSIONS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        emailText = findViewById(R.id.profile_email);
        usernameText = findViewById(R.id.profile_username);
        bookingsText = findViewById(R.id.profile_bookings);
        profileImageView = findViewById(R.id.profile_image);
        locationText = findViewById(R.id.profile_location);
        cameraButton = findViewById(R.id.profile_camera_button);
        updateUsernameButton = findViewById(R.id.profile_update_username_button);
        sortBookingsButton = findViewById(R.id.profile_sort_bookings_button);
        latestBookingsButton = findViewById(R.id.profile_latest_bookings_button);
        futureBookingsButton = findViewById(R.id.profile_future_bookings_button);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        cameraButton.setOnClickListener(v -> checkCameraPermissionAndOpenCamera());

        checkLocationPermissionAndShowLocation();

        if (user == null) {
            emailText.setText("Nincs bejelentkezve");
            usernameText.setText("Nincs bejelentkezve");
            bookingsText.setText("");
            updateUsernameButton.setEnabled(false);
            return;
        }

        emailText.setText(user.getEmail());

        // Username kiolvasása Firestore-ból
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            String username = documentSnapshot.getString("username");
            if (username != null && !username.isEmpty()) {
                usernameText.setText(username);
            } else {
                usernameText.setText("Nincs megadva");
            }
        }).addOnFailureListener(e -> {
            usernameText.setText("Nincs megadva");
        });

        // Username frissítése
        updateUsernameButton.setOnClickListener(v -> showUpdateUsernameDialog());

        // Alapértelmezett: időrendi sorrend (orderBy)
        loadBookingsOrdered();

        sortBookingsButton.setOnClickListener(v -> loadBookingsOrdered());
        latestBookingsButton.setOnClickListener(v -> loadLatestBookings());
        futureBookingsButton.setOnClickListener(v -> loadFutureBookings());
    }

    private void showUpdateUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Felhasználónév frissítése");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Új felhasználónév");
        builder.setView(input);

        builder.setPositiveButton("Mentés", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                HashMap<String, Object> update = new HashMap<>();
                update.put("username", newUsername);
                db.collection("users").document(user.getUid())
                        .set(update, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            usernameText.setText(newUsername);
                            Toast.makeText(this, "Felhasználónév frissítve!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Hiba a név frissítésekor.", Toast.LENGTH_SHORT).show();
                        });
            }
        });
        builder.setNegativeButton("Mégse", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadBookingsOrdered() {
        if (user == null) return;
        db.collection("bookings")
                .whereEqualTo("userId", user.getUid())
                .orderBy("bookingDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingDocs.clear();
                    StringBuilder sb = new StringBuilder();
                    int idx = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        bookingDocs.add(doc);
                        String date = doc.getString("bookingDate");
                        sb.append(idx + 1).append(". ").append(date != null ? date : "N/A")
                          .append("   [Törléshez nyomj ide]").append("\n");
                        idx++;
                    }
                    if (bookingDocs.isEmpty()) {
                        bookingsText.setText("Nincsenek foglalások.");
                    } else {
                        bookingsText.setText(sb.toString());
                        bookingsText.setOnClickListener(v -> showDeleteDialog());
                    }
                })
                .addOnFailureListener(e -> bookingsText.setText("Hiba a foglalások lekérdezésekor."));
    }

    private void loadLatestBookings() {
        if (user == null) return;
        db.collection("bookings")
                .whereEqualTo("userId", user.getUid())
                .orderBy("bookingDate", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingDocs.clear();
                    StringBuilder sb = new StringBuilder();
                    int idx = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        bookingDocs.add(doc);
                        String date = doc.getString("bookingDate");
                        sb.append(idx + 1).append(". ").append(date != null ? date : "N/A")
                          .append("   [Törléshez nyomj ide]").append("\n");
                        idx++;
                    }
                    if (bookingDocs.isEmpty()) {
                        bookingsText.setText("Nincsenek foglalások.");
                    } else {
                        bookingsText.setText(sb.toString());
                        bookingsText.setOnClickListener(v -> showDeleteDialog());
                    }
                })
                .addOnFailureListener(e -> bookingsText.setText("Hiba a foglalások lekérdezésekor."));
    }

    private void loadFutureBookings() {
        if (user == null) return;
        String nowIso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
        db.collection("bookings")
                .whereEqualTo("userId", user.getUid())
                .whereGreaterThanOrEqualTo("bookingDate", nowIso)
                .orderBy("bookingDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingDocs.clear();
                    StringBuilder sb = new StringBuilder();
                    int idx = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        bookingDocs.add(doc);
                        String date = doc.getString("bookingDate");
                        sb.append(idx + 1).append(". ").append(date != null ? date : "N/A")
                          .append("   [Törléshez nyomj ide]").append("\n");
                        idx++;
                    }
                    if (bookingDocs.isEmpty()) {
                        bookingsText.setText("Nincsenek jövőbeli foglalások.");
                    } else {
                        bookingsText.setText(sb.toString());
                        bookingsText.setOnClickListener(v -> showDeleteDialog());
                    }
                })
                .addOnFailureListener(e -> bookingsText.setText("Hiba a foglalások lekérdezésekor."));
    }

    private void loadBookingsWithDelete() {
        db.collection("bookings")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingDocs.clear();
                    StringBuilder sb = new StringBuilder();
                    int idx = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        bookingDocs.add(doc);
                        String date = doc.getString("bookingDate");
                        sb.append(idx + 1).append(". ").append(date != null ? date : "N/A")
                          .append("   [Törléshez nyomj ide]").append("\n");
                        idx++;
                    }
                    if (bookingDocs.isEmpty()) {
                        bookingsText.setText("Nincsenek foglalások.");
                    } else {
                        bookingsText.setText(sb.toString());
                        bookingsText.setOnClickListener(v -> showDeleteDialog());
                    }
                })
                .addOnFailureListener(e -> bookingsText.setText("Hiba a foglalások lekérdezésekor."));
    }

    private void showDeleteDialog() {
        if (bookingDocs.isEmpty()) return;
        String[] items = new String[bookingDocs.size()];
        for (int i = 0; i < bookingDocs.size(); i++) {
            String date = bookingDocs.get(i).getString("bookingDate");
            items[i] = (i + 1) + ". " + (date != null ? date : "N/A");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Foglalás törlése");
        builder.setItems(items, (dialog, which) -> {
            DocumentSnapshot doc = bookingDocs.get(which);
            doc.getReference().delete().addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Foglalás törölve!", Toast.LENGTH_SHORT).show();
                loadBookingsWithDelete();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Hiba a törléskor.", Toast.LENGTH_SHORT).show();
            });
        });
        builder.setNegativeButton("Mégse", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Kamera engedély szükséges a profilképhez.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showLocation();
            } else {
                locationText.setText("Helymeghatározás engedély megtagadva.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profileImageView.setImageBitmap(imageBitmap);
            // Itt el lehetne menteni a képet Firebase Storage-ba is, ha szükséges
        }
    }

    private void checkLocationPermissionAndShowLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            showLocation();
        }
    }

    private void showLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationText.setText("Nincs helyhozzáférés.");
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            locationText.setText("Hely: " + location.getLatitude() + ", " + location.getLongitude());
        } else {
            locationText.setText("Nem elérhető a hely.");
        }
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
            // Már itt vagyunk
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
}
