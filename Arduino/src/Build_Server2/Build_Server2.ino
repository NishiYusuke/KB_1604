#include <ESP8266WiFi.h>

#define WIFI_SSID "Team_Hanshin"
#define WIFI_PWD "Team_Hanshin"
const char* ssid = "Team_Hanshin";
const char* password = "Team_Hanshin";

const bool  SW= 0;
bool  SW_state=0;

WiFiServer server(8080);
WiFiClient client;
IPAddress ip( 192, 168, 10, 1 );
IPAddress subnet( 255, 255, 255, 0 );

void connectWiFi(const char* ssid ,const char* password) {
   
  WiFi.disconnect();
  WiFi.mode(WIFI_AP);
  WiFi.softAPConfig(ip, ip, subnet);
  WiFi.softAP(WIFI_SSID, WIFI_PWD);
  
  Serial.print("IP address: ");
  Serial.println("192, 168, 10, 1");
}

void setup() {
  Serial.begin(115200);
  connectWiFi(ssid ,password);
  server.begin();
  client = server.available();
  
  pinMode(SW,INPUT);
}

void loop() {

  while (!client) {
    client = server.available();
    delay(10);
  }
  
  while (!client.available()) {
    delay(10);
  }
  Serial.println("client connected");
  
  String res = client.readStringUntil('\n');
  Serial.println(res);
  client.flush();

  SW_state=digitalRead(SW);
  if (res.equals("ON")) {
    if(SW_state==HIGH){
      Serial.println("Lock");
      client.print("Lock");
    }else{
      Serial.println("Open");
      client.print("Open");
    }
  }
  client.stop();
}
