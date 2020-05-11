package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.model.Game;
import com.example.myapplication.model.Notification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BookingActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getUid();

    private Game game;
    private boolean isBooked = false;
    private String bookingCode, group, gameId;
    private int numOfMember;

    private TextView codeTV, timeTV;
    private ImageView gameIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        codeTV = findViewById(R.id.code);
        gameIV = findViewById(R.id.game_image);
        timeTV = findViewById(R.id.time);

        db.collection("customers").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                bookingCode = documentSnapshot.getString("booking_code");
                if (bookingCode != null) {
                    isBooked = true;
                    codeTV.setText(bookingCode);
                    getBooking();
                }
            }
        });

        gameIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBooked) {
                    gotoGameDetail();
                } else {
                    gotoSelectGame();
                }
            }
        });
    }

    public void cancel(View view) {
        if (!isBooked) {
            Toast.makeText(this, "คุณยังไม่ได้ทำการจอง", Toast.LENGTH_SHORT).show();
        } else {
            if (group.equals("no")) {
                deleteBooking();
            } else {
                db.collection("BOOKING/" + bookingCode + "/customers").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        numOfMember = queryDocumentSnapshots.getDocuments().size();
                        if (numOfMember == 1) {
                            deleteBooking();
                        } else {
                            deleteMember();
                        }
                    }
                });
            }

            // Notification
            String title = "ยกเลิกการจองสำเร็จ";
            String detail = "รหัสจองของคุณคือ \"" + bookingCode + "\"";
            String image = getIcon("cancel");
            sendNotification(new Notification(new Date(), detail, image, title));

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void gotoSelectGame() {
        Intent intent = new Intent(this, SelectGameActivity.class);
        startActivity(intent);
    }

    private void gotoGameDetail() {
        int min = game.getPlayerMin();
        int max = game.getPlayerMax();
        String detail = game.getGameDetail();
        String type = game.getTypeName();
        String data = "ประเภทเกม: " + type + "\nจำนวนผู้เล่น: " + min + " - " + max + "\n" + detail;

        Intent intent = new Intent(this, GameDetailActivity.class);
        intent.putExtra("gameId", game.getGameId());
        intent.putExtra("image", game.getImageRef());
        intent.putExtra("gameName", game.getGameName());
        intent.putExtra("gameDetail", data);
        startActivity(intent);
    }

    private void getBooking() {
        db.collection("BOOKING").document(bookingCode).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                group = documentSnapshot.getString("group");
                gameId = documentSnapshot.getString("game_id");
                Date date = documentSnapshot.getDate("booking_start");
                String time = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(date);
                timeTV.setText(time);
                getGame();
            }
        });
    }

    private void getGame() {
        db.collection("games").document(gameId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.getData() != null) {
                        double active = doc.getDouble("active");
                        double available = doc.getDouble("available");
                        String detail = doc.getString("game_detail");
                        String name = doc.getString("game_name");
                        double max = doc.getDouble("player_min");
                        double min = doc.getDouble("player_max");
                        double quantity = doc.getDouble("quantity");
                        double rating = doc.getDouble("rating");
                        String type = doc.getString("type_name");
                        String imageName = doc.getString("image_name");
                        String image = doc.getString("image_ref");
                        game = new Game(gameId, name, type, detail, imageName, image, (int) min, (int) max, (int) quantity, (int) available, (int) active, rating);

                        Picasso.get().load(image).into(gameIV);
                    }
                }
            }
        });
    }

    private void deleteBooking() {
        db.collection("BOOKING/" + bookingCode + "/customers").document(userId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                db.collection("BOOKING").document(bookingCode).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateCustomerBooking();
                        updateGameAvailable();
                    }
                });
            }
        });
    }

    private void deleteMember() {
        db.collection("BOOKING/" + bookingCode + "/customers").document(userId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                updateCustomerBooking();
            }
        });
    }

    private void updateCustomerBooking() {
        DocumentReference ref = db.collection("customers").document(userId);
        ref.update("booking_code", FieldValue.delete());
    }

    private void updateGameAvailable() {
        int value = game.getAvailable() + 1;
        db.collection("games").document(game.getGameId()).update("available", value).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("(-.-)?", "UPDATE games (available)");
            }
        });
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
}
