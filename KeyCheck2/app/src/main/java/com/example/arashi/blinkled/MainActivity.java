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

//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.select.Elements;


import com.example.arashi.blinkled.AsyncSocket;
import com.example.arashi.blinkled.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import static android.content.Context.WIFI_SERVICE;
import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,LocationListener{
    //arduino
    private TextView mText = null; //arduinoからの信号を表示
    String str;
    Handler handler= new Handler();
    String resSt ;

    //通知
    private int REQUEST_CODE_MAIN_ACTIVITY = 1;
    private int NOTIFICATION_CLICK = 2;

    //タイマー
    private Runnable updateTimer;
    private long startTime;
    boolean check = true;
    Handler handlerTimer = new Handler();

    //gps
    private LocationManager locationManager;
    private TextView resText;
    private int gpsChangeCount = 0;

    //履歴
    public ArrayList<String> items = new ArrayList<>();
    ListView myListView;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = (TextView)findViewById(R.id.result);

        resText = (TextView) findViewById(R.id.textView);

        myListView = (ListView) findViewById(R.id.myListView);

        adapter = new ArrayAdapter<String>(
                this,
                R.layout.list_item,
                items
        );


        // ListViewに表示
        myListView.setAdapter(adapter);

        //defaultの履歴データを用意しとく
        items.add(0,"Open : 11月01日10:10:30");
        items.add(0,"Lock : 11月01日10:10:43");
        items.add(0,"Open : 11月02日13:22:25");
        items.add(0,"Lock : 11月02日13:22:32");
        items.add(0,"Open : 11月02日18:56:56");
        items.add(0,"Lock : 11月02日18:57:01");
        items.add(0,"Open : 11月03日07:34:11");
        items.add(0,"Lock : 11月03日07:34:19");

        //gps
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }
        startTimer();

    }



    @Override
    public void onClick(View v) {
    }

    //履歴に追加
    public void addList(String res){
        Date date = new Date();
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM'月'dd'日'HH:mm:ss");
        String datast = sdf1.format(date);
        items.add(0,res + " : "+datast);
    }

    //ローカルでarduinoと通信（いまは使用していない）
    private void sendLedOperation(String OnOff) {
        String editText = "192.168.10.1:8080";
        String ip_and_port[] = editText.split(":");
        AsyncSocket socket = new AsyncSocket(ip_and_port[0], ip_and_port[1], mText);
        socket.execute(OnOff);
        socket = null;
    }

    //arduinoにアクセス（いまは使用していない）
    public void arduinoAccess(String s){
        sendLedOperation("ON");
    }

    //webから情報をとってくる
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
                            if(items.get(0).indexOf(resSt) < 0) {
                                mText.setText(resSt);
                                addList(resSt);
                            }
                        }
                    });

                } catch(Exception ex) {
                    System.out.println(ex);
                }
            }
        }).start();

        startTimer();
    }

    // InputStreamをStringに変換
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

    //一度呼ばれたら常に実行する関数
    public void startTimer() {
        // startTimeの取得
        startTime = SystemClock.elapsedRealtime();

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
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                MainActivity.this, REQUEST_CODE_MAIN_ACTIVITY, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext());
        builder.setContentIntent(contentIntent);
        // ステータスバーに表示されるテキスト
        builder.setTicker("key");
        // アイコン
        builder.setSmallIcon(R.drawable.icon);
        // 表示されるタイトル
        builder.setContentTitle("key");
        // 表示されるサブタイトル
        builder.setContentText("a key is open");
        // 表示されるアイコン
        builder.setLargeIcon(largeIcon);
        // 通知するタイミング
        builder.setWhen(System.currentTimeMillis());
        // 通知時の音・バイブ・ライト
        builder.setDefaults(Notification.DEFAULT_SOUND
                | Notification.DEFAULT_VIBRATE
                | Notification.DEFAULT_LIGHTS);
        // タップするとキャンセル
        builder.setAutoCancel(true);
        // NotificationManagerを取得
        NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        // 通知
        manager.notify(NOTIFICATION_CLICK, builder.build());
    }

    ///////////////////////////////////////// gps relate/////////////////////////////////////////
    private void locationStart() {
        Log.d("debug", "locationStart()");

        // LocationManager インスタンス生成
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            // GPSを設定するように促す
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
            Log.d("debug", "gpsEnable, startActivity");
        } else {
            Log.d("debug", "gpsEnabled");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);

            Log.d("debug", "checkSelfPermission false");
            return;
        }
//        sendNotification();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("debug", "checkSelfPermission true");

                locationStart();
                return;

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.d("debug", "LocationProvider.AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                break;
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        sendNotification();
        //gpsが変化した回数
        gpsChangeCount++;
        //wifiモジュール、鍵のある場所
        double ido = 34.691192;
        double keido = 135.192088;
        //現在地とのgpsの差を計算
        double idoDef = abs((double) location.getLatitude() - ido);
        double keidoDef = abs((double) location.getLongitude() - keido);
        //何m鍵があいた状態で移動した時通知をするか
        int defMeter = 10;
        //通知をするかしないか判断
        if (idoDef > 0.00008983148616*defMeter || keidoDef > 0.00010966382364*defMeter) {
            resText.setText("out");
            if(items.get(0).indexOf("Open") >= 0) {
                sendNotification();
            }
        } else {
            resText.setText("in");
        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
