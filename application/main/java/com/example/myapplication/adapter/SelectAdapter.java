package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Game;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SelectAdapter extends RecyclerView.Adapter<SelectAdapter.SelectHolder> {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ItemClickListener listener;
    private Context context;

    private List<Game> gameList;
    private List<String> favList;
    private String bookingId;
    private String collectionPath;

    public SelectAdapter(Context context, String bookingId, List<Game> gameList) {
        this.context = context;
        this.bookingId = bookingId;
        this.gameList = gameList;
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    public void setGameList(List<Game> gameList) {
        this.gameList = gameList;
    }

    @NonNull
    @Override
    public SelectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_select, parent, false);
        SelectHolder holder = new SelectHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SelectHolder holder, int position) {
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

    class SelectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;
        TextView name;
        TextView available;

        public SelectHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.imageGame);
            name = itemView.findViewById(R.id.tvGameName);
            available = itemView.findViewById(R.id.tvAvailable);
            itemView.setOnClickListener(this);
        }

        public void setItem(final int position) {

            Picasso.get().load(gameList.get(position).getImageRef()).into(image);
            name.setText(gameList.get(position).getGameName());
            int numOfAvailable = gameList.get(position).getAvailable();
            available.setText("มีอยู่ " + numOfAvailable);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick(getAdapterPosition());
            }
        }
    }

}
