# SleepSafe

SleepSafe is an Android application designed to help users track, analyze, and improve their sleep quality. Using advanced motion and audio detection, the app provides comprehensive sleep analysis while ensuring user privacy and data security.

## Features

- **Sleep Tracking**
  - Automatic sleep phase detection
  - Motion tracking
  - Ambient sound monitoring
  - Real-time sleep quality analysis

- **Smart Alarm System**
  - Customizable wake-up times
  - Snooze functionality
  - Gentle wake-up optimization

- **Sleep Analysis**
  - Detailed sleep phase statistics
  - Sleep quality metrics
  - Historical sleep data visualization
  - Personalized sleep insights

- **Privacy-Focused**
  - Local data storage
  - Minimal permissions required
  - No cloud data transmission
  - Secure data handling

## Architecture

SleepSafe follows the MVVM (Model-View-ViewModel) architecture pattern and is built with modern Android development practices:

```
com.example.sleepsafe/
├── components/           # Reusable UI components
├── data/                # Data models and database operations
├── screens/             # UI screens using Jetpack Compose
├── ui/                  # Theme and styling
├── utils/              # Utility classes and helpers
└── viewmodel/          # ViewModels for each screen
```

### Key Components

- **SleepTrackingService**: Core service handling sleep monitoring
- **MotionDetector**: Processes device motion for sleep phase detection
- **AudioRecorder**: Handles ambient sound monitoring
- **SleepDatabase**: Local data persistence using Room
- **ViewModels**: Handle business logic and state management

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture Components**
  - ViewModel
  - Room Database
  - Navigation Component
  - LiveData/StateFlow
- **Concurrency**: Kotlin Coroutines
- **Dependency Injection**: Hilt (Android)
- **Build System**: Gradle (Kotlin DSL)

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- Android SDK 21 or higher
- Kotlin 1.5.0 or higher

### Setup

1. Clone the repository:
```bash
git clone https://github.com/dowells-mike/sleepsafe.git
```

2. Open the project in Android Studio

3. Sync project with Gradle files

4. Run the app on an emulator or physical device

### Required Permissions

The app requires the following permissions:
- `ACTIVITY_RECOGNITION` - For motion detection
- `RECORD_AUDIO` - For ambient sound monitoring
- `FOREGROUND_SERVICE` - For sleep tracking service
- `SCHEDULE_EXACT_ALARM` - For alarm functionality

## Screenshots

<div align="center">
  <img src="![home](https://github.com/user-attachments/assets/e6f45a5f-8f84-42ec-bad6-febfd0fc831c)" width="250" alt="Home Screen" />
  &nbsp;&nbsp;&nbsp;
  <img src="[analysis](https://github.com/user-attachments/assets/b3d7c353-fdb2-42a5-9340-afa9a67acad0)" width="250" alt="Analysis Screen" />
  &nbsp;&nbsp;&nbsp;
  <img src="[settings](https://github.com/user-attachments/assets/fc4950ba-fe7f-4204-850e-61b4c30522b8)" width="250" alt="Settings Screen" />
</div>

## Contributing

We welcome contributions to SleepSafe! Here's how you can help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow Kotlin coding conventions
- Use meaningful names for functions and variables
- Write unit tests for new features
- Document public APIs
- Keep functions small and focused

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Android Jetpack](https://developer.android.com/jetpack)
- [Kotlin](https://kotlinlang.org/)
- [Material Design](https://material.io/design)

## Contact

Project Link: [https://github.com/yourusername/sleepsafe](https://github.com/dowells-mike/sleepsafe)
