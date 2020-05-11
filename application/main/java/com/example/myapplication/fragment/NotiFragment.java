package com.example.myapplication.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.NotiAdapter;
import com.example.myapplication.model.Notification;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class NotiFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notiRef;

    private String cusId, collectionPath;
    private NotiAdapter adapter;
    private RecyclerView notifyRV;

    public NotiFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_noti, container, false);
        notifyRV = view.findViewById(R.id.noti_rv);

        cusId = FirebaseAuth.getInstance().getUid();
        collectionPath = "notification/" + cusId + "/notification_list";
        notiRef = db.collection(collectionPath);
        setRecyclerView();

        // Delete notification item
        adapter.setOnLongItemClickListener(new NotiAdapter.OnItemLongClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                final String id = documentSnapshot.getId();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setPositiveButton("ลบ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteNoti(id);
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                // Setting button design
                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
                button.setGravity(Gravity.CENTER_VERTICAL);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void setRecyclerView() {
        Query notiQuery = notiRef.orderBy("date", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Notification> options = new FirestoreRecyclerOptions
                .Builder<Notification>()
                .setQuery(notiQuery, Notification.class)
                .build();

        adapter = new NotiAdapter(options);
        notifyRV.setHasFixedSize(true);
        notifyRV.setLayoutManager(new LinearLayoutManager(getContext()));
        notifyRV.setAdapter(adapter);
    }

    private void deleteNoti(String id) {
        db.collection(collectionPath).document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("(-.-)?", "DELETE notification");
            }
        });
    }
}
