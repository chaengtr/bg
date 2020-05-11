package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import com.example.myapplication.adapter.SelectAdapter;
import com.example.myapplication.model.Game;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectGameActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private CollectionReference gameRef = db.collection("games");
    private CollectionReference bookingRef = db.collection("booking");

    private SelectAdapter adapter;

    private List<Game> dataSearch = new ArrayList<>();
    private List<Game> gameList = new ArrayList<>();
    private String bookingId;

    private EditText search;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_game);
        getDataFromDB();

        search = findViewById(R.id.search);
        recyclerView = findViewById(R.id.recycler_view);

        bookingId = bookingRef.document().getId();
        Log.d("wow", "onCreate: " + bookingId);

        adapter = new SelectAdapter(this, bookingId, gameList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dataSearch.clear();
                if (search.length() != 0) {
                    String str = (s + "").toLowerCase();
                    for (Game game : gameList) {
                        if ((game.getGameName().toLowerCase()).contains(str)) {
                            dataSearch.add(game);
                        }
                        adapter.setGameList(dataSearch);
                        adapter.setItemClickListener(new SelectAdapter.ItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                setGameDetail(dataSearch, position);
                            }
                        });
                    }
                } else {
                    adapter.setGameList(gameList);
                    adapter.setItemClickListener(new SelectAdapter.ItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            setGameDetail(gameList, position);
                        }
                    });
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        adapter.setItemClickListener(new SelectAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                setGameDetail(gameList, position);
            }
        });

    }

    private void setGameDetail(List<Game> gameList, int position) {
        int min = gameList.get(position).getPlayerMin();
        int max = gameList.get(position).getPlayerMax();
        String detail = gameList.get(position).getGameDetail();
        String type = gameList.get(position).getTypeName();
        String data = "ประเภทเกม: " + type + "\nจำนวนผู้เล่น: " + min + " - " + max + "\n" + detail;
        Intent intent = new Intent(getApplicationContext(), BookingDetailActivity.class);
        intent.putExtra("gameId", gameList.get(position).getGameId());
        intent.putExtra("image", gameList.get(position).getImageRef());
        intent.putExtra("gameName", gameList.get(position).getGameName());
        intent.putExtra("type", type);
        intent.putExtra("detail", detail);
        intent.putExtra("playerMin", min);
        intent.putExtra("playerMax", max);
        intent.putExtra("available", gameList.get(position).getAvailable());
        startActivity(intent);
    }


    /* DATABASE */
    private void getDataFromDB() {
        /* GAME */
        Query gameQuery = gameRef.orderBy("rating", Query.Direction.DESCENDING);
        gameQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot q : queryDocumentSnapshots) {
                    String id = q.getId();
                    String name = q.getString("game_name");
                    String type = q.getString("type_name");
                    String detail = q.getString("game_detail");
                    String imageName = q.getString("image_name");
                    String imageRef = q.getString("image_ref");
                    double min = q.getDouble("player_min");
                    double max = q.getDouble("player_max");
                    double quantity = q.getDouble("quantity");
                    double available = q.getDouble("available");
                    double active = q.getDouble("active");
                    double rating = q.getDouble("rating");
                    gameList.add(new Game(id, name, type, detail, imageName, imageRef, (int) min,
                            (int) max, (int) quantity, (int) available, (int) active, rating));
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
