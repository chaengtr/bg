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

public class FavorActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private CollectionReference gameRef = db.collection("games");

    private FavorAdapter adapter;

    private List<Game> dataSearch = new ArrayList<>();
    private List<Game> gameList = new ArrayList<>();
    private List<String> favList = new ArrayList<>();
    private String cusId;

    private EditText search;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favor);
        getDataFromDB();

        search = findViewById(R.id.search);
        recyclerView = findViewById(R.id.recycler_view);

        adapter = new FavorAdapter(this, cusId, gameList, favList);
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

        adapter.setItemClickListener(new FavorAdapter.ItemClickListener() {
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
        Intent intent = new Intent(FavorActivity.this, GameDetailActivity.class);
        intent.putExtra("gameId", gameList.get(position).getGameId());
        intent.putExtra("image", gameList.get(position).getImageRef());
        intent.putExtra("gameName", gameList.get(position).getGameName());
        intent.putExtra("gameDetail", data);
        startActivity(intent);
    }

    private void getDataFromDB() {
        if (user != null) {
            cusId = user.getUid();
            Query query = db.collection("favor/" + cusId + "/fav_list").orderBy("date", Query.Direction.DESCENDING);
            query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (DocumentSnapshot q : queryDocumentSnapshots) {
                        favList.add(q.getId());
                        Log.i("(-.-)?", "GET favor: " + q.getId());
                        getGame(q.getId());
                    }
                }
            });
        } else {
            Log.i("(-.-)?", "NO USER");
        }
    }

    private void getGame(String gameId) {
        db.collection("games").document(gameId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String name = documentSnapshot.getString("game_name");
                String type = documentSnapshot.getString("type_name");
                String detail = documentSnapshot.getString("game_detail");
                String imageName = documentSnapshot.getString("image_name");
                String imageRef = documentSnapshot.getString("image_ref");
                double min = documentSnapshot.getDouble("player_min");
                double max = documentSnapshot.getDouble("player_max");
                double quantity = documentSnapshot.getDouble("quantity");
                double available = documentSnapshot.getDouble("available");
                double active = documentSnapshot.getDouble("active");
                double rating = documentSnapshot.getDouble("rating");
                gameList.add(new Game(documentSnapshot.getId(), name, type, detail, imageName, imageRef, (int) min,
                        (int) max, (int) quantity, (int) available, (int) active, rating));
                adapter.notifyDataSetChanged();
            }
        });
    }
}
