# Video Production Workflow: AI Logistics Hub

This document outlines the automated recording sequence for the promotional video. Antigravity will execute these steps using the browser subagent to produce a high-quality video artifact.

## 🎬 Recording Sequence

### Scene 1: The Clean Slate (0:00 - 0:10)
1. **Navigate** to `http://localhost:8080`.
2. **Action**: Ensure the dashboard is in a "Clean State" (0 records). If records exist, click **"Reset Database"** first.
3. **Outcome**: Visual of the empty, glassmorphic dashboard with the "Live Monitoring Active" pulse.

### Scene 2: The Moment of Production (0:10 - 0:25)
1. **Action**: Click the **"Run Demo Scenarios"** button.
2. **Wait**: Allow 5-10 seconds for the AI to process all 5 files from `demo-assets`.
3. **Outcome**: Table populates live with IDs #1 through #5.
4. **Highlight**: Hover over an **URGENT** badge (e.g., Red status) to show interactivity.

### Scene 3: The Custom Extraction (0:25 - 0:45)
1. **Navigate** to `http://localhost:8080/swagger-ui/index.html`.
2. **Action**: Find `POST /api/send/ai/extract` in the "Sending Operations" section.
3. **Input**: Click "Try it out" and paste the following text:
   ```text
   URGENT: CRITICAL Port Blockage at Rotterdam. Shipment #AX-99 blocked by strike. Rescue cost estimate: $125,000. Status: DELAYED.
   ```
4. **Execute**: Click the big blue button.
5. **Outcome**: Show the structured JSON response appearing instantly in the Swagger console.

### Scene 4: The Integration Proof (0:45 - 0:55)
1. **Navigate** back to the Dashboard.
2. **Action**: Show record #6 appearing in the list.
3. **Commentary**: Mentions that Slack notifications have been dispatched automatically (as per `AIService` logic).

### Scene 5: Closing Shot (0:55 - 1:00)
1. **Action**: Scroll the populated dashboard from bottom to top, framing all extraction records as a final wide shot.
2. **Outcome**: Clean ending on the glassmorphic dashboard with all records visible and the "Live Monitoring Active" pulse.

> **Note**: Showing `AIService.java` in an IDE is not implementable via Playwright (a browser-only tool). The code architecture is covered in the README and `docs/ai-integration.md` for reviewers who want the technical deep-dive.

## 🛠️ Recording Configuration
- **Recording Name**: `ai_logistics_hub_showcase`
- **Resolution**: Standard browser viewport.
- **Duration**: Target ~60 seconds.

---
**Note to Model**: Run these steps in a single `browser_subagent` session if possible, or sequential steps to ensure one cohesive `webp` recording.

## 🤖 Current Automation Approach

Since the browser subagent recordings were taking too long and experiencing timeouts attempting a single cohesive take, we are currently generating the recording via a **Node.js Playwright script**. 

This script natively connects to `localhost:8080`, executes the requested interactions exactly as outlined above, and leverages Playwright's built-in `recordVideo` functionality to reliably export a `.webm` file into the `video-recorder/videos` directory.

### ⚠️ Prerequisites Before Running Any Script

> These must be satisfied **before** running `npm run build-video` or `npm run record`.

**1. Start the backend with the `demo` Spring profile:**
```bash
# From the project root
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```
The `demo` profile is required to expose `/api/demo/reset` and `/api/demo/populate`.  
`generate_audio.js` does **not** need the backend — only `record.js` does.  
Running `npm run build-video` without the backend will generate audio successfully, then abort at the recording step.

**2. Install FFmpeg** (required by `merge_video_audio.js`):
```bash
sudo apt install ffmpeg   # Ubuntu/Debian
brew install ffmpeg       # macOS
```

### ⚠️ The "Sound & Narrative" Problem
The automated Playwright approach has two major limitations for a promotional showcase:
1. **No Audio**: The generated clips are completely silent.
2. **Technical Pacing**: The script executes interactions with technical precision but lacks the natural pacing and "storytelling" feel of a manual demo.

### 🎙️ Automatización de Audio (Generación y Mezcla)
Para evitar procesos manuales, hemos implementado una pipeline de audio:
1. **Generación (TTS)**: `video-recorder/generate_audio.js` crea los archivos `.mp3` utilizando textos acortados para lograr una narrativa fluida.
2. **Mezcla Inteligente**: `video-recorder/merge_video_audio.js` utiliza **FFmpeg** para:
    - **Sincronizar**: Inyecta los audios en los tiempos exactos del video usando audios ajustados, garantizando velocidad de voz natural sin solapamientos.
    - **Resultado**: Un video final `final_showcase_with_audio.webm` con narrativa fluida y natural.

## 📤 Published demo

The narrated showcase is on YouTube: **[https://youtu.be/TULulfYLYKE](https://youtu.be/TULulfYLYKE)** (linked from the root `README.md`).

Local build artifacts remain under `video-recorder/videos/` and are **gitignored**. Optional: attach `final_showcase_with_audio.webm` to a GitHub Release for direct download.
