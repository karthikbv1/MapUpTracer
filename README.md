# 📍 MapUpTracer – Android GPS Location Tracker Assessment

MapUpTracer is a native Android GPS tracking application developed as part of the **MapUp Android Developer Assessment**.

The app tracks user movement sessions in real time, stores location history locally, supports background tracking, allows route visualization using Google Maps, exports session data as CSV, and includes lightweight **AI-powered route insights**.

---

## 🚀 Features Implemented

- 📍 Real-time GPS location tracking
- 🔄 Background tracking support
- 🔔 Foreground service notification while tracking
- ⏹️ Start / Stop tracking controls
- 📂 Session history with saved tracking sessions
- 🗺️ Open tracked route in Google Maps
- 📄 Export session data as CSV
- 🌙 In-app Dark / Light mode toggle
- 🤖 AI-style route summary / insight
- 🧭 Travel mode prediction:
  - Stationary
  - Walking
  - Running
  - Vehicle

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Database:** Room Database
- **Location Services:** Fused Location Provider API
- **Background Tracking:** Foreground Service
- **Maps:** Google Maps Intent Integration
- **Architecture Pattern:** Layered Android Architecture
- **AI Feature:** Rule-based movement intelligence

---

## ⚙️ Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_GITHUB_USERNAME/MapUpTracer.git
```

### 2. Open the project

Open the project in **Android Studio**.

### 3. Add Google Maps API Key

Inside `AndroidManifest.xml`, replace the API key value:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY" />
```

### 4. Sync Gradle

Allow Android Studio to sync all dependencies.

### 5. Run the app

Run the application on:

- A **physical Android device** (recommended)
- Or an **emulator** (limited for real GPS tracking)

### 6. Grant required permissions

When the app launches, allow:

- Fine Location
- Coarse Location
- Notification Permission (Android 13+)

---

## 🏗️ Architecture Overview

The project follows a simple layered architecture for clarity and maintainability.

### Main Components

#### 1. UI Layer

Built using **Jetpack Compose**.

**Responsible for:**

- Home screen UI
- Session list
- Start / Stop tracking buttons
- Export button
- Dark mode toggle
- AI route summary display

#### 2. Service Layer

Handled using `LocationService.kt`

**Responsible for:**

- Background location tracking
- Foreground notification
- Starting and stopping tracking
- Capturing GPS points continuously

#### 3. Data Layer

Built using **Room Database**

**Includes:**

- `SessionEntity`
- `LocationEntity`
- `LocationDao`
- `AppDatabase`

**Responsible for:**

- Saving sessions
- Storing route points
- Retrieving session history

#### 4. Utility Layer

Contains helper classes such as:

- `DistanceUtils.kt`
- `ExportUtils.kt`
- `AIUtils.kt`

**Responsible for:**

- Distance calculation
- CSV export
- Route intelligence generation

---

## 📂 Project Structure

```bash
app/
 ┣ data/
 ┃ ┣ AppDatabase.kt
 ┃ ┣ LocationDao.kt
 ┃ ┣ LocationEntity.kt
 ┃ ┗ SessionEntity.kt
 ┣ service/
 ┃ ┗ LocationService.kt
 ┣ utils/
 ┃ ┣ DistanceUtils.kt
 ┃ ┣ ExportUtils.kt
 ┃ ┗ AIUtils.kt
 ┣ ui/theme/
 ┃ ┣ Color.kt
 ┃ ┣ Theme.kt
 ┃ ┗ Type.kt
 ┗ MainActivity.kt
```

---

## 📸 App Screenshots

Add your screenshots inside a `/screenshots` folder in the repository.

### Screens to include:

1. Home Screen  
2. Dark Mode UI  
3. Session History  
4. Route Opened in Google Maps  
5. CSV Export Feature  
6. AI Route Insight  

### Example screenshot markdown

```md
![Home Screen](screenshots/home.png)
![Dark Mode](screenshots/dark_mode.png)
![Session History](screenshots/session_history.png)
```

---

## 🤖 AI / Smart Feature Added

To make the app more intelligent and aligned with **AI/ML concepts**, I added a lightweight **AI-inspired route analysis module**.

### It performs:

- Travel mode prediction
- Route movement pattern summary
- Session activity classification

### Example insight

> “This session covered 1.25 km in 12 min. Likely activity: Walking. Movement appears short and consistent.”

This is currently implemented using **rule-based logic**, but can later be extended using a **real ML model or LLM**.

---

## 📄 Export Functionality

Users can export tracking sessions as **CSV files**.

### Export location

```bash
Android/data/com.karthik.mapuptracer/files/Documents/
```

---

## 🔑 Permissions Used

- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_LOCATION`
- `POST_NOTIFICATIONS` (Android 13+)

---

## ⚠️ Known Limitations

- Route visualization currently opens in **Google Maps** instead of an embedded in-app map view.
- AI route insight is currently **rule-based**, not powered by a real machine learning model.
- Background tracking depends on **Android battery optimization settings** on some devices.
- CSV export is implemented, but **GPX export** is not yet added.
- App is currently optimized for **single-device local usage** (no cloud sync).

---

## 🎥 Video Submission

**Demo Video Link:**  
[PASTE_YOUR_VIDEO_LINK_HERE]

---

## 📥 APK / App Download

**APK Download Link:**  
[PASTE_YOUR_APK_LINK_HERE]

---

## 👨‍💻 Developer

**Karthik B.V.**  
MCA Student | AI & ML Enthusiast

---

## 📌 Assessment Notes

This project was built as part of the **MapUp Android Developer Assessment**.

This repository is intended for **review purposes only**.

---

## 📜 License

This project is for **assessment / educational use only**.
