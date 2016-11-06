package com.example.arashi.thereadhandler;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Handler handler = new Handler();
    private TextView text;
    private ScheduledExecutorService srv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.text = (TextView) findViewById(R.id.date_field);
    }

    public void onClickStart(View v) {

        srv = Executors.newSingleThreadScheduledExecutor();
        srv.scheduleAtFixedRate(new Runnable() {
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        text.setText("Got Data:" + new Date().toString());
                    }
                });
            }
        },  0, 500, TimeUnit.MILLISECONDS);
    }

    public void onClickToast(View v) {
        Toast.makeText(this, "テキスト", Toast.LENGTH_LONG).show();
    }
}
