# Detailed System Design: CCA Attendance & Gate Dispatch System (Android 12+)

A production-ready, highly secure, lightweight, and modern Material Design 3 native Android application developed using **Kotlin (1.9+)**, **Jetpack Compose**, **Room Database (2.6+) encrypted with 256-bit AES SQLCipher**, and **Hilt Dependency Injection**.

The system acts as a strict **air-gapped (zero network permissions)** off-grid mobile solution for managing Co-Curricular Activities (CCA) class schedules, tracking coach/student attendance, and enforcing multi-model gate-security protocols for student departure.

---

## 1. System Architecture & High-Level Constraints

### 1.1 Mandatory System Constraints
- **Target OS**: Android 12+ (`minSdk = 31`, `targetSdk = 35`, `compileSdk = 35`).
- **Connectivity**: **STRICT AIR-GAP**. Zero network permissions (`INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` are omitted from `AndroidManifest.xml`).
- **Architecture**: Clean Architecture + MVVM + Unidirectional Data Flow (UDF).
- **Security**: Hardware-backed **Android Keystore System** generating an AES-256 master key that encrypts/decrypts a 256-bit database passphrase stored in **Preferences DataStore**, used directly by **SQLCipher** to encrypt the SQLite database.
- **Build Tooling**: Gradle Kotlin DSL (`build.gradle.kts`), Version Catalog (`libs.versions.toml`), and R8 Full Mode optimization.

### 1.2 Layered Architectural Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                             PRESENTATION LAYER                              │
│  Jetpack Compose + Material Design 3 (Dynamic Color)                        │
│  - MainEntrancePortal  │  - CoachPortal  │  - GateDispatchPortal            │
│  - Interactive SignatureCanvas (SVG Generation)                             │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ UDF (UiState / UiEvent / UiEffect)
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                             VIEWMODEL LAYER                                 │
│  BaseViewModel<UiState, UiEvent, UiEffect> (StateFlow & Channel)           │
│  - EntryViewModel     │  - CoachViewModel     │  - DispatchViewModel        │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Repository Invocations
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                             REPOSITORY & DOMAIN                             │
│  - AppRepository (Audit Counters, Logs, Dispatch Rules)                     │
│  - SeedService (Safe Non-Destructive Data Seeding)                          │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Room DAOs
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                             PERSISTENCE & SECURITY                          │
│  - AppDatabase (Room 2.6+)                                                  │
│  - SQLCipher SupportFactory (256-bit AES DB Encryption)                     │
│  - KeyProvider (Android Keystore AES-256 + Preferences DataStore)          │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Module Breakdown & Tech Details

### 2.1 Security & Key Management Module (`com.example.ccagatedispatch.security`)
- **`KeyProvider.kt`**:
  - Singleton component injected via Hilt.
  - Interacts with `AndroidKeyStore` to generate/retrieve a master secret key using `AES/GCM/NoPadding` (256-bit key size).
  - Generates a cryptographically random 32-byte (256-bit) passphrase using `SecureRandom` on first boot.
  - Encrypts the passphrase with the master key and persists it in `Preferences DataStore`.
  - On subsequent launches, decrypts the passphrase seamlessly without relying on network or external services.

### 2.2 Database & Persistence Module (`com.example.ccagatedispatch.data.local`)
- **`AppDatabase.kt`**:
  - Room Database holding 12 entities with normalized foreign key relationships and index structures.
- **`DatabaseModule.kt`**:
  - Hilt DI module providing `SupportFactory` from `net.sqlcipher.database.SupportFactory` initialized with `KeyProvider`'s passphrase.
  - Builds the encrypted `AppDatabase` via `.openHelperFactory(factory)`.
- **Relational Entities (`AppEntities.kt`)**:
  1. `AcademicYearEntity`: Primary key `year`.
  2. `CoachEntity`: Primary key `coach_id`, foreign key to `academic_years`.
  3. `CcaEntity`: Primary key `cca_id`, foreign key to `academic_years`.
  4. `VanEntity`: Primary key `van_id`, driver ID index, foreign key to `academic_years`.
  5. `StudentEntity`: Primary key `student_id`, foreign keys to `vans` and `academic_years`. Stores `commute_mode` (Self, Parent, Van), `parent_id`, and `self_auth` (Pre-Auth boolean).
  6. `CcaCoachCrossRef`: Many-to-many lookup for CCA class coaches.
  7. `StudentCcaEnrollmentCrossRef`: Many-to-many enrollment lookup.
  8. `EntryLogEntity`: Auto-id, `student_id`, timestamp, status (`Entered`).
  9. `CoachAttendanceEntity`: Auto-id, `coach_id`, `cca_id`, timestamp, status (`Present`).
  10. `ClassSessionEntity`: Auto-id, `cca_id`, `date` (unique per CCA per day), `completed_by_coach_id`, `is_completed`.
  11. `StudentAttendanceEntity`: Auto-id, `student_id`, `cca_id`, `date_key`, `status` (`Present`/`Absent`).
  12. `ExitLogEntity`: Auto-id, `student_id`, `gate_id`, `commute_mode`, `override_reason`, `digital_signature_svg`, timestamp.

- **`AppDao.kt`**:
  - Contains Reactive `Flow<Int>` queries for top-bar audit counters (total students, entry count, exit count, completed sessions).
  - Contains CRUD methods for scanning, roster fetching, class session completion, parent sibling grouping, van roster querying, and logging.

### 2.3 Repository & Data Seeding (`com.example.ccagatedispatch.data.repository` & `seed`)
- **`AppRepository.kt`**: Central data store encapsulating database queries and exposing thread-safe Flow streams and suspend functions.
- **`SeedService.kt`**: Operates on cold start. Executes `appDao.getStudentCount()` to check existing records before seeding mock data, guaranteeing active logs/attendance are never overwritten on app process restarts.

### 2.4 Presentation & UDF State Architecture (`com.example.ccagatedispatch.ui`)
- **`BaseViewModel.kt`**:
  - Generic abstract class `BaseViewModel<State : UiState, Event : UiEvent, Effect : UiEffect>` enforcing Unidirectional Data Flow (UDF).
  - Emits read-only `StateFlow<State>` and single-shot events via `Channel<Effect>`.

- **Portal ViewModels & Compose Screens**:
  1. **Main Entrance Logging (`EntryViewModel.kt` / `EntryScreen.kt`)**:
     - Simulates barcode/OCR scanning of student ID cards.
     - Verifies active academic year student registration and inserts record into `entry_logs`.
     - Displays live feed of entry logs with timestamps.
  2. **Coach Portal (`CoachViewModel.kt` / `CoachScreen.kt`)**:
     - Coach check-in logging (`coach_attendance`).
     - Interactive student attendance roster toggling (`Present` / `Absent`).
     - "Complete Class Session" button updates `class_sessions` and opens gate departure window.
  3. **Gate Dispatch Portal (`DispatchViewModel.kt` / `DispatchScreen.kt` & `SignatureCanvas.kt`)**:
     - **Self Commute**: Checks pre-authorization (`self_auth`) and class session completion. Offers guard override dialog with mandatory reason entry.
     - **Parent Pickup**: Scans Parent ID, queries and groups all sibling students under that Parent ID, and allows visual release verification.
     - **Van Handover**: Loads route roster for Driver/Van ID. Features a Jetpack Compose `SignatureCanvas` allowing drivers to sign directly on screen (captured as SVG vector data) prior to batch dispatch.

---

## 3. High-Security Core Workflows

### 3.1 Main Entrance Logging
```
[ Scan / Type Student ID ]
           │
           ▼
[ Query Room DB (StudentEntity) ]
   ├── Found ────► [ Log Entry to entry_logs (Status: Entered) ] ──► [ Update Live Dashboard ]
   └── Not Found ─► [ Display ACCESS DENIED Error ]
```

### 3.2 Secure Exit Dispatch Protocols

#### Self Commute Flow
```
[ Scan Student Card ] ──► [ Check selfAuth & Class Session Completion ]
                                  ├── Authorized ─────► [ Log Exit Gate ]
                                  └── Not Authorized ──► [ Prompt Guard Override Dialog ] ──► [ Log Exit with Reason ]
```

#### Parent Pickup Flow
```
[ Scan Parent ID ] ──► [ Group Sibling Students ] ──► [ Guard Visual Check ] ──► [ Release Individual Student ]
```

#### Van Handover Flow
```
[ Scan Driver / Van ID ] ──► [ Load Route Roster ] ──► [ Boarding Checklist ] ──► [ Driver Signs on SignatureCanvas ] ──► [ Batch Release with SVG Signature ]
```

---

## 4. Verification & R8 Rules Summary

- **R8 Full Mode Rules (`proguard-rules.pro`)**:
  - Preserves all `@Entity` and `@Dao` classes and Room database initializers.
  - Keeps native SQLCipher packages (`net.sqlcipher.**`).
  - Preserves Hilt / Dagger dependency injection entry points and DataStore preference serializers.
- **Air-Gap Verification**:
  - Verified `AndroidManifest.xml` contains zero internet or network-state permissions.
