package com.example.myapplication.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.Holder> {
    public static final String TAG = "TAG";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> customerList;

    public UserAdapter(List<String> customerList) {
        this.customerList = customerList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.setItem(position);
    }

    @Override
    public int getItemCount() {
        if (customerList == null){
            return 0;
        }
        return customerList.size();
    }

    public class Holder extends RecyclerView.ViewHolder{
        private CircleImageView civUserImage;
        private TextView tvUsername;

        public Holder(@NonNull View itemView) {
            super(itemView);

            civUserImage = itemView.findViewById(R.id.civ_user_image);
            tvUsername = itemView.findViewById(R.id.tv_username);
        }

        public void setItem(int position){
            getCustomer(customerList.get(position));
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
                                tvUsername.setText(name);
                                Picasso.get().load(image).into(civUserImage);
                            } catch (Exception e) {
                                civUserImage.setImageResource(R.drawable.ic_user_profile);
                                civUserImage.setBorderWidth(1);
                            }
                        }
                    });
        }
    }
}
