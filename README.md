# Genie

Genie is an Android automation app that converts voice commands into automated actions using GPT-4 and Android’s Accessibility Services.

## Project Structure
```
Genie/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/yourcompany/genie/
│   │   │   │   ├── MainActivity.java # input user voice
│   │   │   │   ├── conversation/
│   │   │   │   │   └── ConversationalAgent.java
│   │   │   │   ├── model/
│   │   │   │   │   └── ChatRequestBody.java
│   │   │   │   │   └── ChatResponseObject.java
│   │   │   │   ├── network/
│   │   │   │   │   └── JsonApi.java
│   │   │   │   ├── microapp/
│   │   │   │   │   └── MicroAppGenerator.kt
│   │   │   │   ├── trigger/
│   │   │   │   │   └── ContextAwareTrigger.kt
│   │   │   │   ├── service/
│   │   │   │   │   └── GenieAccessibilityService.kt
│   │   │   │   └── utils/
│   │   │   │       ├── GPTApiClient.kt # call gpt api
│   │   │   │       ├── SessionContext.kt
│   │   │   │       ├── SpeechRecognitionUtil.kt
│   │   │   │       └── UIAutomationUtil.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   └── activity_main.xml
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── styles.xml
│   │   │   │   └── xml/
│   │   │   │       └── accessibility_service_config.xml
│   │   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
└── settings.gradle
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

