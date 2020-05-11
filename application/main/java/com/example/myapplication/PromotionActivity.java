package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.myapplication.adapter.PromotionAdapter;
import com.example.myapplication.fragment.ProfileFragment;
import com.example.myapplication.model.Promotion;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

public class PromotionActivity extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    CollectionReference proRef;
    DocumentReference cusRef;

    PromotionAdapter adapter;

    TextView tvCusPt;

    String cusId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);

        tvCusPt =(TextView)findViewById(R.id.tv_cus_pt);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        cusId = firebaseAuth.getCurrentUser().getUid();
        proRef = firebaseFirestore.collection("promotions");
        cusRef = firebaseFirestore.collection("customers").document(cusId);

        showCustomerPoints();
        setUpRecyclerView();
        deleteProItem();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK ){
//                //
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    private void showCustomerPoints() {
        cusRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    String point = document.get("point") + " คะแนน";

                    tvCusPt.setText(point);
                }
            }
        });
    }

    private void deleteProItem(){

    }

    private void setUpRecyclerView(){
        Query query = proRef.whereGreaterThanOrEqualTo("pro_end", new Date()).orderBy("pro_end", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Promotion> options = new FirestoreRecyclerOptions
                .Builder<Promotion>()
                .setQuery(query, Promotion.class)
                .build();

        adapter = new PromotionAdapter(options);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.rv_pro);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new PromotionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
//                Promotion promotions = documentSnapshot.toObject(Promotion.class);
                String id = documentSnapshot.getId();
                Intent intent = new Intent(getApplicationContext(), RedeemActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}
