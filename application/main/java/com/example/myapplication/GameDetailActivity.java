package com.example.myapplication;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.CommentAdapter;
import com.example.myapplication.model.Comment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class GameDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private CollectionReference commentRef;

    private CommentAdapter adapter;

    private List<Comment> commentList = new ArrayList<>();
    private List<SliceValue> pieData = new ArrayList<>();
    private String collectionPath;
    private String gameId, userId, userComment;
    private double userRating, sumRating, averageRating;
    private int commentAmount;

    private ImageView imageGame;
    private RatingBar ratingBar;
    private TextView tvGameName, tvGameDetail, tvRatingHead, tvCommentHead, tvComment;
    private RecyclerView commentRV;
    private PieChartView pieChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        imageGame = findViewById(R.id.game_image);
        ratingBar = findViewById(R.id.rating_bar);
        tvGameName = findViewById(R.id.game_name_tv);
        tvGameDetail = findViewById(R.id.game_detail_tv);
        tvRatingHead = findViewById(R.id.rating_head_tv);
        tvCommentHead = findViewById(R.id.comment_head_tv);
        tvComment = findViewById(R.id.comment_tv);
        commentRV = findViewById(R.id.comment_rv);
        pieChartView = findViewById(R.id.chart);

        // Get game detail
        gameId = getIntent().getStringExtra("gameId");
        Picasso.get().load(getIntent().getStringExtra("image")).into(imageGame);
        tvGameName.setText(getIntent().getStringExtra("gameName"));
        tvGameDetail.setText(getIntent().getStringExtra("gameDetail"));

        // Database
        collectionPath = "comment/" + gameId + "/comment_list";
        commentRef = db.collection(collectionPath);
        getDataFromDB();

        adapter = new CommentAdapter(commentList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        commentRV.setLayoutManager(layoutManager);
        commentRV.setAdapter(adapter);
    }

    public void sendComment(View view) {
        String comment = tvComment.getText().toString().trim();
        userRating = ratingBar.getRating();
        String message;
        if (user != null) {
            addComment(comment);
            getDataFromDB();
            message = "ส่งความคิดเห็นสำเร็จ";
        } else {
            message = "กรุณาลงชื่อเข้าใช้งาน";
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void removeComment(View view) {
        if (user != null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("ยืนยัน");
            alert.setMessage("กรุณาเลือก \"ยืนยัน\" เพื่อลบความคิดเห็น");
            alert.setNegativeButton("ยกเลิก", null);
            alert.setPositiveButton("ยืนยัน", new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteComment();
                    getDataFromDB();
                    Toast.makeText(GameDetailActivity.this,
                            "ลบความคิดเห็นสำเร็จ", Toast.LENGTH_SHORT).show();
                }
            });
            alert.show();
        } else {
            Toast.makeText(GameDetailActivity.this,
                    "กรุณาลงชื่อเข้าใช้งาน", Toast.LENGTH_SHORT).show();
        }
    }

    /* DATABASE */
    private void getDataFromDB() {
        getUserComment();
        getAllComment();
    }

    private void getUserComment() {
        // Get user comment
        if (user != null) {
            userId = user.getUid();
            db.collection(collectionPath)
                    .document(userId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.getData() != null) {
                                userRating = documentSnapshot.getDouble("rating");
                                userComment = documentSnapshot.getString("comment");
                            } else {
                                userRating = 0;
                                userComment = "";
                            }
                            ratingBar.setRating((float) userRating);
                            tvComment.setText(userComment);
                            Log.i("(-.-)?", "GET user rating: " + userRating);
                        }
                    });
        }
    }

    private void getAllComment() {
        commentList.clear();
        sumRating = 0;
        Query commentQuery = commentRef.orderBy("time", Query.Direction.DESCENDING);
        commentQuery.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        commentAmount = queryDocumentSnapshots.size();
                        tvRatingHead.setText("คะแนนความนิยม (" + commentAmount + ")");
                        tvCommentHead.setText("ความคิดเห็น (" + commentAmount + ")");

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String id = doc.getId();
                            String comment = doc.getString("comment");
                            double rating = doc.getDouble("rating");
                            Date date = doc.getDate("time");

                            sumRating += rating;
                            commentList.add(new Comment(id, comment, rating, date));
                        }

                        if (commentAmount != 0) {
                            averageRating = sumRating / commentAmount;
                        } else {
                            averageRating = 0;
                        }

                        updateRatingToGame();
                        setPieChart();
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void updateRatingToGame() {
        db.collection("games")
                .document(gameId)
                .update("rating", averageRating)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("(-.-)?", "Update rating: " + averageRating);
                    }
                });
    }

    private void setPieChart() {
        pieData.clear();
        double percentRating = averageRating / 5 * 100;
        double noPercentRating = 100 - percentRating;
        pieData.add(new SliceValue((float) percentRating, Color.parseColor("#FFA200")));
        pieData.add(new SliceValue((float) noPercentRating, Color.parseColor("#909090")));

        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasCenterCircle(true)
                .setCenterText1(String.format("%.1f", averageRating))
                .setCenterText1FontSize(20)
                .setCenterText2("คะแนน");
        pieChartView.setPieChartData(pieChartData);
    }

    private void addComment(String comment) {
        Map<String, Object> data = new HashMap<>();
        Date date = new Date();
        data.put("comment", comment);
        data.put("rating", userRating);
        data.put("time", date);

        db.collection(collectionPath)
                .document(userId)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("(-.-)?", "ADD comment: " + userId);
                    }
                });
    }

    private void deleteComment() {
        db.collection(collectionPath)
                .document(userId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("(-.-)?", "DELETE comment: " + userId);
                    }
                });
    }
}
