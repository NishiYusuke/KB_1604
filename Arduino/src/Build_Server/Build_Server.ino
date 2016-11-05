#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#define WIFI_SSID "Team_Hanshin"
#define WIFI_PWD "Team_Hanshin"
ESP8266WebServer server( 80 );
IPAddress ip( 192, 168, 10, 1 );
IPAddress subnet( 255, 255, 255, 0 );
void setup() { 
  WiFi.mode(WIFI_AP);
  WiFi.softAPConfig(ip, ip, subnet);
  WiFi.softAP(WIFI_SSID, WIFI_PWD);
  server.on("/", [](){server.send( 200, "text/html", "<h1>Hello! Web Server!</h1>");});
    server.begin(); 
    }
    void loop() {
      server.handleClient();
      }
