package com.example.arashi.blinkled;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.WifiManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import com.example.arashi.blinkled.AsyncSocket;
import com.example.arashi.blinkled.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import static android.content.Context.WIFI_SERVICE;
import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //arduinoにアクセス
    private TextView mText = null; //arduinoからの信号を表示

//    private Button button1 = null;

    //通知
    private int REQUEST_CODE_MAIN_ACTIVITY = 1;
    private int NOTIFICATION_CLICK = 2;

    //rssi
    private TextView rssiText = null;

    //タイマー
    private Handler handler = new Handler();
    private Runnable updateTimer;
    private long startTime;
    boolean check = true;

    //履歴
    public ArrayList<String> items = new ArrayList<>();
    ListView myListView;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = (TextView)findViewById(R.id.result);

        rssiText = (TextView)findViewById(R.id.rssi);

        myListView = (ListView) findViewById(R.id.myListView);

        // Adapter - ArrayAdapter
        adapter = new ArrayAdapter<String>(
                this,
                R.layout.list_item,
                items
        );


        // ListViewに表示
        myListView.setAdapter(adapter);

//        button1 = (Button)findViewById(R.id.button1);
//        button1.setOnClickListener(this);

        //defaultのlistデータ
        items.add(0,"open : 11月01日10:10:00");
        items.add(0,"close : 11月01日10:11:00");
        items.add(0,"open : 11月01日13:10:00");
        items.add(0,"close : 11月01日13:12:00");
        items.add(0,"open : 11月01日10:10:00");
        items.add(0,"close : 11月01日10:11:00");
        items.add(0,"open : 11月01日13:10:00");
        items.add(0,"close : 11月01日13:12:00");


        startTimer();

    }



    @Override
    public void onClick(View v) {
//        Date date = new Date();
//        SimpleDateFormat sdf1 = new SimpleDateFormat("MM'月'dd'日'HH:mm:ss");
//
//        String datast = sdf1.format(date);
//        switch (v.getId()) {
//            case R.id.button1:
//                items.add(0,"check : "+datast);
//                break;
//
//            default:
//        }

        myListView.setAdapter(adapter);

    }

    public void addList(String res){
        Date date = new Date();
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM'月'dd'日'HH:mm:ss");
        String datast = sdf1.format(date);
        items.add(0,res + " : "+datast);
    }

    private void sendLedOperation(String OnOff) {
        String editText = "192.168.10.1:8080";
        String ip_and_port[] = editText.split(":");
        AsyncSocket socket = new AsyncSocket(ip_and_port[0], ip_and_port[1], mText);
        socket.execute(OnOff);
        socket = null;
    }


    //arduinoにアクセス
    public void arduinoAccess(String s){
        sendLedOperation("ON");
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
                    WifiManager manager = (WifiManager)getSystemService(WIFI_SERVICE);
                    WifiInfo info = manager.getConnectionInfo();
                    int rssi = info.getRssi();

                    //arduinoの応答から履歴を作成
                    arduinoAccess("on");
                    String res = mText.getText().toString();
                    int i = items.get(0).indexOf(res);
                    //履歴に追加
                    if(items.get(0).indexOf(res) < 0){
                        addList(res);
                    }

                    rssiText.setText(String.valueOf(rssi) + " : " + String.valueOf(items.size()));

                    if(rssi < -60 ){ //&&  res.equals("Open")
                        if(check){
                            sendNotification();
                            check = false;
                        }
                    }else{
                        check = true;
                    }

                }
                handler.removeCallbacks(updateTimer);
                handler.postDelayed(updateTimer, 10);
            }
        };
        handler.postDelayed(updateTimer, 10);
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
