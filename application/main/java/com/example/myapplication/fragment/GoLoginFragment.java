package com.example.myapplication.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GoLoginFragment extends Fragment {
    private Button btnGoSign;


    public GoLoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_go_login, container, false);

        btnGoSign = (Button) view.findViewById(R.id.btn_go_sign);

        btnGoSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

}
