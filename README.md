# 🤖 AICompanion

AICompanion is a modern multi-model AI chatbot Android application built using **Kotlin**, **Jetpack Compose**, **Firebase**, and **OpenRouter API**.

The app allows users to chat with multiple AI models, manage conversations, share chats through a web page, switch themes, and enjoy a smooth AI-powered experience through a clean and modern interface.

---

# 📱 Application Preview

| Splash Screen | Login Screen |
|---------------|-------------|
| <img src="screenshots/splash_screen.jpg" width="250"/> | <img src="screenshots/login_screen.jpg" width="250"/> |

| Chat Interface | Model Guide |
|---------------|-------------|
| <img src="screenshots/chat_screen.jpg" width="250"/> | <img src="screenshots/model_guide.jpg" width="250"/> |

| Chat Management Drawer | Shared Chat Webpage |
|------------------------|---------------------|
| <img src="screenshots/sidebar.jpg" width="250"/> | <img src="screenshots/shared_chat.jpg" width="250"/> |

---

# ✨ Features

## 🤖 Multi-Model AI Support

AICompanion provides access to multiple specialized AI models through a single interface.

### 🚀 GPT-4o Mini
- General conversations
- Fast responses
- Android development help
- Everyday questions

### 🧠 Claude Haiku
- Writing assistance
- Document understanding
- Summaries
- Long-form explanations

### 💻 DeepSeek
- Coding assistance
- Debugging
- Data Structures & Algorithms
- Competitive Programming

### 🦙 Llama 3.3
- Reasoning tasks
- Brainstorming
- General knowledge
- Problem solving

---

## 💬 Smart Chat Experience

- Real-time AI conversations
- Switch AI models instantly
- Regenerate responses
- Copy messages
- Text-to-Speech support
- Clean message interface
- Temporary Chat Mode

---

## 📂 Chat Management

- Create new conversations
- Search chat history
- Rename conversations
- Pin important chats
- Unpin chats
- Delete chats

---

## 🔗 Chat Sharing

Generate a public web page for conversations and share them with others.

Shared chats are displayed through a responsive web interface with preserved conversation history.

---

## 🎨 Modern UI

- Material Design inspired interface
- Dark Mode support
- Light Mode support
- Responsive layouts
- Modern purple-themed design
- Smooth user experience

---

## 🔐 Authentication

Secure user authentication powered by Firebase.

### Features
- User Registration
- Login
- Persistent Sessions
- Secure Authentication Flow

---

# 🛠️ Tech Stack

## Frontend
- Kotlin
- Jetpack Compose
- Material 3

## Backend & Cloud
- Firebase Authentication
- Firebase Firestore
- Firebase Storage

## AI Integration
- OpenRouter API
- GPT-4o Mini
- Claude Haiku
- DeepSeek
- Llama 3.3

## Networking
- Retrofit
- Gson Converter
- OkHttp

## Async Processing
- Kotlin Coroutines

---

# 🏗️ Architecture

```text
UI Layer (Jetpack Compose)
            │
            ▼
      View Logic
            │
            ▼
      Retrofit Client
            │
            ▼
      OpenRouter API
            │
            ▼
       AI Models
```

Firebase services handle:

- Authentication
- User Data
- Chat Storage
- Shared Conversations

---

# 🚀 Getting Started

## Clone Repository

```bash
git clone https://github.com/Salonniii/AICompanion.git
```

## Open Project

Open the project using Android Studio.

## Configure API Keys

Add your keys to:

```properties
local.properties
```

```properties
OPENROUTER_API_KEY=YOUR_API_KEY
```

## Firebase Setup

1. Create a Firebase project.
2. Add an Android application.
3. Download `google-services.json`.
4. Place it inside:

```text
app/google-services.json
```

## Run Application

```bash
Build → Run
```

---

# 📁 Project Structure

```text
AICompanion
│
├── app
│   ├── authentication
│   ├── ui
│   ├── network
│   ├── theme
│   ├── firebase
│   └── utils
│
├── screenshots
│
└── README.md
```

---

# 🔮 Future Improvements

- Voice Commands
- Speech-to-Text
- AI Image Generation
- Chat Export
- Advanced Chat Search
- More AI Models
- Custom Themes

---

# 👩‍💻 Developer

### Saloni Gupta

Computer Science Student | Android Developer | AI Enthusiast

**GitHub:** https://github.com/Salonniii

**LinkedIn:** www.linkedin.com/in/saloni-gupta-7840a92b9

---

# ⭐ Support

If you found this project useful, consider giving it a ⭐ on GitHub.

Your support motivates future improvements and new features.

---

### Made with ❤️ using Kotlin, Jetpack Compose, Firebase & OpenRouter API.
