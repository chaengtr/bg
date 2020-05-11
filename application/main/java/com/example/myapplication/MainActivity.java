package com.example.myapplication;

//C:\Users\ACER\AppData\Local\Android\Sdk\emulator>\emulator.exe -avd Nexus_5X_API_29 -dns-server 8.8.8.8

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.example.myapplication.fragment.GoLoginFragment;
import com.example.myapplication.fragment.GroupFragment;
import com.example.myapplication.fragment.HomeFragment;
import com.example.myapplication.fragment.NotiFragment;
import com.example.myapplication.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.nio.channels.Channel;

public class MainActivity extends AppCompatActivity {
    ActionBar actionBar;
    boolean status = false;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        actionBar = getSupportActionBar();

        BottomNavigationView navigationView = (BottomNavigationView)findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        if (firebaseAuth.getCurrentUser() != null){
            status = true;
        }

        loadFragment(new HomeFragment());
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "name";
            String description = "description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private boolean loadFragment(Fragment fragment){
        if (fragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment fragment = null;

                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            actionBar.setTitle(R.string.app_name);
                            fragment = new HomeFragment();
                            break;
                        case R.id.nav_group:
                            actionBar.setTitle("หาเพื่อน");
                            if (status == false) {
                                fragment = new GoLoginFragment();
                            } else {
                                fragment = new GroupFragment();
                            }
                            break;
                        case R.id.nav_noti:
                            actionBar.setTitle("แจ้งเตือน");
                            if (status == false) {
                                fragment = new GoLoginFragment();
                            } else {
                                fragment = new NotiFragment();
                            }
                            break;
                        case R.id.nav_profile:
                            actionBar.setTitle("ข้อมูลส่วนตัว");
                            if (status == false) {
                                fragment = new GoLoginFragment();
                            } else{
                                fragment = new ProfileFragment();
                            }
                            break;
                    }
                    return loadFragment(fragment);
                }
            };

}
