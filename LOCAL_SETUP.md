# Local Windows Setup & Execution Guide: CCA Gate Dispatch System

This document provides step-by-step instructions to set up, build, test, and run the **CCA Attendance & Gate Dispatch System** on a local Windows PC.

---

## 1. Prerequisites & Software Installation

Ensure the following tools are installed on your Windows machine:

1. **Java Development Kit (JDK 17 or 21)**
   - Download JDK 17 or JDK 21 (e.g. Eclipse Temurin or Oracle JDK).
   - Set environment variable `JAVA_HOME` pointing to your JDK folder (e.g., `C:\Program Files\Eclipse Adoptium\jdk-17.0.x`).
   - Add `%JAVA_HOME%\bin` to your system `Path`.

2. **Android Studio (Hedgehog 2023.1.1 or Ladybug / Koala or newer)**
   - Download and install [Android Studio](https://developer.android.com/studio).
   - During setup, install:
     - **Android SDK Platform 35** (Android 15 / API Level 35) & **Platform 31** (minSdk).
     - **Android SDK Build-Tools 35.0.0**.
     - **Android Emulator** & **HAXM / Android Emulator Hypervisor Driver** for Windows (for hardware-accelerated emulation).

3. **Git for Windows**
   - Download and install [Git for Windows](https://git-scm.com/download/win).

---

## 2. Cloning & Environment Configuration

### Step 2.1: Clone the Repository
Open PowerShell or Command Prompt (`cmd`) and run:
```cmd
git clone <repository-url>
cd app
```

### Step 2.2: Configure `local.properties`
In the root directory of the project, create or edit the `local.properties` file:
```properties
# Path to your Windows Android SDK installation
sdk.dir=C\:\\Users\\YOUR_WINDOWS_USERNAME\\AppData\\Local\\Android\\Sdk
```
*(Replace `YOUR_WINDOWS_USERNAME` with your actual Windows account username).*

---

## 3. Opening & Building in Android Studio

1. **Launch Android Studio**.
2. Select **Open** and navigate to the project folder (`app`).
3. Allow Android Studio to import the Gradle project and download dependencies.
4. Verify Gradle Sync:
   - Click **File > Sync Project with Gradle Files**.
   - Ensure the Gradle Sync completes successfully with zero errors.

---

## 4. Running the App on Windows

### Method A: Running via Android Studio Emulator

1. **Create an Android Emulator (AVD)**:
   - Open **Tools > Device Manager** in Android Studio.
   - Click **Create Device**.
   - Select a phone model (e.g., **Pixel 7** or **Pixel 8**).
   - Select system image **API Level 31 or higher** (e.g., Android 12, 13, 14, or 15).
   - Click **Finish**.

2. **Run the Application**:
   - Select `:app` in the run configuration dropdown at the top bar.
   - Select your created Emulator device.
   - Click the green **Run** button (or press `Shift + F10`).
   - The app will build, launch, and automatically seed initial offline test data on first boot.

### Method B: Running via Windows Command Prompt / PowerShell

Open Command Prompt or PowerShell in the root directory:

```powershell
# Compile Kotlin sources
.\gradlew.bat :app:compileDebugKotlin

# Run Unit Tests
.\gradlew.bat :app:testDebugUnitTest

# Assemble Debug APK
.\gradlew.bat :app:assembleDebug
```
The output APK will be located at:
`app\build\outputs\apk\debug\app-debug.apk`

To install directly onto a connected physical Android device or running emulator via ADB:
```cmd
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## 5. Testing App Features Locally

1. **Main Entrance Portal**:
   - Enter `STU_1001` in the OCR/Barcode input field and click **Simulate Scan**.
   - Observe real-time entry confirmation and log stream insertion.

2. **Coach Portal**:
   - Select a Coach (e.g., `Coach Marcus Vance`) and CCA (`Varsity Basketball`).
   - Click **Confirm Coach Check-In**.
   - Toggle student attendance status (`Present`/`Absent`).
   - Click **Complete Class & Open Exit Window**.

3. **Gate Dispatch Portal**:
   - **Self Commute**: Search `STU_1001` (Pre-authed) or `STU_1002` (Not pre-authed, triggers Guard Override dialog).
   - **Parent Pickup**: Search `P_PAR_THORNE` to fetch and release the Thorne sibling group.
   - **Van Handover**: Search `DRV_901` to load Green Route roster. Check boarded students, draw a signature on the **Signature Canvas**, click **Confirm Signature**, and click **Dispatch Batch Van Release**.

---

## 6. Troubleshooting Common Windows Issues

- **Gradle Daemon JDK Mismatch Error**:
  - In Android Studio, go to `File > Settings > Build, Execution, Deployment > Build Tools > Gradle`.
  - Set **Gradle JDK** to JDK 17 or JDK 21.

- **`sdk.dir` Not Found**:
  - Ensure backslashes are escaped in `local.properties` (e.g., `C\:\\Users\\...`).

- **Emulator Performance Lag on Windows**:
  - Enable **Windows Hypervisor Platform** (`Optional Features > Windows Hypervisor Platform`) in Windows Settings or Control Panel.
