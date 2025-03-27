# Genie

Genie is an Android automation app that converts voice commands into automated actions using GPT-4 and Android’s Accessibility Services.

## Project Structure
```
Genie/ <br>
├── app/<br>
├── src/<br>
│   └── main/<br>
│       └── java/<br>
│           └── com/<br>
│               └── example/<br>
│                   └── genie/<br>
│                       ├── MainActivity.kt<br>
│                       ├── conversation/<br>
│                       │   └── ConversationalAgent.kt<br>
│                       ├── microapp/<br>
│                       │   └── MicroAppGenerator.kt<br>
│                       ├── trigger/<br>
│                       │   └── ContextAwareTrigger.kt<br>
│                       ├── service/<br>
│                       │   └── GenieAccessibilityService.kt<br>
│                       └── utils/<br>
│                           ├── SpeechRecognitionUtil.kt<br>
│                           └── UIAutomationUtil.kt<br>
├── res/<br>
│   ├── layout/<br>
│   ├── values/<br>
│   └── xml/<br>
├── AndroidManifest.xml<br>
├── build.gradle<br>
└── settings.gradle<br>
```
## Modules

- **Conversational Agent:** Captures voice commands, gathers context, and uses a two-step GPT-4 process to decide on an action and target UI element.
- **Micro-App Generator:** Converts GPT-4 outputs into actionable automation scripts.
- **Context-Aware Trigger:** Monitors user activity and displays overlay prompts for automation.

## Installation & Setup

1. Clone the repository.
2. Open the project in Android Studio.
3. Run Gradle sync and build the app.
4. Configure Android Manifest and GPT-4 API keys as needed.

## Usage

- Launch the app and tap “Speak” to issue a command (e.g., “Open Calculator and type 123”).
- Genie processes your command and performs the corresponding UI actions automatically.

