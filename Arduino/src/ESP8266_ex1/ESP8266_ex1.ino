#include <ESP8266WiFi.h>

const char* ssid = "HUMAX-EFCDD";
const char* password = "djFmXahmMTX4X";
const int LED = 0;

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
  
  pinMode(LED ,OUTPUT);
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
  
  if (res.equals("ON")) {
    digitalWrite(LED ,HIGH);
    client.print("Lock");
  }
  else if (res.equals("OFF")) {
    digitalWrite(LED ,LOW);
    client.print("Open");
  }
  else {
    Serial.println("ERROR");
    client.print("error");
  }
  client.stop();
}
