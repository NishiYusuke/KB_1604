/**
* IBM IoT Foundation using HTTP
* 
* Author: Ant Elder
* License: Apache License v2
*/
#include <ESP8266WiFi.h>//Used for functions concerning ESP8266 wifi module
#include <ESP8266HTTPClient.h>//Used for HTTP protcol by ESP8266
/** 
 * This source code refer to the recipe of IBM developerWorks, https://developer.ibm.com/recipes/tutorials/connect-an-esp8266-with-the-arduino-sdk-to-the-ibm-iot-foundation/ .
 */ 

#include <PubSubClient.h>//Used for connecting a cloud web-server of bluemix 

//-------- Customise these values -----------
const char* ssid = "fxli-sas";//Wifi rooter's ssid
const char* password = "fxli1408";//Wifi rooter's passwprd

const bool  SW= 0;//ESP8266's I/O pin number
bool  SW_state=0,old_state=0;//Variables for checking door state "Open" or "Lock"

#define ORG "x4bqqb"//Organization name on bluemix
#define DEVICE_TYPE "ESP8266"//Device type on bluemix
#define DEVICE_ID "my-first-ESP8266"//Device ID on bluemix
#define TOKEN "UgtMcZ(1cNjNFXr8cC"//Authentication token on bluemix

//-------- Store names for connecting the web-sever and getting device information --------
char server[] = ORG ".messaging.internetofthings.ibmcloud.com";
char topic[] = "iot-2/evt/status/fmt/json";
char authMethod[] = "use-token-auth";
char token[] = TOKEN;
char clientId[] = "d:" ORG ":" DEVICE_TYPE ":" DEVICE_ID;

WiFiClient wifiClient;//Used for setting client variable
PubSubClient client(wifiClient);//Used by connecting to the web-server

void callback(char* topic, byte* payload, unsigned int length) {//Callback function of the web-server
 Serial.println("callback invoked");
}

void setup() {
 Serial.begin(115200);//Begin serial connection between ESP8266 and PC
 //-------- Indicate --------
 Serial.println();
 Serial.print("Connecting to ");
 Serial.print(ssid);
 
 WiFi.begin(ssid, password);//Begin wifi connection between ESP8266 and wifi rooter
 
 //-------- I/O config --------
 pinMode(SW,INPUT);

//-------- Indicating until ESP8266 connecting to wifi rooter as client
 while (WiFi.status() != WL_CONNECTED) {
 delay(500);
 Serial.print(".");
 } 
 Serial.println("");

//-------- Indicate ESP8266's local IP adrress
 Serial.print("WiFi connected, IP address: ");
 Serial.println(WiFi.localIP());

 client.setServer(server, 1883);//Set sever MQTT's port number
 client.setCallback(callback);//Set call back function
}

void loop() {
 //--------  Reconnecting if ESP8266 lost connection of that web-sever
 if (!!!client.connected()) {
 Serial.print("Reconnecting client to ");
 Serial.println(server);
 while (!!!client.connect(clientId, authMethod, token)) {
 Serial.print(".");
 delay(500);
 }
 Serial.println();
 }
 String payload = "{\"Status\":";//Used as in-put variable for the web-sever  
 SW_state=digitalRead(SW);//Get I/O pin voltage state for checking door state "Open" or "Lock"
 //-------- Only executed when cheanged door state --------
 if (old_state!=SW_state) {
    if(SW_state==HIGH){
      Serial.println("Lock");
      payload +="\"Lock\"";
    }else{
      Serial.println("Open");
      payload +="\"Open\"";
    }
    payload += "}";//"payload" made by json type

    //-------- Indicate "payload"
    Serial.print("Sending payload: ");
    Serial.println(payload);

    //-------- Indicate result of sending "payload" to the web-server which success or failed --------
    if (client.publish(topic, (char*) payload.c_str())) {
      Serial.println("Publish ok");
    }else {
      Serial.println("Publish failed");
    }
 } 
 old_state=SW_state;//Update "old_state"
 }
