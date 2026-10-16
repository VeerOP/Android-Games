# Games Hub 🎮

A collection of classic games built natively for Android using modern **Jetpack Compose**, **Kotlin Coroutines**, and **Material Design 3**.

---

## 🕹️ Featured Games

| Game | Description | Key Features |
| :--- | :--- | :--- |
| **Tic-Tac-Toe** | Classic 2-player turn-based board game. | Turn indicator, win line animations, score tracking, dynamic board reset. |
| **Flappy Bird** | Arcade physics game with continuous gravity and jump mechanics. | Real-time 60 FPS coroutine loop, procedural pipe generation, parallax clouds, high score tracking. |
| **Snake** | Retro grid-based snake game. | Swipe gesture + D-Pad controls, dynamic speed acceleration, collision physics, score tracking. |
| **Memory Match** | Card-matching memory puzzle. | 3D flip card animations, Easy (4x4) and Hard (6x6) modes, timer and move counters. |
| **2048** | Classic sliding tile puzzle game. | Swipe-driven tile slide and merge logic, undo feature, score and best score tracking. |

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin (2.2+)
- **UI Toolkit:** Jetpack Compose with Material Design 3
- **Architecture:** MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
- **State Management:** `StateFlow`, Compose `mutableStateOf`, Coroutines
- **Navigation:** Jetpack Navigation Compose
- **Build System:** Gradle (Kotlin DSL `build.gradle.kts`) with Version Catalogs (`libs.versions.toml`)
- **Compatibility:** Min SDK 24 (Android 7.0+) / Target SDK 37

---

## 📁 Project Structure

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/example/games/
│   ├── MainActivity.kt                # Root activity & Navigation Host
│   ├── Screen.kt                      # Sealed route definitions
│   └── ui/
│       ├── home/
│       │   └── HomeScreen.kt          # Hub menu displaying all games
│       ├── games/
│       │   ├── tictactoe/             # Tic-Tac-Toe (Screen + ViewModel)
│       │   ├── flappybird/            # Flappy Bird (Screen + ViewModel)
│       │   ├── snake/                 # Snake Game (Screen + ViewModel)
│       │   ├── memorymatch/           # Memory Match (Screen + ViewModel)
│       │   └── game2048/              # 2048 Puzzle (Screen + ViewModel)
│       └── theme/                     # Material 3 colors, typography, theme
└── res/                               # App drawables, mipmaps, strings, and xml configs
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Ladybug / Meerkat or later recommended)
- **JDK 21** configured for Gradle
- Android device or emulator running **API 24 or higher**

### Clone & Run
```bash
# 1. Clone the repository
git clone https://github.com/VeerOP/Android-Games.git
cd Android-Games

# 2. Open in Android Studio
# Allow Gradle to sync dependencies and download SDK components

# 3. Build & Install Debug APK
./gradlew assembleDebug
```

---

## 🤝 Team Collaboration & Forking Guide

To collaborate and propose changes without stepping on each other's work, follow this Git workflow:

### 1. Fork the Repository
Click the **Fork** button at the top-right of the GitHub repository page to create a personal copy under your account.

### 2. Clone Your Fork
```bash
git clone https://github.com/<your-username>/Android-Games.git
cd Android-Games
```

### 3. Add the Original Repository as Upstream
```bash
git remote add upstream https://github.com/VeerOP/Android-Games.git
git fetch upstream
```

### 4. Create a Feature Branch
Always work on a dedicated branch named after your task or game:
```bash
git checkout -b feature/<game-or-feature-name>
# Examples:
# git checkout -b feature/chess-game
# git checkout -b feature/flappy-sound-effects
# git checkout -b feature/dark-mode-tweaks
```

### 5. Commit & Push
```bash
git add .
git commit -m "feat(game): add sound effects to Flappy Bird"
git push -u origin feature/<game-or-feature-name>
```

### 6. Create a Pull Request (PR)
1. Navigate to the original repository on GitHub.
2. Click **New Pull Request** -> **Compare across forks**.
3. Select your feature branch and submit for review.

### 7. Keeping Your Fork Updated
```bash
git checkout main
git pull upstream main
git push origin main
```
