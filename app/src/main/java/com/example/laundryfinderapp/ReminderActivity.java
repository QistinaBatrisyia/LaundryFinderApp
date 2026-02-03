package com.example.laundryfinderapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ReminderActivity extends AppCompatActivity {

    private TextView txtSelected;
    private TimePicker timePickerWheel;
    private Calendar selectedCal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        // Bind views
        txtSelected = findViewById(R.id.txtSelected);
        timePickerWheel = findViewById(R.id.timePickerWheel);

        Button btnPickDate = findViewById(R.id.btnPickDate);
        Button btnSetReminder = findViewById(R.id.btnSetReminder);
        TextView txtBack = findViewById(R.id.txtBack);

        // Init calendar
        selectedCal = Calendar.getInstance();
        selectedCal.add(Calendar.MINUTE, 1); // default future time
        selectedCal.set(Calendar.SECOND, 0);
        selectedCal.set(Calendar.MILLISECOND, 0);

        // Init TimePicker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePickerWheel.setHour(selectedCal.get(Calendar.HOUR_OF_DAY));
            timePickerWheel.setMinute(selectedCal.get(Calendar.MINUTE));
        } else {
            timePickerWheel.setCurrentHour(selectedCal.get(Calendar.HOUR_OF_DAY));
            timePickerWheel.setCurrentMinute(selectedCal.get(Calendar.MINUTE));
        }

        updateLabel();

        // Pick date button
        btnPickDate.setOnClickListener(v -> showDatePicker());

        // Time change listener
        timePickerWheel.setOnTimeChangedListener((view, hour, minute) -> updateLabel());

        // Set reminder
        btnSetReminder.setOnClickListener(v -> setReminder());

        // Back
        txtBack.setOnClickListener(v -> finish());
    }

    // ---------------- DATE PICKER ----------------

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedCal.set(Calendar.YEAR, year);
                    selectedCal.set(Calendar.MONTH, month);
                    selectedCal.set(Calendar.DAY_OF_MONTH, day);
                    updateLabel();
                },
                selectedCal.get(Calendar.YEAR),
                selectedCal.get(Calendar.MONTH),
                selectedCal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    // ---------------- UPDATE LABEL ----------------

    private void updateLabel() {
        int hour, minute;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = timePickerWheel.getHour();
            minute = timePickerWheel.getMinute();
        } else {
            hour = timePickerWheel.getCurrentHour();
            minute = timePickerWheel.getCurrentMinute();
        }

        selectedCal.set(Calendar.HOUR_OF_DAY, hour);
        selectedCal.set(Calendar.MINUTE, minute);

        txtSelected.setText(
                "Selected: " +
                        selectedCal.get(Calendar.DAY_OF_MONTH) + "/" +
                        (selectedCal.get(Calendar.MONTH) + 1) + "/" +
                        selectedCal.get(Calendar.YEAR) + "  " +
                        String.format("%02d:%02d", hour, minute)
        );
    }

    // ---------------- SET REMINDER ----------------

    private void setReminder() {
        long triggerTime = selectedCal.getTimeInMillis();
        long now = System.currentTimeMillis();

        if (triggerTime <= now) {
            Toast.makeText(this, "Please select a future time.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", "Laundry Finder");
        intent.putExtra("message", "Reminder: Please collect your laundry!");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }

        Toast.makeText(this, "Reminder set successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
