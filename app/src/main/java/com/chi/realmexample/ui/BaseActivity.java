package com.chi.realmexample.ui;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public abstract class BaseActivity extends AppCompatActivity {

    protected void toast(String pMessage) {
        Toast.makeText(getApplicationContext(), pMessage, Toast.LENGTH_SHORT).show();
    }

}
