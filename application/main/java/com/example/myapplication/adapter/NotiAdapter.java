package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Notification;
import com.example.myapplication.model.TimeDisplay;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

public class NotiAdapter extends FirestoreRecyclerAdapter<Notification, NotiAdapter.Holder> {
    private OnItemLongClickListener listener;

    public NotiAdapter(FirestoreRecyclerOptions<Notification> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(Holder holder, int position, Notification model) {
        Picasso.get().load(model.getImage()).into(holder.image);
        holder.title.setText(model.getTitle());
        holder.detail.setText(model.getDetail());

        TimeDisplay timeDisplay = new TimeDisplay(model.getDate());
        holder.time.setText(timeDisplay.getDisplayDate());
    }

    @Override
    public NotiAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotiAdapter.Holder(view);
    }

    public class Holder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView title, detail, time;

        public Holder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title_tv);
            detail = itemView.findViewById(R.id.detail_tv);
            time = itemView.findViewById(R.id.date_tv);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    return true;
                }
            });
        }
    }

    public interface OnItemLongClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnLongItemClickListener(OnItemLongClickListener listener){
        this.listener = listener;
    }
}
