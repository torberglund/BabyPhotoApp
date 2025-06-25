Baby Daily Photo App - Specification and Execution Plan
________________________________________
📌 Purpose
Create an Android app used by two parents to capture one curated photo per day of their newborn. Each photo is framed consistently using face detection and can be marked as the “active” shot for the day. Photos are stored on a shared NAS and used to build a time-lapse movie.
________________________________________
🎯 Goals
•	Take 1 picture/day with consistent framing.
•	Allow selection of 1 “active” photo/day (for the time-lapse).
•	Sync all photos to user-owned NAS with no vendor lock-in.
•	Build a movie using only “active” photos.
________________________________________
✅ Feature Specification
1. Capture Flow
•	App launches directly into CameraX Preview (locked 3:4).
•	Overlay shows: Oval aligned to child’s face using on-device ML Kit.
•	Status lamp indicates if today’s photo is already taken (green) or not (red).
•	Pressing the shutter:
o	Captures image
o	Saves to local storage: filesDir/photos/YYYY/MM/DD/HHmmss.jpg
o	Proceeds to Review screen
2. Review Screen
•	Displays all photos taken today in a 3-column grid.
•	Tap to select active photo (default = most recent).
•	Others marked passive.
•	Filenames include status:
o	[yymmdd-hhmmss-active].jpg
o	[yymmdd-hhmmss-passive].jpg
3. Sidebar Menu
•	Calendar Picker: browse/select active photo by date
•	Watch Movie: opens playback module
•	Sync Now: manual force sync
•	Settings: NAS credentials, overlay tuning, default FPS
4. Movie Playback
•	Loads all active photos in chronological order
•	Framerate options: 1 / 2 / 5 / 10 / 20 / 30 fps
•	Uses ExoPlayer or custom SurfaceView
•	Images pre-fetched into memory for smooth playback
________________________________________
💾 Storage & Sync
NAS (Preferred)
•	Protocol: SFTP (or fallback WebDAV)
•	Photo path: /volume1/BabyPhotos/photos/YYYY/MM/DD/HHmmss.jpg
•	Metadata path: /volume1/BabyPhotos/index.json
index.json Schema
{
  "version": 1,
  "days": {
    "2025-06-24": {
      "active": "20250624_091400.jpg",
      "shots": [
        {
          "file": "20250624_081215.jpg",
          "device": "dad-pixel8",
          "status": "passive"
        },
        {
          "file": "20250624_091400.jpg",
          "device": "mom-iphone14",
          "status": "active"
        }
      ]
    }
  }
}
•	App syncs index.json on launch, photo upload, and active selection
________________________________________
🧱 Architecture
Android App
•	Kotlin (JVM 11), Jetpack Compose UI
•	CameraX, Navigation Compose, Coil
•	Offline-first logic with Room DB mirroring index.json
Backend
•	NAS with SFTP access and defined directory layout:
BabyPhotos/
├── index.json
└── photos/
    └── 2025/06/24/
        ├── 20250624_081215-passive.jpg
        └── 20250624_091400-active.jpg
________________________________________
📆 Execution Plan
✅ Step 1: Camera MVP
•	Set up CameraX preview with fixed 3:4 aspect ratio
•	Implement shutter button and photo capture
•	Save photo to internal filesDir/photos/yyyy/mm/dd/
•	Transition to review screen after capture
•	Load and display today’s photos in a 3-column grid
🟡 Step 2: Active Selection + Metadata
•	Tap photo in review grid to mark as “active”
•	Write selected filename to index.json under today’s date
•	Show status lamp on viewfinder screen (green if active photo exists today)
•	Display active badge on selected photo
•	Persist and reload selection across launches
🟡 Step 3: NAS Integration
•	Authenticate to Synology NAS using WebDAV or SFTP
•	Upload captured photos to BabyPhotos/photos/yyyy/mm/dd/
•	Upload updated index.json to NAS root
•	On app launch, download and cache index.json
•	Handle conflicts by preferring latest modified version
•	Retry failed uploads with exponential backoff
🟡 Step 4: Archive & Movie UI
•	Implement sidebar navigation drawer
•	Add calendar/date-picker to browse by day
•	Load photos from any selected day for review
•	Build and preview movie using only “active” photos
•	Add FPS control slider (1–30 fps)
•	Preload images into memory for smooth playback
•	(Optional) Export movie to MP4
________________________________________
🧪 Optional Enhancements
•	Face alignment heatmap overlay
•	Multi-child support via subfolders
•	Web viewer from NAS
•	iOS companion app
•	Movie export automation
________________________________________
🔐 Security & Offline
•	App sandbox for all local files
•	SFTP keypair auth, fallback password w/ biometric
•	Retry sync if offline (exponential backoff)
•	Conflicts flagged for manual review
________________________________________
🧪 Testing Strategy
•	Unit: filename, JSON merge, Room
•	Instrumented: Camera flow, flaky sync
•	UI: Espresso for Active toggle, lamp
•	Load: 10 years × 365 = ~14 GB photos
•	Playback: 3,000 frames @ 30 fps stress test
________________________________________
📅 Timeline
Week	Milestone
1	CameraX MVP capture & local review
2	Face detection overlay
3	NAS upload & index.json sync
4	Sync down + Calendar UI
5	Movie playback module
6	Polish, test, Play Store deploy
________________________________________
End of Specification v1
