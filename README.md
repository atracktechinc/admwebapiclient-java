# ADM Web API Client for Testing (Java version)

## Prerequisites
  - IntelliJ IDEA 2019, or other IDE
  - Gradle 4.10.3 or above
  - APPId
  - SecretKey

## Configurations Setting

| Config Name | Description | Available Value |
| ---------- | ------------------- | ---------------------- |
| HttpMethod | HTTP request action | GET, POST, PUT, DELETE |
| BaseURL    | API server base URL. **DO NOT CHANGE.** |    |
| AbsolutePath | Equals to API path. Keep the curly brackets and parameter name, e.g. /api/devices/{IMEI}. | Refer to API Documentation |
| APPId | ADM user profile property | |
| SecretKey | ADM user profile property | |
| AuthenticationScheme | API Key Authentication Scheme. **DO NOT CHNAGE.** | atrack |

## API Documentation

[ADM Web API V1](http://adm.atrack.com.tw/swagger/ui/index)
