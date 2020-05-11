package com.example.myapplication.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Game;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavorAdapter extends RecyclerView.Adapter<FavorAdapter.Holder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ItemClickListener listener;
    private Context context;

    private List<Game> gameList;
    private List<String> favList;
    private String cusId;
    private String collectionPath;

    public FavorAdapter(Context context, String cusId, List<Game> gameList, List<String> favList) {
        this.context = context;
        this.cusId = cusId;
        this.gameList = gameList;
        this.favList = favList;
        collectionPath = "favor/" + cusId + "/fav_list";
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    public void setGameList(List<Game> gameList) {
        this.gameList = gameList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_fav, parent, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.setItem(position);
    }

    @Override
    public int getItemCount() {
        if (gameList == null) {
            return 0;
        }
        return gameList.size();
    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }

    class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image, fav;
        RatingBar ratingBar;
        TextView name, rate;

        public Holder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.game_image);
            fav = itemView.findViewById(R.id.fav);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            name = itemView.findViewById(R.id.game_name_tv);
            rate = itemView.findViewById(R.id.tvRating);
            itemView.setOnClickListener(this);
        }

        public void setItem(final int position) {
            double rating = gameList.get(position).getRating();
            ratingBar.setRating((float) rating);

            Picasso.get().load(gameList.get(position).getImageRef()).into(image);
            name.setText(gameList.get(position).getGameName());
            getComment();

            final boolean[] isFav = {false};
            if (favList.contains(gameList.get(position).getGameId())) {
                isFav[0] = true;
            }
            if (isFav[0]) {
                fav.setImageResource(R.drawable.ic_favorite);
            } else {
                fav.setImageResource(R.drawable.ic_favorite_border);
            }

            fav.setOnClickListener(new View.OnClickListener() {
                String id = gameList.get(position).getGameId();
                String name = gameList.get(position).getGameName();
                @Override
                public void onClick(View v) {
                    String message = "";
                    isFav[0] = !isFav[0];
                    if (isFav[0]) {
                        if (cusId != null) {
                            addGameFavor(id);
                        }
                        fav.setImageResource(R.drawable.ic_favorite);
                        message = String.format("ถูกใจ \"%s\"", name);
                    } else {
                        if (cusId != null) {
                            removeGameFavor(gameList.get(position).getGameId());
                        }
                        fav.setImageResource(R.drawable.ic_favorite_border);
                        message = String.format("ยกเลิกถูกใจ \"%s\"", name);
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick(getAdapterPosition());
            }
        }

        /* DATABASE */
        private void addGameFavor(final String gameId) {
            Map<String, Object> data = new HashMap<>();
            Date date = new Date();
            data.put("game_id", gameId);
            data.put("date", date);

            db.collection(collectionPath)
                    .document(gameId)
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i("(-.-)?", "ADD favor: " + gameId);
                        }
                    });
        }

        private void removeGameFavor(final String gameId) {
            db.collection(collectionPath)
                    .document(gameId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i("(-.-)?", "DELETE favor: " + gameId);
                        }
                    });
        }

        private void getComment() {
            final Game game = gameList.get(getAdapterPosition());
            db.collection("comment/" + game.getGameId() + "/comment_list")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            int number = 0;
                            if (!queryDocumentSnapshots.isEmpty()) {
                                number = queryDocumentSnapshots.size();
                            }
                            rate.setText(String.format("%.1f (%d)", game.getRating(), number));
                        }
                    });
        }
    }
}
