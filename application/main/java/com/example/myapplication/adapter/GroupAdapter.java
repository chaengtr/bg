package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Booking;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.Holder> {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Booking> bookingList;

    private ItemClickListener listener;

    public GroupAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_select, parent, false);
        GroupAdapter.Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.setItem(position);
    }

    @Override
    public int getItemCount() {
        if (bookingList == null) {
            return 0;
        }
        return bookingList.size();
    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView image;
        TextView name;
        TextView member;

        public Holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageGame);
            name = itemView.findViewById(R.id.tvGameName);
            member = itemView.findViewById(R.id.tvAvailable);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick(getAdapterPosition());
            }
        }

        private void setItem(final int position) {
            final Booking booking = bookingList.get(position);
            final String gameId = booking.getGameId();
            db.collection("games").document(gameId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot doc) {
                    String game = doc.getString("game_name");
                    double max = doc.getDouble("player_max");
                    String ref = doc.getString("image_ref");

                    Picasso.get().load(ref).into(image);
                    name.setText(game);
                    member.setText("("+ booking.getMember() +"/" + (int) max + ")");
                }
            });
        }
    }
}
