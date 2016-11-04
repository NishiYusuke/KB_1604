package com.example.arashi.blinkled;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.WifiManager;

import com.example.arashi.blinkled.AsyncSocket;
import com.example.arashi.blinkled.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,LocationListener {
    //arduinoにアクセス
    private EditText mEditText = null;
    private TextView mText = null;
    private Button mLedOn = null;
    private Button mLedOff = null;


    //gps
//    private LocationManager locationManager;
//    private TextView resText;
//    private int gpsChangeCount = 0;

    //通知
    private int REQUEST_CODE_MAIN_ACTIVITY = 1;
    private int NOTIFICATION_CLICK = 2;

    //rssi
    private TextView rssiText = null;

    //タイマー
    private TextView timerLabel;
    private Handler handler = new Handler();
    private Runnable updateTimer;
    private long startTime;
    boolean check = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText)findViewById(R.id.editText);
        mText = (TextView)findViewById(R.id.result);
        mLedOn = (Button)findViewById(R.id.on);
        mLedOff = (Button)findViewById(R.id.off);

        mLedOn.setOnClickListener(this);
        mLedOff.setOnClickListener(this);

        //gps
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }


        rssiText = (TextView)findViewById(R.id.rssi);


        //常に実行する関数を呼び出す
        startTimer();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.on:
                sendLedOperation("ON");
                Log.v("LED" ,"ON");
                break;
            case R.id.off:
                sendLedOperation("OFF");
                Log.v("LED", "OFF");
                break;
            default:
        }
    }

    private void sendLedOperation(String OnOff) {
//        String editText = mEditText.getText().toString();
        String editText = "192.168.0.17:8080";
        String ip_and_port[] = editText.split(":");
        AsyncSocket socket = new AsyncSocket(ip_and_port[0], ip_and_port[1], mText);
        socket.execute(OnOff);
        socket = null;
    }

    ///////////////////////////////////////// gps /////////////////////////////////////////
    private void locationStart() {
//        Log.d("debug", "locationStart()");
//
//        // LocationManager インスタンス生成
//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        if (!gpsEnabled) {
//            // GPSを設定するように促す
//            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(settingsIntent);
//            Log.d("debug", "gpsEnable, startActivity");
//        } else {
//            Log.d("debug", "gpsEnabled");
//        }
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
//
//            Log.d("debug", "checkSelfPermission false");
//            return;
//        }
//
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50, this);
    }

    // 結果の受け取り
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
        // 緯度の表示
//        TextView textView1 = (TextView) findViewById(R.id.text_view1);
//        textView1.setText("ido:" + location.getLatitude());
//
//        gpsChangeCount++;
//
//
//        // 経度の表示
//        TextView textView2 = (TextView) findViewById(R.id.text_view2);
//        textView2.setText("keido:" + location.getLongitude());
//
//        resText = (TextView) findViewById(R.id.textView);
//        //大学34.727...家34.715997...西村家34.79936　1mあたり緯度 : 0.000008983148616
//        double ido = 34.79936;
//        //大学135.237...家135.233611...西村家135.45109　1mあたり経度 : 0.000010966382364
//        double keido = 135.45109;
//
//        double idoDef = abs((double) location.getLatitude() - ido);
//        double keidoDef = abs((double) location.getLongitude() - keido);
//
//        Log.d("debug", "value: " + idoDef);
//        Log.d("debug", "value: " + keidoDef);
//
//        if (idoDef > 0.00008983148616*5 || keidoDef > 0.00010966382364*5) {
//            resText.setText("out : c " + String.valueOf(gpsChangeCount));
//            arduinoAccess("off");
//        } else {
//            resText.setText("in : c " + String.valueOf(gpsChangeCount));
//            arduinoAccess("on");
//
//
//        }
    }

    //arduinoにアクセス
    public void arduinoAccess(String s){
        switch (s) {
            case "on":
                sendLedOperation("ON");
                Log.v("LED" ,"ON");

                break;
//            case "off":
//                sendLedOperation("OFF");
//                Log.v("LED", "OFF");
//                break;
            default:
        }
    }


    //一度呼ばれたら常に実行する関数
    public void startTimer() {
        // startTimeの取得
        startTime = SystemClock.elapsedRealtime(); // 起動してからの経過時間（ミリ秒）

        updateTimer = new Runnable() {
            @Override
            public void run() {
                long t = SystemClock.elapsedRealtime() - startTime; // ミリ秒
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS", Locale.US);
                Log.d("debug",String.valueOf(t));
                if( t > 2*1000){
                    startTime = SystemClock.elapsedRealtime();
                    WifiManager manager = (WifiManager)getSystemService(WIFI_SERVICE);
                    WifiInfo info = manager.getConnectionInfo();
                    int rssi = info.getRssi();
                    rssiText.setText(String.valueOf(rssi));
                    arduinoAccess("on");
                    String res = mText.getText().toString();
                    if(rssi < -50 &&  res.equals("Open")){
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




    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

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