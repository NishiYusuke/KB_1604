/**
* IBM IoT Foundation using HTTP
* 
* Author: Ant Elder
* License: Apache License v2
*/
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>

//-------- Customise these values -----------
const char* ssid = "<yourWiFiSSID>";
const char* password = "<yourWiFiPassword>";

#define ORG "quickstart" // your organization or "quickstart"
#define DEVICE_TYPE "ESP8266" // use this default for quickstart or customize to your registered device type
#define DEVICE_ID "Test1" // use this default for quickstart or customize to your registered device id
#define TOKEN "<yourDeviceToken>" // not used with "quickstart"
#define EVENT "myEvent" // use this default or customize to your event type
//-------- Customise the above values --------

String url = "http://" ORG ".internetofthings.ibmcloud.com/api/v0002/device/types/" DEVICE_TYPE "/devices/" DEVICE_ID "/events/" EVENT;

void setup() {

   Serial.begin(115200); Serial.println(); 

   if (ORG != "quickstart") { // for POST URL doc see: https://docs.internetofthings.ibmcloud.com/messaging/HTTPSDevice.html
      url.replace("http://", String("https://use-token-auth:") + TOKEN + "@");
   }
   Serial.print("IoT Foundation URL: "); Serial.println(url);

   Serial.print("Connecting to: "); Serial.print(ssid);
   WiFi.begin(ssid, password); 
   while (WiFi.status() != WL_CONNECTED) {
     delay(500);
     Serial.print(".");
   }

   Serial.print("nWiFi connected, IP address: "); Serial.println(WiFi.localIP());
}

void loop() {
   HTTPClient http;
   http.begin(url);
   http.addHeader("Content-Type", "application/json");
   // a simple payload, for doc on payload format see: https://docs.internetofthings.ibmcloud.com/messaging/payload.html
   int randNumber = random ( 1,100);
   String payload = String(“{ \”d\”: {\”aMessage\”: “) + randNumber + “} }”;
   Serial.print("POST payload: "); Serial.println(payload);
   int httpCode = http.POST(payload);
   Serial.print("HTTP POST Response: "); Serial.println(httpCode); // HTTP code 200 means ok 
   http.end();
 
   delay(10000);
}
