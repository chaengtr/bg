package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.fragment.GoLoginFragment;
import com.example.myapplication.model.Customer;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "TAG";

    static final int RC_SIGN_IN = 100;

    EditText etEmail, etPassword;
    Button btnSignUp, btnSignIn;
    TextView tvForgotPass;
    SignInButton btnGoogleSignIn;
    ProgressDialog progressDialog;
    ActionBar actionBar;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    GoogleSignInClient mGoogleSignInClient;

    String cusId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        //check the user is logged in or not
        if (firebaseAuth.getCurrentUser() != null){
            finish();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

//        actionBar = getSupportActionBar();
        etEmail = (EditText)findViewById(R.id.et_username);
        etPassword = (EditText)findViewById(R.id.et_password);
        btnSignUp = (Button)findViewById(R.id.btn_sign_up);
        btnSignIn = (Button)findViewById(R.id.btn_sign_in);
        tvForgotPass = (TextView)findViewById(R.id.tv_forgot_pass);
        btnGoogleSignIn = (SignInButton)findViewById(R.id.btn_google_sign_in);

        etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        //sign up button for go to registration page
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivity(intent);
            }
        });

        //sign in button
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        //forgot password? text view for show recover password dialog
        tvForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

        //sign in with google button
        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }


    //create dialog of recover password
    private void showRecoverPasswordDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ลืมรหัสผ่าน");

        LinearLayout linearLayout = new LinearLayout(this);

        final EditText etEmail = new EditText(this);
        etEmail.setHint("อีเมล");
        etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etEmail.setMaxEms(16);

        linearLayout.addView(etEmail);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        builder.setPositiveButton("เปลี่ยนรหัสผ่าน", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = etEmail.getText().toString().trim();
                beginRecover(email);
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

    //send email for reset password
    private void beginRecover(String email){
        progressDialog.setMessage("กรุณารอสักครู่...");
        progressDialog.show();
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "กรุณาตรวจสอบอีเมลของคุณเพื่อเปลี่ยนรหัสผ่าน", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(LoginActivity.this, "การเปลี่ยนรหัสผ่านไม่สำเร็จ...", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: " + e.getMessage());
            }
        });
    }

    //login by email and password that register
    private void loginUser(){
        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "อีเมลไม่ถูกต้อง", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "รหัสผ่านไม่ถูกต้อง", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("กำลังเข้าสู่ระบบ...");
        progressDialog.show();



        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull final Task<AuthResult> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    //Update password changes
                    firebaseFirestore.collection("customers")
                            .whereEqualTo("email",email)
                            .whereEqualTo("cus_password", password)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().isEmpty()) {
                                            cusId = firebaseAuth.getCurrentUser().getUid();
                                            documentReference = firebaseFirestore.collection("customers").document(cusId);
                                            documentReference.update("cus_password",password)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("vac", "Password changed");
                                                        }
                                                    });
                                        }
                                    }
                                }
                            });
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else {
                    Toast.makeText(LoginActivity.this, "อีเมลหรือรหัสผ่านไม่ถูกต้อง", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "" + user.getEmail(),Toast.LENGTH_LONG).show();

                            if (task.getResult().getAdditionalUserInfo().isNewUser()){
                                cusId = firebaseAuth.getCurrentUser().getUid();
                                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                String email = user.getEmail();
                                String username = user.getDisplayName();
//                              int find_at_index =user.getEmail().indexOf("@");
//                              String username = user.getEmail().substring(0,find_at_index);
                                DocumentReference documentReference = firebaseFirestore.collection("customers").document(cusId);
                                Map<String,Object> customer = new HashMap<>();
                                customer.put("cus_username", username);
                                customer.put("email", email);
                                customer.put("last_active_point",timestamp);
                                customer.put("point", 0);
                                customer.put("cus_image", "");
//                                Customer customer = new Customer(
//                                        username,
//                                        email,
//                                        " ",
//                                        0,
//                                        timestamp,
//                                        ""
//                                );

                                //add user data to database
                                documentReference.set(customer).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("vac", "onSuccess: user is created for " + cusId);
                                    }
                                });
                            }

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "การเข้าสู่ระบบไม่สำเร็จ...", Toast.LENGTH_LONG).show();
                            //updateUI(null);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }
        });
    }
}
