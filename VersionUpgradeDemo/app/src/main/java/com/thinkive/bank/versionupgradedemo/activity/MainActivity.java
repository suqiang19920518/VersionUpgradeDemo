package com.thinkive.bank.versionupgradedemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.thinkive.bank.versionupgradedemo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_self :
                startActivity(new Intent(this, SelfActivity.class));
                break;
            case R.id.btn_pgyer:
                startActivity(new Intent(this, PgyerActivity.class));
                break;
            case R.id.btn_others:
                startActivity(new Intent(this, OtherActivity.class));
                break;
        }
    }
}
