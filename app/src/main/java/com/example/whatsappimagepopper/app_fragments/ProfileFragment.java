package com.example.whatsappimagepopper.app_fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.whatsappimagepopper.R;

import java.security.PublicKey;

public class ProfileFragment extends Fragment {
    public View this_fragment;


    public ProfileFragment() { }  // Required empty public constructor

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this_fragment = inflater.inflate(R.layout.fragment_profile, container, false);

        Button btn_signup = this_fragment.findViewById(R.id.signup_btn);
        btn_signup.setOnClickListener((View v)->{
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolder, new SignupFragment()).commit();
        });

        Button btn_login = this_fragment.findViewById(R.id.login_btn);
        btn_login.setOnClickListener((View v)->{
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolder, new LoginFragment()).commit();
        });
        return this_fragment;
    }
}