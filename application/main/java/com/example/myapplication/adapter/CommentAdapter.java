package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Comment;
import com.example.myapplication.model.TimeDisplay;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.Holder> {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        CommentAdapter.Holder holder = new CommentAdapter.Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.setItem(position);
    }

    @Override
    public int getItemCount() {
        if (commentList == null) {
            return 0;
        }
        return commentList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private CircleImageView userImage;
        private RatingBar ratingBar;
        private TextView username, time, comment;

        public Holder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.user_image);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            username = itemView.findViewById(R.id.username_tv);
            time = itemView.findViewById(R.id.time_tv);
            comment = itemView.findViewById(R.id.comment_tv);
        }

        public void setItem(int position) {
            double rating = commentList.get(position).getRating();
            Date date = commentList.get(position).getTime();

            ratingBar.setRating((float) rating);
            comment.setText(commentList.get(position).getComment());
            getCustomer(commentList.get(position).getCusId());

            TimeDisplay timeDisplay = new TimeDisplay(date);
            time.setText(timeDisplay.getDisplayDate());
        }

        /* DATABASE */
        private void getCustomer(String cusId) {
            db.collection("customers")
                    .document(cusId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String name = documentSnapshot.getString("cus_username");
                            String image = documentSnapshot.getString("cus_image");

                            try {
                                username.setText(name);
                                Picasso.get().load(image).into(userImage);
                            } catch (Exception e) {
                                userImage.setImageResource(R.drawable.ic_user_profile);
                                userImage.setBorderWidth(1);
                            }
                        }
                    });
        }
    }
}
