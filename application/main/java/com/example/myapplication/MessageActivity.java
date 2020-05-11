package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.adapter.MessageAdapter;
import com.example.myapplication.model.Message;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    public static final String TAG = "TAG";

    private CircleImageView civUserImage;
    private TextView tvUsername;
    private ImageButton ibSend;
    private EditText etTextSend;
    private Toolbar toolbar;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    CollectionReference cusRef;
    private String collectionPath;

    private MessageAdapter adapter;
    private List<Message> messageList;
    private List<String> customerList;

    RecyclerView recyclerView;

    private String cusId, bookingCode;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        civUserImage = findViewById(R.id.civUserImage);
        tvUsername = findViewById(R.id.tvUsername);
        ibSend = findViewById(R.id.ibSend);
        etTextSend = findViewById(R.id.etTextSend);
        toolbar = findViewById(R.id.toolBar);

        db = FirebaseFirestore.getInstance();
        cusRef = db.collection("customers");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        cusId =firebaseUser.getUid();

        customerList = new ArrayList<>();

        intent = getIntent();
        bookingCode = intent.getStringExtra("bookingCode");
        Log.i(TAG, "bookingCode: " + bookingCode);

        collectionPath = "message/" + bookingCode + "/message_list";

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);

        readMessage();

        db.collection("customers")
                .document(cusId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String image = documentSnapshot.getString("cus_image");
                        String username = documentSnapshot.getString("cus_username");
                        tvUsername.setText(username);
                        if (image.equals("")){
                            Picasso.get()
                                    .load(R.drawable.ic_user_photo)
                                    .placeholder(R.drawable.ic_user_photo)
                                    .into(civUserImage);
                            Log.d(TAG, "onComplete: "+R.drawable.ic_user_photo);
                        }else {
                            try {
                                Picasso.get().load(image).into(civUserImage);
                            } catch (Exception e) {
                                Picasso.get()
                                        .load(R.drawable.ic_user_photo)
                                        .placeholder(R.drawable.ic_user_photo)
                                        .into(civUserImage);
                            }
                        }
                        Log.i(TAG, "image url: " + image);
                    }
                });

        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etTextSend.getText().toString();
                if (!message.equals("")){
                    sendMessage(cusId, message);
                }
                etTextSend.setText("");
            }
        });
    }

    private void sendMessage(final String sender, final String message){
        final Date date = new Date();
        final Message data = new Message(sender, message, date);

        db.collection(collectionPath).document().set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "ADD message: " + sender + " " + message);
            }
        });
    }

    private void readMessage() {
        db.collection(collectionPath).orderBy("time").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                messageList.clear();
                for (DocumentSnapshot q : queryDocumentSnapshots) {
                    String sender = q.getString("sender");
                    String message = q.getString("message");
                    Date time = q.getDate("time");
                    messageList.add(new Message(sender, message, time));
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "Read message: " + sender + " " + message);
                }
            }
        });
    }
}
