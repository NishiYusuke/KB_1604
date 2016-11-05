#include <ESP8266WiFi.h>

const char* ssid = "fxli-sas";
const char* password = "fxli1408";
//const char* ssid = "HUMAX-EFCDD";
//const char* password = "djFmXahmMTX4X";
const bool  SW= 0;
bool  SW_state=0;

WiFiServer server(8080);
WiFiClient client;

void connectWiFi(const char* ssid ,const char* password) {
   
  WiFi.disconnect();
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid ,password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
    Serial.print(".");
  }
  Serial.println();
  Serial.println("WiFi connected");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
}

void setup() {
  Serial.begin(115200);
  connectWiFi(ssid ,password);
  server.begin();
  client = server.available();
  
  pinMode(SW,INPUT);
}

void loop() {
   
  while ((WiFi.status() != WL_CONNECTED)) {
    connectWiFi(ssid ,password);
  }

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
