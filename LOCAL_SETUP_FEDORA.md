# Local Linux Fedora Setup & Execution Guide: CCA Gate Dispatch System

This document provides detailed step-by-step instructions to set up, build, test, and run the **CCA Attendance & Gate Dispatch System** on **Linux Fedora** (Fedora Workstation 38 / 39 / 40 / 41).

---

## 1. Prerequisites & Software Installation on Fedora

Open your terminal on Fedora and execute the following commands to install required tools and system libraries:

### Step 1.1: System Update & Development Tools
```bash
sudo dnf update -y
sudo dnf install -y git curl wget unzip tar
```

### Step 1.2: Install Java Development Kit (JDK 17 or 21)
Install OpenJDK 17 or OpenJDK 21 via `dnf`:
```bash
# Install OpenJDK 17
sudo dnf install -y java-17-openjdk java-17-openjdk-devel

# Verify Java installation
java -version
javac -version
```

Ensure `JAVA_HOME` environment variable is set in your `~/.bashrc` or `~/.zshrc`:
```bash
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

### Step 1.3: Enable KVM Hardware Acceleration for Android Emulator
For Android Emulator hardware acceleration on Fedora, KVM (Kernel-based Virtual Machine) must be enabled and your user added to the `kvm` group:

```bash
# Install KVM and virtualization utils
sudo dnf install -y qemu-kvm libvirt virt-install bridge-utils

# Verify KVM support
lscpu | grep Virtualization

# Add current user to the kvm group
sudo usermod -aG kvm $USER

# Apply group changes without logging out
newgrp kvm
```

Verify KVM permission:
```bash
ls -l /dev/kvm
# Expected output: crw-rw----. 1 root kvm ... /dev/kvm
```

### Step 1.4: Install Android Studio on Fedora
You can install Android Studio using **Flatpak** (recommended on Fedora) or via direct tarball download:

#### Option A: Via Flatpak (Recommended)
```bash
# Enable Flathub if not enabled
flatpak remote-add --if-not-exists flathub https://dl.flathub.org/repo/flathub.flatpakrepo

# Install Android Studio
flatpak install flathub com.google.AndroidStudio -y
```

#### Option B: Via Official Tarball
1. Download Android Studio for Linux from [developer.android.com/studio](https://developer.android.com/studio).
2. Extract to `/opt`:
   ```bash
   sudo tar -xzf android-studio-*-linux.tar.gz -C /opt/
   /opt/android-studio/bin/studio.sh
   ```

---

## 2. Cloning & Environment Setup

### Step 2.1: Clone the Repository
```bash
git clone <repository-url>
cd app
```

### Step 2.2: Configure `local.properties`
Set up the Android SDK path in `local.properties`:

- **Standard Linux SDK Path**: `/home/$USER/Android/Sdk`
- **Flatpak Android Studio SDK Path**: `/home/$USER/Android/Sdk` or `~/.var/app/com.google.AndroidStudio/data/Android/Sdk`

Create or edit `local.properties` in the root project folder:
```properties
sdk.dir=/home/YOUR_FEDORA_USERNAME/Android/Sdk
```
*(Replace `YOUR_FEDORA_USERNAME` with your actual Linux user name, check using `whoami`).*

---

## 3. Building the Application on Fedora

### Option A: Using Command Line / Terminal (Gradle Wrapper)

Make the Gradle wrapper script executable:
```bash
chmod +x gradlew
```

1. **Compile Kotlin Sources**:
   ```bash
   ./gradlew :app:compileDebugKotlin
   ```

2. **Execute Unit Tests**:
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```

3. **Build Debug APK**:
   ```bash
   ./gradlew :app:assembleDebug
   ```
   The generated APK will be available at:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 4. Running the Application on Fedora

### Method 1: Running in Android Studio Emulator on Fedora
1. Launch Android Studio.
2. Open the project directory (`app`).
3. Open **Tools > Device Manager**.
4. Create an AVD (e.g. Pixel 7 running API Level 31+ / Android 12+).
5. Click **Run** (`Shift + F10`).

### Method 2: Installing to USB Physical Device or Emulator via ADB
1. Enable **Developer Options** and **USB Debugging** on your physical Android device.
2. Connect your phone via USB.
3. Install `android-tools` package on Fedora:
   ```bash
   sudo dnf install -y android-tools
   ```
4. Verify connected device:
   ```bash
   adb devices
   ```
5. Install and launch APK:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 5. Testing App Workflows Locally

1. **Main Entrance Portal**:
   - Type/Scan student ID `STU_1001` to test OCR entry verification and live event logging.
2. **Coach Portal**:
   - Select Coach `Coach Marcus Vance` and CCA `Varsity Basketball`.
   - Perform coach check-in, toggle student roster status (`Present`/`Absent`), and finalize class session.
3. **Gate Dispatch Portal**:
   - **Self Commute**: Validate pre-auth checking (`STU_1001` vs `STU_1002` guard override modal).
   - **Parent Pickup**: Search parent ID `P_PAR_THORNE` to retrieve and release sibling group.
   - **Van Handover**: Search driver `DRV_901`, check boarded roster, sign on the interactive `SignatureCanvas`, and authorize batch dispatch.

---

## 6. Fedora Specific Troubleshooting

- **`/dev/kvm permission denied`**:
  - Run `sudo usermod -aG kvm $USER` and reboot or execute `newgrp kvm`.
- **ADB Device Permission Errors (`no permissions`)**:
  - Install Android udev rules on Fedora:
    ```bash
    sudo dnf install -y android-udev-rules
    sudo systemctl restart systemd-udevd
    ```
- **SELinux Denials**:
  - If SELinux blocks emulator execution, verify status via `sestatus`. Set permissive mode temporarily if debugging hypervisor rules: `sudo setenforce 0`.
