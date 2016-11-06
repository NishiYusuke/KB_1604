package com.example.arashi.sukureipinngu4;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    TextView res = null;
    String str;
    Handler handler= new Handler();
    String resSt ;

    //timer
    private Runnable updateTimer;
    private long startTime;
    boolean check = true;
    Handler handlerTimer = new Handler();

    //通知
    private int REQUEST_CODE_MAIN_ACTIVITY = 1;
    private int NOTIFICATION_CLICK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        res = (TextView) findViewById(R.id.res);

        search();



    }


    // InputStream -> String
    static String InputStreamToString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    public void search(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    URL url = new URL("https://teamhanshin.mybluemix.net/Door");
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    str = InputStreamToString(con.getInputStream());
                    if(str.indexOf("Open") >= 0){
                        resSt = "Open";
                    }else if(str.indexOf("Lock") >= 0){
                        resSt = "Lock";
                    }else{
                        resSt = "False";
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            res.setText(resSt);
                        }
                    });

                } catch(Exception ex) {
                    System.out.println(ex);
                }
            }
        }).start();

        startTimer();
    }

    //一度呼ばれたら常に実行する関数 wifiとの電波強度で距離を推測
    public void startTimer() {
        // startTimeの取得
        startTime = SystemClock.elapsedRealtime(); // 起動してからの経過時間（ミリ秒）

        updateTimer = new Runnable() {
            @Override
            public void run() {
                long t = SystemClock.elapsedRealtime() - startTime; // ミリ秒
                Log.d("debug",String.valueOf(t));
                if( t > 3*1000){
                    startTime = SystemClock.elapsedRealtime();
                    search();
                }
                handlerTimer.removeCallbacks(updateTimer);
                handlerTimer.postDelayed(updateTimer, 10);
            }
        };
        handlerTimer.postDelayed(updateTimer, 10);
    }


    //通知
    private void sendNotification() {
        // Intent の作成
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                MainActivity.this, REQUEST_CODE_MAIN_ACTIVITY, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // LargeIcon の Bitmap を生成
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

        // NotificationBuilderを作成
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext());
        builder.setContentIntent(contentIntent);
        // ステータスバーに表示されるテキスト
        builder.setTicker("key");
        // アイコン
        builder.setSmallIcon(R.drawable.icon);
        // Notificationを開いたときに表示されるタイトル
        builder.setContentTitle("key");
        // Notificationを開いたときに表示されるサブタイトル
        builder.setContentText("a key is open");
        // Notificationを開いたときに表示されるアイコン
        builder.setLargeIcon(largeIcon);
        // 通知するタイミング
        builder.setWhen(System.currentTimeMillis());
        // 通知時の音・バイブ・ライト
        builder.setDefaults(Notification.DEFAULT_SOUND
                | Notification.DEFAULT_VIBRATE
                | Notification.DEFAULT_LIGHTS);
        // タップするとキャンセル(消える)
        builder.setAutoCancel(true);
        // NotificationManagerを取得
        NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        // Notificationを作成して通知
        manager.notify(NOTIFICATION_CLICK, builder.build());
    }
}
