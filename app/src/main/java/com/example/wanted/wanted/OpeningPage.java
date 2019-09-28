package com.example.wanted.wanted;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class OpeningPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opening_page);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run()
            {
                Intent intent = new Intent(OpeningPage.this, PhoneAuth.class);
                startActivity(intent);
                finish();

            }
        }, 1000);   //5 seconds


    }
}
