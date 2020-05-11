package com.example.myapplication.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Promotion;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class PromotionAdapter extends FirestoreRecyclerAdapter<Promotion, PromotionAdapter.PromotionHolder> {
    private  OnItemClickListener listener;

    public PromotionAdapter(@NonNull FirestoreRecyclerOptions<Promotion> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull PromotionHolder holder, int position, @NonNull Promotion model) {
//        holder.tvProName.setText(model.getProName());
        String proName = getSnapshots().getSnapshot(position).getString("pro_name");
        holder.tvProName.setText(proName);

    }

    @NonNull
    @Override
    public PromotionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pro, parent, false);
        return new PromotionHolder(v);
    }

    class PromotionHolder extends RecyclerView.ViewHolder{
        TextView tvProName;

        public PromotionHolder(final View itemView) {
            super(itemView);

            tvProName = (TextView)itemView.findViewById(R.id.tv_pro_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
