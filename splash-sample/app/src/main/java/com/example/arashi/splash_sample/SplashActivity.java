package com.example.arashi.splash_sample;

/**
 * Created by arashi on 2016/11/16.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        Handler handler = new Handler();
        handler.postDelayed(new splashHandler(), 2000);
    }

    class splashHandler implements Runnable {
        public void run() {
            Intent inte = new Intent(getApplication(), MainActivity.class);
            startActivity(inte);
            SplashActivity.this.finish();
        }
    }
}