# Genie

Genie is an Android automation app that converts voice commands into automated actions using GPT-4 and Android’s Accessibility Services.

## Project Structure

Genie/ 
├── app/ │ <br>
├── src/main/java/com/yourcompany/genie/ <br>
│ │ ├── MainActivity.kt <br>
│ │ ├── conversation/ConversationalAgent.kt <br>
│ │ ├── microapp/MicroAppGenerator.kt <br>
│ │ ├── trigger/ContextAwareTrigger.kt <br>
│ │ ├── service/GenieAccessibilityService.kt <br>
│ │ └── utils/{SpeechRecognitionUtil.kt, UIAutomationUtil.kt} <br>
│ ├── res/{layout, values, xml} <br>
│ └── AndroidManifest.xml <br>
├── build.gradle <br>
└── settings.gradle<br>


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

