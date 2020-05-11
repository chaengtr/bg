package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.myapplication.model.Notification;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.picasso.Picasso;

import org.threeten.bp.LocalTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BookingDetailActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getUid();

    private List<LocalTime> timeData = new ArrayList<>();
    private List<String> bookingCodes = new ArrayList<>();
    private ArrayAdapter<LocalTime> adapter;
    private LocalTime selectedTime;
    private String gameImage, gameName, gameId, bookingCode, type, detail;
    private int min, max, available;

    private ImageView imageGame;
    private TextView tvGameName, player;
    private Spinner spinner;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        AndroidThreeTen.init(this);

        imageGame = findViewById(R.id.game_image);
        tvGameName = findViewById(R.id.game_name_tv);
        player = findViewById(R.id.player);
        spinner = findViewById(R.id.spinner);
        checkBox = findViewById(R.id.checkBox);

        gameImage = getIntent().getStringExtra("image");
        gameName = getIntent().getStringExtra("gameName");
        gameId = getIntent().getStringExtra("gameId");
        type = getIntent().getStringExtra("type");
        detail = getIntent().getStringExtra("detail");
        min = getIntent().getIntExtra("playerMin", 0);
        max = getIntent().getIntExtra("playerMax", 0);
        available = getIntent().getIntExtra("available", 0);
        Picasso.get().load(gameImage).into(imageGame);
        tvGameName.setText(gameName);
        player.setText(String.format("จำนวนผู้เล่น %d - %d คน", min, max));

        getBookingCode();
        setSpinner(LocalTime.now());

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, timeData);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTime = adapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        imageGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoGameDetail();
            }
        });
    }

    public void submit(View view) {
        bookingCode = randomCode();

        // ถ้า booking code ซ้ำ random ใหม่
        while (bookingCodes.contains(bookingCode)) {
            bookingCode = randomCode();
        }

        String isGroup;
        if (checkBox.isChecked()) {
            isGroup = "yes";
        } else {
            isGroup = "no";
        }
        addBooking(isGroup);

        // Notification
        String title = "จองสำเร็จ";
        String detail = "รหัสจองของคุณคือ \"" + bookingCode + "\"";
        String image = getIcon("accept");
        sendNotification(new Notification(new Date(), detail, image, title));

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void gotoGameDetail() {
        String data = "ประเภทเกม: " + type + "\nจำนวนผู้เล่น: " + min + " - " + max + "\n" + detail;
        Intent intent = new Intent(this, GameDetailActivity.class);
        intent.putExtra("gameId", gameId);
        intent.putExtra("image", gameImage);
        intent.putExtra("gameName", gameName);
        intent.putExtra("gameDetail", data);
        startActivity(intent);
    }

    public void cancel(View view) {
        finish();
    }

    private void setSpinner(LocalTime now) {
        LocalTime start = LocalTime.of(10, 0, 0);
        LocalTime end = LocalTime.of(23, 0, 0);
        List<LocalTime> list = new ArrayList<>();

        // Create time choice model
        list.add(start);
        while (start.isBefore(end)) {
            start = start.plusHours(1);
            list.add(start);
        }

        // Set 6 choice in spinner
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (now.isBefore(list.get(i))) {
                index += 1;
                timeData.add(list.get(i));
                if (index == 3) {
                    break;
                }
            }
        }
    }

    private void getBookingCode() {
        db.collection("BOOKING").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot s : queryDocumentSnapshots) {
                    bookingCodes.add(s.getId());
                }
            }
        });
    }

    private String randomCode() {
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String num = "1234567890";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            if (i < 2) {
                sb.append(str.charAt(random.nextInt(str.length())));
            } else {
                sb.append(num.charAt(random.nextInt(num.length())));
            }
        }
        return sb.toString();
    }

    private String getIcon(String icon) {
        if (icon.equals("accept")) {
            return "https://www.flaticon.com/premium-icon/icons/svg/2550/2550322.svg";
        } else if (icon.equals("cancel")) {
            return "https://www.flaticon.com/premium-icon/icons/svg/2550/2550327.svg";
        }
        return "";
    }

    private void sendNotification(Notification notification) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getDetail())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_noti)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());

        addNotification(notification);
    }

    private void addNotification(Notification notification) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();
        String collectionPath = "notification/" + userId + "/notification_list";

        database.collection(collectionPath).add(notification).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.i("(-.-)?", "ADD notification");
            }
        });
    }

    private void addBooking(String isGroup) {
        Map<String, Object> data = new HashMap<>();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.HOUR_OF_DAY, selectedTime.getHour());
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date start = c.getTime();

        data.put("booking_end", null);
        data.put("booking_start", start);
        data.put("game_id", gameId);
        data.put("group", isGroup);
        data.put("member_max", max);
        db.collection("BOOKING").document(bookingCode).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                reduceNumberOfGame();
                Log.i("(-.-)?", "ADD booking");
            }
        });

        Map<String, Object> cus = new HashMap<>();
        cus.put("receipt_status", "ยังไม่ออกใบเสร็จ");
        cus.put("usage_status", "จอง");
        cus.put("promotion_id", "");
        db.collection("BOOKING/" + bookingCode + "/customers").document(userId).set(cus).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("(-.-)?", "ADD booking (customers)");
            }
        });

        db.collection("customers").document(userId).update("booking_code", bookingCode).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("(-.-)?", "ADD customers (booking_status)");
            }
        });
    }

    private void reduceNumberOfGame() {
        int value = available - 1;
        db.collection("games").document(gameId).update("available", value).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("(-.-)?", "UPDATE games (available)");
            }
        });
    }
}
