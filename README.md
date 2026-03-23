# GooseDroid 🪿

An Android virtual pet inspired by [Desktop Goose](https://samperson.itch.io/desktop-goose). A procedurally animated goose lives as a screen overlay — no sprites, everything is drawn with Canvas.

## Features

**Living Overlay** — The goose roams your screen as a system overlay, walking over your apps, honking, and causing mischief.

**Procedural Animation** — Fully rendered with Android Canvas draw calls. Skeletal rig system with bones, expressions, poses, and foot IK. No bitmaps for the goose itself.

**AI Behaviors** — Priority-based behavior tree drives decisions. The goose wanders, reacts to touch, sleeps, eats, plays, trolls you, and adapts to time of day, battery level, and inactivity.

**Tamagotchi Needs** — Hunger, energy, and happiness decay over time. Neglect the goose and it'll let you know. Critical needs trigger notifications.

**Touch Interactions** — Pet, drag, throw, boop, tickle, belly rub, and more. Each gesture has unique responses and affects personality.

**Personality Evolution** — Traits like playfulness, affection, bravery, and mischief evolve based on how you interact. Range from -100 to +100.

**Evolution Stages** — Egg → Hatchling → Gosling → Adult → Elder → Legendary → Cosmic. Your goose grows over time.

**Mini-Games** — Feeding, chasing, catching, hide & seek, honk hero, and memory honk.

**Visual Effects** — Particle systems for hearts, sparkles, confetti, dust trails, and more.

**Sound System** — Extended honk types, footsteps, and emotional sounds.

**Easter Eggs** — Secret modes, hidden interactions, and the Konami code.

**Trolling** — Fake notifications, vibration, and troll messages. It's a goose, after all.

**Home Screen Widget** — Quick feed/play/sleep actions without opening the app.

**Customization** — Name your goose, change colors, pick hats, accessories, and creature types.

## Screenshots

> Coming soon

## Architecture

```
TheGoose (Central coordinator)
├── GooseAI             — Modular behavior system with memory and routines
├── GooseBehaviorTree   — Priority-based: critical needs → interaction → personality → idle
├── GoosePhysics        — Movement, jumping, bouncing, friction, surface types
├── GooseRig            — Skeletal system: bones, expressions, poses
├── GooseRenderer       — Procedural drawing, particles, trails, glow
├── GooseTouchHandler   — Gesture recognition (tap, pet, drag, throw, boop, tickle...)
├── GooseLLM            — On-device template-based thought generation
├── GooseVisualEffects  — Particle effects (hearts, sparkles, confetti, dust)
├── GooseSoundEffects   — Honks, footsteps, emotional sounds
├── GooseDreams         — Sleep thought bubbles based on daily experiences
├── GooseEasterEggs     — Secret modes and hidden interactions
├── GooseTrolling       — Fake notifications, vibration, troll messages
├── GooseSystemReactions— Reacts to battery, time of day, inactivity
└── MiniGames           — Feeding, chasing, hide & seek, honk hero, memory honk

Pet Systems:
├── PetNeeds            — Hunger, energy, happiness with decay rates
├── PetPersonality      — Evolving traits [-100..+100]
├── GooseEvolution      — Growth stages from Egg to Cosmic
├── PetAppearance       — Colors, hats, accessories, creature types
└── PetWidget           — Home screen widget
```

## Tech Stack

| | |
|---|---|
| **Language** | Java 11 |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 36 |
| **Build** | Gradle + AGP 8.12.0 |
| **UI** | Android Canvas (procedural rendering) |
| **AI** | Custom behavior tree + decision engine |
| **Physics** | Custom physics engine with IK |
| **Audio** | SoundPool + MediaPlayer |

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires keystore config in local.properties)
./gradlew assembleRelease

# Run tests
./gradlew test
```

The APK will be generated in `app/build/outputs/apk/`.

## Installation

1. Build the APK or download from releases
2. Install on your Android device
3. Grant overlay permission when prompted
4. Configure your pet in the settings panel
5. Toggle the goose on!

## Permissions

| Permission | Purpose |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Screen overlay |
| `VIBRATE` | Haptic feedback & trolling |
| `POST_NOTIFICATIONS` | Pet need alerts |
| `SCHEDULE_EXACT_ALARM` | Timed reminders |
| `FOREGROUND_SERVICE` | Keep goose alive |
| `RECEIVE_BOOT_COMPLETED` | Auto-start on boot |

## Troubleshooting

1. **Goose not appearing**: Check that overlay permission is granted in Settings > Apps > GooseDroid > Display over other apps
2. **Touch not working**: Enable "Touchable" option in the app settings
3. **App crashes**: Create an issue with logs from the error dialog

## License

All rights reserved. This source code is provided for educational and reference purposes.
