package com.example.mukormosidopont2;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private TextView selectedDateText;
    private long selectedDateMillis = -1;

    private static final String CHANNEL_ID = "booking_channel";
    private static final int NOTIFICATION_ID = 1001;
    public static final int ALARM_NOTIFICATION_ID = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        CalendarView calendarView = findViewById(R.id.calendarView);
        selectedDateText = findViewById(R.id.selectedDateText);
        Button bookAppointmentButton = findViewById(R.id.bookAppointmentButton);

        createNotificationChannel();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            selectedDateText.setText("Kiválasztott nap: " + selectedDate);

            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 22, 0, 0); // 22:00:00 fixen, ahogy a példában
            cal.set(Calendar.MILLISECOND, 0);
            selectedDateMillis = cal.getTimeInMillis();
        });

        bookAppointmentButton.setOnClickListener(v -> {
            String selectedDate = selectedDateText.getText().toString();
            if (selectedDate.equals("Válassz egy napot a naptárból") || selectedDateMillis == -1) {
                Toast.makeText(this, "Először válassz egy napot!", Toast.LENGTH_SHORT).show();
            } else {
                // Firebase Firestore mentés
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(this, "Nem vagy bejelentkezve!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String userId = user.getUid();

                // A kiválasztott dátum ISO 8601 formátumban
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                String isoDate = sdf.format(new Date(selectedDateMillis)); // kiválasztott dátum

                HashMap<String, Object> booking = new HashMap<>();
                booking.put("bookingDate", isoDate);
                booking.put("userId", userId);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("bookings")
                        .add(booking)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Időpont lefoglalva: " + selectedDate, Toast.LENGTH_SHORT).show();
                            showBookingNotification(selectedDate);
                            setAlarmForNotification();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Hiba történt a foglalás mentésekor.", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void showBookingNotification(String selectedDate) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Foglalás sikeres")
                .setContentText("Időpont lefoglalva: " + selectedDate)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Foglalás csatorna";
            String description = "Foglalási értesítések";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setAlarmForNotification() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = System.currentTimeMillis() + 60 * 1000; // 1 perc múlva
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "Foglalási oldal aktív!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(this, "Foglalási oldal szünetel!", Toast.LENGTH_SHORT).show();
    }

    private boolean onMenuItemClick(@NonNull MenuItem item) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            Toast.makeText(this, "This feature requires a newer version of Android.", Toast.LENGTH_LONG).show();
            return false;
        }

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
}

// AlarmReceiver osztály a foglalás emlékeztetőhöz
class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "booking_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Foglalás emlékeztető")
                .setContentText("Ne felejtsd el az időpontodat!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(BookingActivity.ALARM_NOTIFICATION_ID, builder.build());
    }
}