package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.model.Customer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    private EditText etUsername, etEmail, etPassword, etConfirmPass;
    private Button btnSignUp;
    private ProgressDialog progressDialog;
    private String cusId;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etUsername = (EditText)findViewById(R.id.et_username);
        etEmail = (EditText)findViewById(R.id.et_email);
        etPassword = (EditText)findViewById(R.id.et_password);
        etConfirmPass = (EditText)findViewById(R.id.et_confirm_pass);
        btnSignUp = (Button)findViewById(R.id.btn_sign_up);

        etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        etConfirmPass.setTransformationMethod(PasswordTransformationMethod.getInstance());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);

        //check the user is logged in or not
        if (firebaseAuth.getCurrentUser() != null){
            finish();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        //sign up button for register
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register(){
        final String username = etUsername.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        String confirmPass = etConfirmPass.getText().toString().trim();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "ชื่อผู้ใช้ไม่ถูกต้อง", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "อีเมลไม่ถูกต้อง", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6){
            Toast.makeText(this, "รหัสผ่านไม่ถูกต้อง (รหัสผ่าน 6 ตัวขึ้นไป)", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(confirmPass) || !password.equals(confirmPass)){
            Toast.makeText(this, "ยืนยันรหัสผ่านไม่ถูกต้อง", Toast.LENGTH_LONG).show();
            return;
        }

        //check username
        firebaseFirestore.collection("customers")
                .whereEqualTo("cus_username", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()){
                                //create new user
                                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(RegistrationActivity.this, "สมัครสมาชิกสำเร็จ", Toast.LENGTH_SHORT).show();

                                            //time to register
                                            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                                            //get id of user
                                            cusId = firebaseAuth.getCurrentUser().getUid();

                                            //add user data to database
                                            DocumentReference documentReference = firebaseFirestore.collection("customers").document(cusId);
                                            Map<String,Object> customer = new HashMap<>();
                                            customer.put("cus_password", password);
                                            customer.put("cus_username", username);
                                            customer.put("email", email);
                                            customer.put("last_active_point",timestamp);
                                            customer.put("point", 0);
                                            customer.put("cus_image", "");
//                                            Customer customer = new Customer(
//                                                    username,
//                                                    email,
//                                                    password,
//                                                    0,
//                                                    timestamp,
//                                                    ""
//                                            );

                                            documentReference.set(customer).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "onSuccess: user is created for " + cusId);
                                                }
                                            });

                                            //goto user profile page
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        }else{
                                            progressDialog.dismiss();
                                            Toast.makeText(RegistrationActivity.this, "กรุณาลองอีกครั้ง", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }else {
                                progressDialog.dismiss();
                                Toast.makeText(RegistrationActivity.this, "ชื่อผู้ใช้นี้ผู้ใช้งานแล้ว", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        progressDialog.setMessage("กำลังลงทะเบียน...");
        progressDialog.show();


    }
}
