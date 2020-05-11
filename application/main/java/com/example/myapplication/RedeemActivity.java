package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;

public class RedeemActivity extends AppCompatActivity {
    TextView tvProName, tvProDetail, tvProStart, tvProEnd, tvCusPt, tvRedeemPt;
    Button btnRedeem;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    DocumentReference proRef, cusRef, bookRef;

    String cusId = "", usageStatus, id, proUse, bookingCode;
    boolean bookingStatus = false;
    int cusPoint = 0, proPoint = 0, totalPoint = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem);

        tvProName = (TextView)findViewById(R.id.tv_pro_name);
        tvProDetail = (TextView)findViewById(R.id.tv_pro_detail);
        tvProStart = (TextView)findViewById(R.id.tv_pro_start);
        tvProEnd = (TextView)findViewById(R.id.tv_pro_end);
        tvCusPt = (TextView)findViewById(R.id.tv_cus_pt);
        tvRedeemPt = (TextView)findViewById(R.id.tv_redeem_pt);
        btnRedeem = (Button)findViewById(R.id.btn_redeem);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        proRef = firebaseFirestore.collection("promotions").document(id);
        cusId = firebaseAuth.getCurrentUser().getUid();
        cusRef = firebaseFirestore.collection("customers").document(cusId);

        proRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    String name = document.getString("pro_name");
                    String detail = document.getString("pro_detail");
                    String start = new SimpleDateFormat("yyyy.MM.dd").format(document.getTimestamp("pro_start").toDate());
                    String end = new SimpleDateFormat("yyyy.MM.dd").format(document.getTimestamp("pro_end").toDate());
                    String point = document.getLong("pro_point").intValue() + "";
                    proPoint = document.getLong("pro_point").intValue();

                    tvProName.setText(name);
                    tvProDetail.setText(detail);
                    tvProStart.setText(start);
                    tvProEnd.setText(end);
                    tvRedeemPt.setText(point);
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });

        cusRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    String point = document.get("point") + "";
                    cusPoint = document.getLong("point").intValue();
                    tvCusPt.setText(point);

                    bookingCode = document.getString("booking_code");
                    bookRef = firebaseFirestore.collection("BOOKING/" + bookingCode + "/customers").document(cusId);
                    bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                bookingStatus = true;
                                usageStatus = doc.getString("usage_status");
                                proUse = doc.getString("promotion_id");
                            }
                        }
                    });
                }else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });

        btnRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookingStatus) {
                    if (!proUse.isEmpty()) {
                        showRejectDialog("ไม่สามารถใช้โปรโมชันได้", "คุณใช้คะแนนแลกสิทธิกับการจองนี้แล้ว");
                    } else if (!usageStatus.equals("เสร็จสิ้นการจอง")) {
                        showRejectDialog("ไม่สามารถใช้โปรโมชันได้", "คะแนนไม่พอ หรือเงื่อนไขการใช้งานไม่ถูกต้อง กรุณาติดต่อพนักงาน");
                    } else if (cusPoint >= proPoint) {
                        showConfirmDialog();
                    } else {
                        showRejectDialog("ไม่สามารถแลกคะแนนได้", "คะแนนของคุณไม่เพียงพอ");
                    }
                } else {
                    showRejectDialog("ไม่สามารถใช้โปรโมชันได้","คุณยังไม่ได้จองเกม");
                }
            }
        });
    }

    private void redeemPoint(){
        totalPoint = cusPoint - proPoint;
        cusRef.update("point", totalPoint).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent = new Intent(getApplicationContext(), PromotionActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });
    }

    private void updateBooking() {
        bookRef.update("promotion_id", id).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("(-.-)?", "Update booking (promotion_id)");
            }
        });
    }

    private void showConfirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("แลกคะแนน");

        LinearLayout linearLayout = new LinearLayout(this);
        TextView tvTotalPt = new TextView(this);
        tvTotalPt.setText("คุณต้องการแลก " +proPoint+" คะแนน");

        linearLayout.addView(tvTotalPt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        builder.setPositiveButton("ยืนยัน", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateBooking();
                redeemPoint();
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

    private void showRejectDialog(String title, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        LinearLayout linearLayout = new LinearLayout(this);
        TextView tvTotalPt = new TextView(this);
        tvTotalPt.setText(text);

        linearLayout.addView(tvTotalPt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        builder.setNegativeButton("ตกลง", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}
