package com.example.myapplication.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Message;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> {
    public static final String TAG = "TAG";

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser firebaseUser;
    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_right_chat, parent, false);
            MessageAdapter.Holder holder = new MessageAdapter.Holder(view);
            return holder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_left_chat, parent, false);
            MessageAdapter.Holder holder = new MessageAdapter.Holder(view);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.Holder holder, int position) {
        db.collection("customers")
                .document(messageList.get(position).getSender())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String image = documentSnapshot.getString("cus_image");
                        try {
                            Picasso.get().load(image).into(holder.civUserImage);
                        } catch (Exception e) {
                            holder.civUserImage.setImageResource(R.drawable.ic_user_profile);
                            holder.civUserImage.setBorderWidth(1);
                        }
                    }
                });
        Message message = messageList.get(position);
        Log.i(TAG, "onBindViewHolder: " + message.getMessage());
        holder.showMessage.setText(message.getMessage());

    }

    @Override
    public int getItemCount() {
        if (messageList == null) {
            return 0;
        }
        return messageList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private CircleImageView civUserImage;
        private TextView showMessage;

        public Holder(@NonNull View itemView) {
            super(itemView);

            civUserImage = itemView.findViewById(R.id.civUserImage);
            showMessage = itemView.findViewById(R.id.showMessage);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (messageList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }
}
