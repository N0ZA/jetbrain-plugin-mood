# 🧠 CodeMood: The Emotional Intelligence Layer for IntelliJ

**CodeMood** is an IntelliJ IDEA plugin that monitors your mental state while you code. It doesn't just check your syntax; it checks *you*.

By analyzing your behavioral patterns (typing speed, deletion rates, pauses), CodeMood detects when you are in **Flow**, **Frustrated**, or **Fatigued**, and provides real-time interventions to keep you productive and healthy.

---

## ✨ Features

### 1. 🕵️ Passive Monitoring ("The Senses")
CodeMood sits quietly in the background, respecting your privacy.
- **Keystroke Dynamics:** Monitors your typing rhythm (WPM).
- **Frustration Detection:** Detects "rage deleting" (rapid backspacing), a key sign of struggle.
- **Privacy First:** No code is sent to the cloud. Analysis happens locally on your machine.

### 2. 📊 The Dashboard ("The Brain")
Open the **CodeMood** tool window to see your mind on the screen.
- **Live WPM Graph:** Watch your focus spike and dip in real-time.
- **Stats Panel:** Track your Average WPM and Deletion Rate.
- **Pomodoro Timer:** Integrated focus timer to structure your sessions.

### 3. 🛡️ Active Interventions ("The Care")
When you go off-track, CodeMood nudges you back.
- **😤 Frustrated?** If you start rage-deleting, a **Breathing Exercise** dialog automatically pops up to help you reset.
- **👀 Eye Strain?** The **20-20-20 Rule** enforcer reminds you every 20 minutes to look away from the screen.
- **📉 Low Motivation?** Click the **AI Motivation** button to get a context-aware tip from GPT-3.5 based on your current mood.

### 4. 🕰️ History ("The Memory")
- **Daily Persistence:** Your stats (Total Minutes, Avg WPM) are saved locally so you can track your productivity trends over time.

---

## 🚀 Setup Guide

### Prerequisites
- **Java 17 JDK** installed.
- **IntelliJ IDEA** (2023.2 or later).
- *(Optional)* **OpenAI API Key** for the "AI Motivation" feature.

### Installation

1.  **Open the Project:**
    - Open IntelliJ IDEA.
    - Select **Open** and point to the `jetbrain-plugin-mood` folder.
    - Wait for Gradle to sync (it sends the IntelliJ Platform SDK).

2.  **Run the Plugin:**
    - Open the **Gradle** side panel (right-hand side).
    - Navigate to `Tasks` -> `intellij` -> `runIde`.
    - Double-click `runIde`.
    - A new **Sandbox IntelliJ** window will open with the plugin installed.

### Configuration (AI Feature)
1.  In the *Sandbox* IDE, go to **Settings/Preferences** -> **Tools** -> **CodeMood**.
2.  Enter your **OpenAI API Key**.
3.  Check "Enable AI Motivation".

---

## 🧪 How to Test It

### ✅ Test 1: The Status Bar
Look at the bottom-right of the Sandbox IDE. You should see **"Mood: 😐 Neutral"**.

### ✅ Test 2: Triggering "Flow"
Type in a file continuously for 30 seconds without deleting.
- **Result:** Status Bar changes to **"Mood: ⚡ Flow"**.

### ✅ Test 3: Triggering "Frustration"
Simulate making a mistake and "rage delete" (press Backspace >20 times quickly).
- **Result:**
    - Status Bar changes to **"Mood: 😤 Frustrated"**.
    - A **Breathing Exercise** dialog pops up.

### ✅ Test 4: The Dashboard
Open the **CodeMood** tool window (sidebar).
- Watch the line graph update as you type.
- Click **"Get AI Motivation 🤖"** to see a generated tip.

---

## 📂 Project Structure
- `src/main/kotlin/com/antigravity/codemood/services/MoodAnalysisService.kt`: The core logic engine.
- `src/main/kotlin/com/antigravity/codemood/ui/CodeMoodToolWindowFactory.kt`: The Dashboard UI.
- `src/main/kotlin/com/antigravity/codemood/listeners/DeletionActionListener.kt`: The frustration sensor.
