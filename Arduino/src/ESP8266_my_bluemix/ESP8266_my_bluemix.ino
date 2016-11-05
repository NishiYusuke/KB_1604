/**
* IBM IoT Foundation using HTTP
* 
* Author: Ant Elder
* License: Apache License v2
*/
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
/** 
 * This source code refer to the recipe of IBM developerWorks, https://developer.ibm.com/recipes/tutorials/connect-an-esp8266-with-the-arduino-sdk-to-the-ibm-iot-foundation/ .
 */ 

#include <ESP8266WiFi.h>
#include <PubSubClient.h>

//-------- Customise these values -----------
//const char* ssid = "fxli-sas";
//const char* password = "fxli1408";
const char* ssid = "HUMAX-EFCDD";
const char* password = "djFmXahmMTX4X";

const bool  SW= 0;
bool  SW_state=0,old_state=0;

#define ORG "x4bqqb"      // 組織ID
#define DEVICE_TYPE "ESP8266"  // デバイス・タイプ
#define DEVICE_ID "my-first-ESP8266"      // デバイスID
#define TOKEN "UgtMcZ(1cNjNFXr8cC"              // 認証トークン
//-------- Customise the above values --------

char server[] = ORG ".messaging.internetofthings.ibmcloud.com";
char topic[] = "iot-2/evt/status/fmt/json";
char authMethod[] = "use-token-auth";
char token[] = TOKEN;
char clientId[] = "d:" ORG ":" DEVICE_TYPE ":" DEVICE_ID;

WiFiClient wifiClient;
PubSubClient client(wifiClient);

void callback(char* topic, byte* payload, unsigned int length) {
 Serial.println("callback invoked");
}

void setup() {
 Serial.begin(115200);
 Serial.println();

 Serial.print("Connecting to ");
 Serial.print(ssid);
 WiFi.begin(ssid, password);
 pinMode(SW,INPUT);

 while (WiFi.status() != WL_CONNECTED) {
 delay(500);
 Serial.print(".");
 } 
 Serial.println("");

 Serial.print("WiFi connected, IP address: ");
 Serial.println(WiFi.localIP());

 client.setServer(server, 1883);
 client.setCallback(callback);
}

int counter = 0;

void loop() {

 if (!!!client.connected()) {
 Serial.print("Reconnecting client to ");
 Serial.println(server);
 while (!!!client.connect(clientId, authMethod, token)) {
 Serial.print(".");
 delay(500);
 }
 Serial.println();
 }
 
 
 SW_state=digitalRead(SW);
 if (old_state!=SW_state) {
    String payload="";
    if(SW_state==HIGH){
      Serial.println("Lock");
      payload +="Lock";
    }else{
      Serial.println("Open");
      payload +="Open";
    }
    Serial.print("Sending payload: ");
    Serial.println(payload);
    if (client.publish(topic, (char*) payload.c_str())) {
      Serial.println("Publish ok");
    }else {
      Serial.println("Publish failed");
    }
 } 
 old_state=SW_state;
 }
