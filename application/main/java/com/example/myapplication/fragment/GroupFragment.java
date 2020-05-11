package com.example.myapplication.fragment;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.BookingActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.SelectGameActivity;
import com.example.myapplication.UsersActivity;
import com.example.myapplication.adapter.GroupAdapter;
import com.example.myapplication.model.Booking;
import com.example.myapplication.model.Notification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.threeten.bp.LocalTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getUid();

    private String bookingCode;
    private boolean booking = false;
    private boolean isGroup = false;
    private List<Booking> bookingList = new ArrayList<>();

    private GroupAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private ImageView chat;

    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        fabAdd = view.findViewById(R.id.fab_add);
        chat = view.findViewById(R.id.chat);

        adapter = new GroupAdapter(bookingList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        isBooked(); // ตรวจสอบว่ามีการจองอยู่หรือไม่
        getBooking();
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (booking) {
                    Toast.makeText(getContext(), "ไม่สามารถจองเกมซ้ำได้", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), SelectGameActivity.class);
                    intent.putExtra("group", "true");
                    startActivity(intent);
                }
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("ii", "onClick: " + booking + isGroup);
                if (booking && isGroup) {
                    Intent intent = new Intent(getActivity(), UsersActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(getContext(), "ต้องทำการจองแบบกลุ่ม หรือเข้าร่วมกลุ่มก่อน", Toast.LENGTH_SHORT).show();
                }
            }
        });

        adapter.setItemClickListener(new GroupAdapter.ItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                db.collection("BOOKING").document(bookingList.get(position).getBookingCode()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        double max_mem = documentSnapshot.getDouble("member_max");
                        Booking booking2 = bookingList.get(position);
                        if (booking2.getMember() >= max_mem){
                            Toast.makeText(getContext(),"จำนวนสมาชิกเต็มแล้ว ไม่สามารถเข้าร่วมได้", Toast.LENGTH_SHORT).show();
                        }else if (booking){
                            Toast.makeText(getContext(), "ไม่สามารถจองเกมซ้ำได้", Toast.LENGTH_SHORT).show();
                        }else {
                            showRequestDialog(position);
                        }
                    }
                });
            }
        });

        return view;
    }

    private void isBooked() {
        db.collection("customers").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    bookingCode = doc.getString("booking_code");
                    if (bookingCode != null) {
                        booking = true;
                        db.collection("BOOKING").document(bookingCode).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot doc = task.getResult();
                                    String group = doc.getString("group");
                                    if (group.equals("yes")){
                                        isGroup = true;
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });


    }

    private void getBooking() {
        db.collection("BOOKING").whereEqualTo("group", "yes").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    final String code = doc.getId();
                    final Date start = doc.getDate("booking_start");
                    final String game = doc.getString("game_id");
                    final String group = doc.getString("group");
                    final double max = doc.getDouble("member_max");

                    final List<String>  customers = new ArrayList<>();
                    CollectionReference ref = db.collection("BOOKING/" + code + "/customers");
                    ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                customers.add(doc.getId());
                            }

                            Date date = new Date();
                            if (date.before(start)) {
                                bookingList.add(new Booking(code, start, game, group, (int) max, customers));
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    private void showRequestDialog(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("เข้าร่วม");

        LinearLayout linearLayout = new LinearLayout(getActivity());
        TextView tvTotalPt = new TextView(getActivity());
        tvTotalPt.setText("คุณต้องการเข้าร่วมกลุ่ม");

        linearLayout.addView(tvTotalPt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        builder.setPositiveButton("ยืนยัน", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Map<String, Object> cus = new HashMap<>();
                cus.put("receipt_status", "ยังไม่ออกใบเสร็จ");
                cus.put("usage_status", "จอง");
                cus.put("promotion_id", "");
                db.collection("BOOKING/" + bookingList.get(position).getBookingCode() + "/customers").document(userId).set(cus).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("(-.-)?", "ADD booking (customers)");
                    }
                });
                db.collection("customers").document(userId).update("booking_code", bookingList.get(position).getBookingCode()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("(-.-)?", "ADD customers (booking_status)" + bookingList.get(position).getBookingCode());
                    }
                });

                // Notification
                String title = "จองสำเร็จ";
                String detail = "รหัสจองของคุณคือ \"" + bookingList.get(position).getBookingCode() + "\"";
                String image = getIcon("accept");
                sendNotification(new Notification(new Date(), detail, image, title));

                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private String getIcon(String icon) {
        if (icon.equals("accept")) {
            return "https://www.flaticon.com/premium-icon/icons/svg/2550/2550322.svg";
        } else if (icon.equals("cancel")) {
            return "https://www.flaticon.com/premium-icon/icons/svg/2550/2550327.svg";
        }
        return "";
    }

    private void sendNotification(Notification notification) {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "channel_id")
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getDetail())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_noti)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        notificationManager.notify(0, builder.build());

        addNotification(notification);
    }

    private void addNotification(Notification notification) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();
        String collectionPath = "notification/" + userId + "/notification_list";

        database.collection(collectionPath).add(notification).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.i("(-.-)?", "ADD notification");
            }
        });
    }
}
