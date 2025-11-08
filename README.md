# Android Notes App (Kotlin + Firebase)

A clean, modern notes app for Android written in Kotlin, featuring Firebase Authentication, Cloud Firestore storage, full CRUD operations on notes, on-device search, multi-select & batch delete, and a polished Glassmorphism UI with gradient accents.

---

## Demo Screenshots

| Login | Home | Add / Edit |
|-------|------|------------|
| <img src="screenshots/Screenshot_20251109_002218_Notes App.jpg" width="260" alt="Login screen" /> | <img src="screenshots/Screenshot_20251109_002206_Notes App.jpg" width="260" alt="Home screen" /> | <img src="screenshots/Screenshot_20251109_001847_Notes App.jpg" width="260" alt="Add/Edit Note" /> |

---

## Features

### 🧾 Full CRUD on Notes
- **Create**: Floating Action Button opens the editor
- **Read**: Live list backed by Firestore snapshot listener
- **Update**: Tap any note to edit title/content/category
- **Delete**: Long-press to enter selection mode and batch delete (one or many) from Home

### 🔎 Instant On-Device Search
Filters the in-memory list locally (no Firestore query costs)

### 🏷️ 5 Built-in Categories (color-coded)
PERSONAL, WORK, STUDY, IDEAS, OTHER — saved on the note and visualized via gradients

### ⏱️ Human-friendly Timestamps
"Just now", "5 minutes ago", "Yesterday", … falling back to formatted dates

### 🔐 Email/Password Auth (Firebase Auth)
Clear validation, inline errors, and smooth IME actions

### Modern UI/UX (Glassmorphism & friends)
- ✨ Glassmorphism surfaces (translucent cards, subtle borders, soft blur-like feel)
- 🎨 Gradient accents for categories + decorative background blobs
- 💡 Ripple feedback on interactive elements; rounded icon containers
- 🧰 Material components with sensible elevation and shadows
- 🧲 Polished selection mode: check overlay + scrim, animated toolbar, FAB hide/show
- 🧭 Action-bar-free header with an inline Logout icon
- ♿ Thoughtful touches: hint/secondary text contrast, hit targets, IME "Go" for faster flows

---

## How It Works

- **Authentication**: Users sign in with email/password via Firebase Auth
- **Notes**: Stored under each user in Cloud Firestore (`/users/{uid}/notes/{noteId}`)
- **CRUD**:
  - **Create/Update**: `AddNoteActivity` handles editor + category picker (5 chips)
  - **Read**: `MainActivity` listens to Firestore; UI updates instantly
  - **Delete**: Long-press any note → selection mode → delete one or multiple
- **On-device search**: The adapter maintains a master list and filters locally when typing
- **UI State**: `ListAdapter` + `DiffUtil` for smooth updates; adapter manages selection state

---

### Firestore layout (per user):

```
/users/{uid}/notes/{noteId} {
    title: string
    content: string
    timestamp: Timestamp
    category: "PERSONAL" | "WORK" | "STUDY" | "IDEAS" | "OTHER"
}
```

---

## Getting Started

### Prerequisites
- Android Studio (latest stable) with Kotlin
- A Firebase project

### Firebase Setup

1. Add an Android app in Firebase Console using your package name (e.g., `com.android.app.notesapp`)
2. Download `google-services.json` to `app/`
3. Enable **Authentication → Email/Password**
4. Create a **Cloud Firestore database** (Native mode)

### Security Rules (starter)

For `/users/{uid}/notes/{noteId}` collections:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/notes/{noteId} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

### Run

1. Open the project in Android Studio
2. Sync Gradle
3. Run on device/emulator

> If targeting older devices, ensure:
> ```gradle
> android { 
>   defaultConfig { 
>     vectorDrawables.useSupportLibrary = true 
>   } 
> }
> ```

---

## Implementation Notes

- **Local Search**: The adapter's `applyFilter(query)` filters a cached list and calls `submitList(filtered)`
- **Multi-Select**: Adapter tracks selected IDs; item shows check icon + scrim when selected
- **Batch Delete**: `MainActivity` uses Firestore `batch.delete()` across selected IDs
- **Category UI**: Category → gradient drawable (used for the left color bar and editor color chips)
- **Timestamps**: Relative labels with a clean fallback to `MMM dd, yyyy`

---

## Roadmap

- [ ] Category filter chips on Home (quick filter)
- [ ] Reminders / due dates
- [ ] Rich text / checklists
- [ ] Offline cache & conflict strategy
- [ ] Export / import

---

## Contributing

PRs welcome!

1. Fork the repo
2. Create a branch: `git checkout -b feat/your-idea`
3. Commit: `git commit -m "feat: your idea"`
4. Push: `git push origin feat/your-idea`
5. Open a Pull Request with context + screenshots (for UI)

---

## License

MIT — see [LICENSE](LICENSE)
