#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <TimeLib.h>//timeライブラリver1.4の場合
  
const char* ssid = "HUMAX-EFCDD";
const char* password = "djFmXahmMTX4X";
  
boolean SSE_on = false;//Server-Sent Events設定が済んだかどうかのフラグ
  
WiFiServer server(80);
 
WiFiClient client;
  
//-------NTPサーバー定義----------------
unsigned int localPort = 2390;      // local port to listen for UDP packets
//IPAddress timeServer(129, 6, 15, 28); // time.nist.gov NTP server
IPAddress timeServerIP; // time.nist.gov NTP server address
const char* ntpServerName = "time.nist.gov";
const int NTP_PACKET_SIZE = 48; // NTP time stamp is in the first 48 bytes of the message
byte packetBuffer[ NTP_PACKET_SIZE]; //buffer to hold incoming and outgoing packets
WiFiUDP udp;
  
long LastTime = 0;
  
//*****************セットアップ**********************
void setup() {
  Serial.begin(115200);//このシリアル通信はモニター用
  delay(10);
  
  // Connect to WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
    
  WiFi.begin(ssid, password);
    
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");
    
  // Start the server
  server.begin();
  Serial.println("Server started");
  
  // Print the IP address
  Serial.println(WiFi.localIP());
  
  //NTPサーバーでタイムを取得
  udp.begin(localPort);
  WiFi.hostByName(ntpServerName, timeServerIP); 
  setSyncProvider(getNtpTime);
  delay(3000);
}
  
//メインループ***********************************************
void loop() {
  HTTP_Responce();
  yield();//これは重要！！これがないと動作しない。
}
  
//**********************Server-Sent Events レスポンス関数**************
void HTTP_Responce()
{
  client = server.available();//クライアント生成は各関数内でしか実行できないので注意
  while(client.status()!=CLOSED){
    String req = client.readStringUntil('\r');
    if (req.indexOf("GET / HTTP") != -1){//ブラウザからリクエストを受信したらこの文字列を検知する
      Serial.print(req);
      //ブラウザからのリクエストでAccept-Language文字列の行まで読み込む
      while(req.indexOf("Accept-Language") == -1){
        req = client.readStringUntil('\r');
        Serial.print(req);
        yield();
      }
      String str;
      //-------ここからHTTPレスポンスのHTMLとJavaScriptコード
      str = "HTTP/1.1 200 OK\r\n";
      str += "Content-Type:text/html\r\n";
      str += "Connection:close\r\n\r\n";//１行空行が必要
      str += "<!DOCTYPE html><html><head>\r\n";
      str += "<meta name=\"viewport\" content=\"initial-scale=1.5\">\r\n";
      str += "<script type=\"text/javascript\">\r\n";
      str += "var source=new EventSource(\"";
      str += String(WiFi.localIP());//ルーターのローカルＩＰアドレスを自動取得
      str += "\");\r\n";
      str += "var obj1=document.getElementById(\"SSE_stop\");\r\n";
      str += "source.addEventListener('msg_1',function(event){\r\n";
      str += "var ms1 = document.getElementById('msgs1');\r\n";
      str += "ms1.innerHTML = event.data;});\r\n";
      str += "source.addEventListener('msg_2',function(event){\r\n";
      str += "var ms2 = document.getElementById('msgs2');\r\n";
      str += "ms2.innerHTML = event.data;});\r\n";
      str += "source.addEventListener('msg_3',function(event){\r\n";
      str += "var ms3 = document.getElementById('msgs3');\r\n";
      str += "ms3.innerHTML = event.data;});\r\n";
      str += "function fnc1(){source.close();}\r\n";//ＳＴＯＰボタンを押したら切断する関数
      str += "</script></head><body><form><FONT size=\"1\">\r\n";
      str += "NTP Server Sync</FONT><br>\r\n";
      str += "<FONT size=\"3\"><b>ESP-WROOM-02(ESP8266)<br>Arduino Watch</b><br>\r\n";
      str += "<FONT size=\"6\" color=\"#7777FF\"><b>\r\n";
      str += "<div id=\"msgs1\">Event wait 1</div>\r\n";
      str += "<div id=\"msgs2\">Event wait 2</div>\r\n";
      str += "</b></FONT>\r\n";
      str += "Get Sync NTP server Time";
      str += "<div id=\"msgs3\">Event wait 3</div><br>\r\n";
      str += "<input type=\"button\" id=\"SSE_stop\" value=\"SSE STOP\" onclick=\"fnc1()\">\r\n";
      str += "</form></body></html>\r\n";
      delay(1000);//1秒待ってレスポンスをブラウザに送信
      client.print(str);
      delay(1);//これが重要！これが無いと切断できないかもしれない。
      str = "";
      SSE_on = true;//Server-Sent Event 設定終了フラグ
      client.stop();
      Serial.println("\nGET HTTP client stop--------------------");
      req ="";
      SSE_Responce();
    }else if(req != ""){
      delay(1);
      client.stop();
      delay(1);
      client.flush();
    }
    req ="";
    yield();
  }
}
//**************Server-Sent Events データ送信関数****************************
void SSE_Responce()
{//HTTPレスポンス１度目を送信したら、すぐにブラウザから２回目のGETリクエストが来る
  while(SSE_on == true){//無限ループ
    client = server.available();
    while(client.status()!=CLOSED){
      String req = client.readStringUntil('\r');
      if(req.indexOf("GET") != -1){//２回目のGETを検知したらServer-Sent Eventsレスポンス送信
        Serial.println("GET in--------------------");
        Serial.print(req);
        while(req.indexOf("Accept-Language") == -1){
          req = client.readStringUntil('\r');
          Serial.print(req);
        }
        if(SSE_on == true){
          Serial.println("\nsse responce send--------------------");
          String sse_resp;
          //ストリーム配信をブラウザが認識するためのレスポンス
          sse_resp = "HTTP/1.1 200 OK\r\n";
          sse_resp += "Content-Type:text/event-stream\r\n";//SSE使用時に必ずサーバー側からブラウザへこれを返す
          sse_resp += "Cache-Control:no-cache\r\n";
          sse_resp += "\r\n";//必ずこの空行が必要
            
          client.print(sse_resp);
          delay(3000);//ここの秒数はもう少し少なくても問題ない
          Serial.println(sse_resp);
  
          String sse_data;
          
          Serial.println("sse data send--------------------");
          String str_h;
          String str_m;
          String str_s;
          String sync_h="?";
          String sync_m="?";
          String sync_s="?";
          LastTime = millis();   
          while(client.status()!=CLOSED){//Event Sourceデータの無限ループストリーム送信
            if(hour()<10){//一桁の数値を二桁にする
              str_h = "0" + String(hour()) ;
            }else{
              str_h = String(hour());
            }
            if(minute()<10){
              str_m = "0" + String(minute()) ;
            }else{
              str_m = String(minute());
            }
            if(second()<10){
              str_s = "0" + String(second()) ;
            }else{
              str_s = String(second());
            }
            //３０秒毎にNTPサーバーから時刻をゲットしてArduinoタイムを修正
            if(millis()-LastTime > 30000){
              WiFi.hostByName(ntpServerName, timeServerIP); 
              setSyncProvider(getNtpTime);
              LastTime = millis();
              sync_h = str_h;
              sync_m = str_m;
              sync_s = str_s;
            }
            sse_data = "event:msg_1\n";//ブラウザへ送るeventを発生させて改行コードをつける
            sse_data += "data:";
            sse_data += String(year())+"/"+String(month())+"/"+String(day());//data:の後に送りたいデータをつける
            sse_data += "\n\n";//イベントを発生させるためには必ず改行コード２回連続をつける
            sse_data += "event:msg_2\n";
            sse_data += "data:";
            sse_data += str_h+":"+str_m+":"+str_s;
            sse_data += "\n\n";
            sse_data += "event:msg_3\n";
            sse_data += "data:";
            sse_data += sync_h + ":" + sync_m + ":" + sync_s;//NTPサーバから取得した時刻を表示
            sse_data += "\n\n";
            client.print(sse_data);
            sse_data = "";
            yield();
          }
          delay(1);
          client.stop();
          delay(1);
          client.flush();
          Serial.println("Client.Stop-----------------");
          Serial.println();
          SSE_on = false;
          break;
        }
      }
      req = "";
      yield();
    }
    yield();
  }
}
//************NTPサーバータイム取得関数*****************************
const int timeZone = 9;     // 日本時間
//const int timeZone = 1;     // Central European Time
//const int timeZone = -5;  // Eastern Standard Time (USA)
//const int timeZone = -4;  // Eastern Daylight Time (USA)
//const int timeZone = -8;  // Pacific Standard Time (USA)
//const int timeZone = -7;  // Pacific Daylight Time (USA)
  
time_t getNtpTime()
{
  while (udp.parsePacket() > 0) ; // discard any previously received packets
  sendNTPpacket(timeServerIP);
  uint32_t beginWait = millis();
  while (millis() - beginWait < 1500) {
    delay(1);//これを入れないと更新できない場合がある。
    int size = udp.parsePacket();
    if (size >= NTP_PACKET_SIZE) {
      udp.read(packetBuffer, NTP_PACKET_SIZE);  // read packet into the buffer
      unsigned long secsSince1900;
      // convert four bytes starting at location 40 to a long integer
      secsSince1900 =  (unsigned long)packetBuffer[40] << 24;
      secsSince1900 |= (unsigned long)packetBuffer[41] << 16;
      secsSince1900 |= (unsigned long)packetBuffer[42] << 8;
      secsSince1900 |= (unsigned long)packetBuffer[43];
      return secsSince1900 - 2208988800UL + timeZone * SECS_PER_HOUR;
    }
  }
  return 0; // return 0 if unable to get the time
}
//******************NTPリクエストパケット送信****************************
unsigned long sendNTPpacket(IPAddress& address)
{
//  Serial.println("sending NTP packet...");
  // set all bytes in the buffer to 0
  memset(packetBuffer, 0, NTP_PACKET_SIZE);
  // Initialize values needed to form NTP request
  // (see URL above for details on the packets)
  packetBuffer[0] = 0b11100011;   // LI, Version, Mode
  packetBuffer[1] = 0;     // Stratum, or type of clock
  packetBuffer[2] = 6;     // Polling Interval
  packetBuffer[3] = 0xEC;  // Peer Clock Precision
  // 8 bytes of zero for Root Delay & Root Dispersion
  packetBuffer[12]  = 49;
  packetBuffer[13]  = 0x4E;
  packetBuffer[14]  = 49;
  packetBuffer[15]  = 52;
  
  // all NTP fields have been given values, now
  // you can send a packet requesting a timestamp:
  udp.beginPacket(address, 123); //NTP requests are to port 123
  udp.write(packetBuffer, NTP_PACKET_SIZE);
  udp.endPacket();
}
