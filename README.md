# 🎯 Spotlight – EventExplorer

**Spotlight** (EventExplorer) is an Android mobile application built using **Kotlin** that allows users to explore, save, and interact with local events in their community.  
The app integrates multiple Android components such as **Firebase Authentication**, **SQLite Database**, **Google Maps**, **Multimedia (Audio/Video)**, and **Accelerometer Sensors** to deliver a modern and interactive event discovery experience.

--

## 🧭 Project Overview

Spotlight is designed to help users **discover events**, **view event details**, and **save their favorites**.  
It brings together multiple Android concepts into a single cohesive prototype, demonstrating user authentication, database integration, mapping, and multimedia capabilities.

---

## 🚀 Key Features

### 🔐 User Authentication (Firebase)
- Secure **login** and **signup** using Firebase Authentication.
- Redirects authenticated users directly to the Home screen.
- Prevents unauthorized access to event data.

<img width="208" height="458" alt="image" src="https://github.com/user-attachments/assets/a2f54807-55b5-488a-ad78-412a4ab35f93" />

<img width="205" height="457" alt="image" src="https://github.com/user-attachments/assets/814f3f3b-9393-4900-9111-5c066763e6bf" />

### 🏠 Dynamic Home Page
- Displays events using **RecyclerView** in a clean, scrollable list.
- Toolbar includes:
    - **Map** button (left)
    - **Saved Events** button (right)
    - **Logout** button (right)
- Features a **Search Bar** (top right) that filters events by name in real-time.

<img width="224" height="498" alt="image" src="https://github.com/user-attachments/assets/d4bb4947-5e08-428f-a254-40d27dd3d24a" />

### 🗺️ Google Maps Integration
- Shows all event locations with **custom markers**.
- Includes user’s **current GPS location**.
- Tapping “View on Map” in an event detail centers the map on that specific event.

<img width="186" height="407" alt="image" src="https://github.com/user-attachments/assets/968119db-67a2-480e-ba99-770ae3e08d2a" />

### 💾 Database Integration (SQLite)
- Local database with two tables:
    - `events` – all available events.
    - `saved_events` – events user has saved.
- Prevents duplicate entries.
- Supports adding, viewing, and deleting saved events.\

<img width="236" height="529" alt="image" src="https://github.com/user-attachments/assets/83a55667-9b79-4d26-a534-22930bd05889" />

### 🎬 Multimedia Integration
- Each event has a **promotional video or audio**.
- Integrated **VideoView** and **MediaPlayer** for smooth playback.
- Examples:
    - 🎵 *Music Fest* – plays MP3 background music.
    - 🎥 *Food Carnival / Fun Fair / Art Exhibit* – plays MP4 event video.

<img width="206" height="457" alt="image" src="https://github.com/user-attachments/assets/5ea19c2b-dd4d-47b0-a0c2-d7d6559d27db" />

### 📱 Sensor Integration (Accelerometer)
- Detects **shake gesture** using accelerometer.
- On shake, prompts:
  > “Undo last saved event?”
- If confirmed, the most recent saved event is removed automatically.

### ⭐ Saved Events Page
- Displays all user-saved events.
- Allows removing events.
- Shows “No events added” if list is empty.
- Includes a **Back** button for easy navigation.

---

## 🧠 Tech Stack

| Component | Technology |
|------------|-------------|
| **Language** | Kotlin |
| **Database** | SQLite (via `SQLiteOpenHelper`) |
| **Authentication** | Firebase Authentication |
| **Maps** | Google Maps SDK |
| **Media** | VideoView, MediaPlayer |
| **Sensors** | Accelerometer |
| **UI** | XML Layouts (Material Design) |
| **IDE** | Android Studio |

---

## 🗂 Project Structure

```
app/src/main/java/com/Project/Project/
│
├── HomeActivity.kt
├── LoginActivity.kt
├── SignupActivity.kt
├── EventDetailActivity.kt
├── MapActivity.kt
├── SavedEventsActivity.kt
├── SensorActivity.kt
├── Event.kt
├── EventAdapter.kt
└── EventDatabaseHelper.kt

app/src/main/res/
│
├── layout/          # XML Layouts
├── drawable/        # Icons (ic_map, ic_star, ic_logout, ic_search)
├── raw/             # Audio/Video media files
└── values/          # Colors, Styles, Strings
```

---

## ⚙️ Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/spatel12696/Spotlight_EventExplorer.git
   ```

2. **Open in Android Studio**
    - File → Open → Select this project folder.

3. **Sync Gradle**
    - Let Android Studio install dependencies automatically.

4. **Add Firebase Configuration**
    - Place your `google-services.json` file inside `/app`.

5. **Add Google Maps API Key**
    - Open `AndroidManifest.xml` and replace the placeholder API key:
      ```xml
      <meta-data
          android:name="com.google.android.geo.API_KEY"
          android:value="YOUR_API_KEY_HERE" />
      ```

6. **Run the App**
    - On an emulator or physical Android device (Android 8.0+ recommended).

---

## 🧭 Roadmap & Future Enhancements

- 🔍 **Search Bar Integration** (Completed)
- 📱 **Accelerometer Undo Feature** (Completed)
- 👤 **User Profiles** — personalized recommendations.
- 🔔 **Push Notifications** for upcoming events.
- 🌐 **Firebase Realtime Database** for live event updates.
- 🎨 **UI Enhancements** with animations and transitions.
