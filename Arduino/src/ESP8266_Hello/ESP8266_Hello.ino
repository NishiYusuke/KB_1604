#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>

const char* ssid = "HUMAX-EFCDD";
const char* password = "djFmXahmMTX4X";

ESP8266WebServer server(80);

const int led = 13;

void handleRoot() {
  //digitalWrite(led, 1);
  server.send(200, "text/plain", "Hello World");
  //digitalWrite(led, 0);
}
