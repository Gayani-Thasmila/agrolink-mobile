# AgroLink Mobile

An Android mobile application for **AgroLink**, developed using **Java** and **Android Studio**. The application provides users with a mobile platform to browse agricultural products, manage their accounts, place orders, and interact with the AgroLink backend.

## Overview

AgroLink Mobile is the Android client application of the AgroLink system.

The application communicates with the AgroLink Spring Boot backend through REST APIs and uses Firebase for authentication and messaging-related functionality.

## Key Features

* User registration and login
* User profile management
* Product browsing
* Product details
* Shopping and order-related functionality
* Payment-related functionality
* Firebase authentication
* Firebase messaging and notifications
* REST API integration
* Android mobile user interface

## Technologies

* **Java**
* **Android**
* **Android Studio**
* **XML**
* **Firebase Authentication**
* **Firebase Cloud Messaging (FCM)**
* **REST API**
* **Spring Boot**
* **Gradle**

## Application Architecture

```text
Android App (Java + XML)
          ↓
      REST APIs
          ↓
  AgroLink Backend
       ↙       ↘
   MySQL      Firebase
```

The Android application communicates with the backend through REST APIs. The backend handles server-side operations and database access, while Firebase provides authentication and messaging-related services.

## Backend Integration

The mobile application connects to the AgroLink Spring Boot backend for server-side operations.

Backend repository:

https://github.com/Gayani-Thasmila/agrolink-backend

When testing the application on a physical Android device with a locally running backend, use the local IP address of the computer running the backend instead of `localhost`.

Example:

```text
http://<YOUR_COMPUTER_IP>:8080/api
```

Replace `<YOUR_COMPUTER_IP>` with the local IP address of the development computer.

The Android device and development computer should normally be connected to the same Wi-Fi network when testing the local backend.

## Firebase

Firebase is used for authentication and messaging-related functionality within the application.

Firebase is used for:

* User authentication
* Cloud messaging
* Push notifications

Firebase configuration files and credentials should be handled securely and should not expose private keys or other sensitive information in source control.

## Payment

The application includes payment-related functionality integrated with the AgroLink backend.

Payment credentials and merchant secrets should never be stored directly in the source code or committed to GitHub.

## Project Structure

```text
agrolink-mobile/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle
├── gradle/
├── build.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle
└── README.md
```

## Getting Started

### Prerequisites

Make sure the following are installed:

* Android Studio
* Android SDK
* Java JDK
* Android device or emulator
* Internet connection

### 1. Clone the Repository

```bash
git clone https://github.com/Gayani-Thasmila/agrolink-mobile.git
cd agrolink-mobile
```

### 2. Open the Project

Open the project in **Android Studio** and allow Gradle to complete the required synchronization.

### 3. Configure the Backend URL

When using a physical Android device with a locally running backend, configure the application to use the development computer's local IP address.

Do not use:

```text
http://localhost:8080
```

Use:

```text
http://<YOUR_COMPUTER_IP>:8080
```

instead.

### 4. Run the Application

Connect an Android device or start an Android emulator, then run the application from Android Studio.

## Related Project

### AgroLink Backend

Spring Boot REST API providing the server-side functionality for the AgroLink mobile application.

Repository:

https://github.com/Gayani-Thasmila/agrolink-backend

## Security

The repository should not contain:

* API keys
* Database passwords
* Payment merchant secrets
* Private keys
* Firebase service-account private keys
* Local development credentials
* Generated build files
* IDE-specific files

Sensitive configuration should be handled through appropriate local configuration or environment-specific mechanisms.

## Development

This project was developed as part of a Software Engineering project and demonstrates Android application development using Java, Firebase integration, REST API communication, backend integration, authentication, notifications, payment-related functionality, and mobile UI development.

## Author

**Gayani Thasmila**

Software Engineering Undergraduate
