package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.adapter.UserAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {
    public static final String TAG = "TAG";

    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<String> customerList;
    private String cusId, bookingCode;

    private Button btnChat;
    private TextView tvMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        customerList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        cusId = firebaseUser.getUid();

        btnChat = findViewById(R.id.btn_chat);
        tvMember = findViewById(R.id.tv_member);

        db.collection("customers").document(cusId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    bookingCode = doc.getString("booking_code");
                    readCustomers(bookingCode);
                }
            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChat();
            }
        });
    }

    private void goToChat(){
        Intent intent = new Intent(UsersActivity.this, MessageActivity.class);
        intent.putExtra("bookingCode", bookingCode);
        Log.i(TAG, "goToChat: " + bookingCode);
        startActivity(intent);
    }

    private void readCustomers(String bookingCode){
        db.collection("BOOKING/" + bookingCode + "/customers")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot q : queryDocumentSnapshots) {
                            customerList.add(q.getId());
                            Log.i(TAG, "Read customer: " + q.getId());
                        }
                        adapter = new UserAdapter(customerList);
                        recyclerView.setAdapter(adapter);
                        Log.i(TAG, "readCustomers: " + customerList.size());
                        tvMember.setText("สมาชิก (" +  customerList.size() + ")");
                    }
                });

    }
}
