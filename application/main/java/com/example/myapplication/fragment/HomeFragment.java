package com.example.myapplication.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.GameDetailActivity;
import com.example.myapplication.GamesActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapter.BannerSliderAdapter;
import com.example.myapplication.adapter.FavorAdapter;
import com.example.myapplication.model.Banner;
import com.example.myapplication.model.Game;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private CollectionReference bannerRef = db.collection("banners");
    private CollectionReference gameRef = db.collection("games");

    private BannerSliderAdapter bannerAdapter;
    private FavorAdapter favorAdapter;

    private List<Banner> bannerList = new ArrayList<>();
    private List<Game> gameList = new ArrayList<>();
    private List<String> favList = new ArrayList<>();
    private String cusId;

    private TextView seeAll;
    private SliderView sliderView;
    private RecyclerView recyclerView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getDataFromDB();

        seeAll = view.findViewById(R.id.see_text);
        sliderView = view.findViewById(R.id.image_slider);
        recyclerView = view.findViewById(R.id.recycler_view);

        // Game adapter
        favorAdapter = new FavorAdapter(getActivity(), cusId, gameList, favList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(favorAdapter);

        seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GamesActivity.class);
                startActivity(intent);
            }
        });

        sliderView.setOnIndicatorClickListener(new DrawController.ClickListener() {
            @Override
            public void onIndicatorClicked(int position) {
                sliderView.setCurrentPagePosition(position);
            }
        });

        favorAdapter.setItemClickListener(new FavorAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                setGameDetail(gameList, position);
            }
        });

        return view;
    }

    private void setGameDetail(List<Game> gameList, int position) {
        int min = gameList.get(position).getPlayerMin();
        int max = gameList.get(position).getPlayerMax();
        String detail = gameList.get(position).getGameDetail();
        String type = gameList.get(position).getTypeName();
        String data = "ประเภทเกม: " + type + "\nจำนวนผู้เล่น: " + min + " - " + max + "\n" + detail;
        Intent intent = new Intent(getView().getContext(), GameDetailActivity.class);
        intent.putExtra("gameId", gameList.get(position).getGameId());
        intent.putExtra("image", gameList.get(position).getImageRef());
        intent.putExtra("gameName", gameList.get(position).getGameName());
        intent.putExtra("gameDetail", data);
        startActivity(intent);
    }

    /* DATABASE */
    private void getDataFromDB() {
        /* BANNER */
        Query bannerQuery = bannerRef.whereGreaterThanOrEqualTo("banner_end", new Date())
                .orderBy("banner_end");
        bannerQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot q : queryDocumentSnapshots) {
                    String id = q.getId();
                    String name = q.getString("banner_name");
                    String ref = q.getString("banner_image");
                    Date start = q.getDate("banner_start");
                    Date end = q.getDate("banner_end");
                    bannerList.add(new Banner(id, name, ref, start, end));
                }
                // Banner adapter
                bannerAdapter = new BannerSliderAdapter(getActivity(), bannerList);
                sliderView.setSliderAdapter(bannerAdapter);
            }
        });

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
                            favorAdapter.notifyDataSetChanged();
                        }
                    });
        } else {
            Log.i("(-.-)?", "NO USER");
        }

        /* GAME */
        Query gameQuery = gameRef.orderBy("rating", Query.Direction.DESCENDING).limit(4);
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
                favorAdapter.notifyDataSetChanged();
            }
        });
    }
}
