package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.myapplication.adapter.FavorAdapter;
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

public class GamesActivity extends AppCompatActivity {
    private String sortKeyword;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private CollectionReference gameRef = db.collection("games");

    private FavorAdapter adapter;
    private ArrayAdapter<String> spinnerAdapter;

    private List<Game> dataSearch = new ArrayList<>();
    private List<Game> gameList = new ArrayList<>();
    private List<String> spinnerData = new ArrayList<>();
    private List<String> favList = new ArrayList<>();
    private String cusId;

    private EditText search;
    private RecyclerView recyclerView;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_favor);
        getDataFromDB();
        setSpinnerData();

        search = findViewById(R.id.search);
        recyclerView = findViewById(R.id.recycler_view);
        spinner = findViewById(R.id.spinner);

        adapter = new FavorAdapter(this, cusId, gameList, favList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, spinnerData);
        spinner.setAdapter(spinnerAdapter);

        adapter.setItemClickListener(new FavorAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                setGameDetail(gameList, position);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortKeyword = spinnerData.get(position);
                setRecyclerData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

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
                        adapter.setItemClickListener(new FavorAdapter.ItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                setGameDetail(dataSearch, position);
                            }
                        });
                    }
                } else {
                    adapter.setGameList(gameList);
                    adapter.setItemClickListener(new FavorAdapter.ItemClickListener() {
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
    }

    private void setSpinnerData() {
        spinnerData.add("ความนิยม");
        spinnerData.add("จำนวนผู้เล่นน้อยสุด");
        spinnerData.add("จำนวนผู้เล่นมากสุด");
        spinnerData.add("ชื่อเกม");
    }

    private void setRecyclerData() {
        gameList.clear();
        Query gameQuery;
        if (sortKeyword.equals("ความนิยม")) {
            gameQuery = gameRef.orderBy("rating", Query.Direction.DESCENDING);
        } else if (sortKeyword.equals("จำนวนผู้เล่นน้อยสุด")) {
            gameQuery = gameRef.orderBy("player_min");
        } else if (sortKeyword.equals("จำนวนผู้เล่นมากสุด")) {
            gameQuery = gameRef.orderBy("player_max", Query.Direction.DESCENDING);
        } else {
            gameQuery = gameRef.orderBy("game_name");
        }

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

    private void setGameDetail(List<Game> gameList, int position) {
        int min = gameList.get(position).getPlayerMin();
        int max = gameList.get(position).getPlayerMax();
        String detail = gameList.get(position).getGameDetail();
        String type = gameList.get(position).getTypeName();
        String data = "ประเภทเกม: " + type + "\nจำนวนผู้เล่น: " + min + " - " + max + " คน \n" + detail;
        Intent intent = new Intent(GamesActivity.this, GameDetailActivity.class);
        intent.putExtra("gameId", gameList.get(position).getGameId());
        intent.putExtra("image", gameList.get(position).getImageRef());
        intent.putExtra("gameName", gameList.get(position).getGameName());
        intent.putExtra("gameDetail", data);
        startActivity(intent);
    }

    /* DATABASE */
    private void getDataFromDB() {
        /* FAVOR */
        if (user != null) {
            cusId = user.getUid();
            db.collection("favor/" + cusId + "/fav_list")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot q : queryDocumentSnapshots) {
                                favList.add(q.getId());
                                Log.i("(-.-)?", "GET favor: " + q.getId());
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
        } else {
            Log.i("(-.-)?", "NO USER");
        }
    }
}
