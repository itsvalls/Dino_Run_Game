#  Dino Run Game  — Java OOP Edition


## Project Overview

**Dino Run Game ** is a thrilling expansion of the classic Chrome Dino game, built entirely using **Java** and structured with powerful **Object-Oriented Programming (OOP)** principles. It introduces new gameplay mechanics, improved visuals, and clean modular design.

>  Dodge cactuses, fly past birds, collect coins, activate shields — and survive the endless desert!

---

##  Key Features

-  **Bird Obstacles** — Flying enemies for added challenge
-  **Shield Power-ups** — Temporary invincibility
-  **Coin Collection** — Score-boosting collectibles
-  **OOP Design** — Modular, scalable structure
-  **High Score Tracking** — File I/O for persistent scores
-  **Collision Detection** — Event-based interactions
-  **Sound Management** — Background music & FX

---

##  Architecture & Folder Structure

### 📁 `game_object/`
Defines all core game elements (inherit from `GameObj.java`):
- `Dino.java`, `Birds.java`, `Cactuses.java`, `Clouds.java`, `Coin.java`, `Shield.java`
- `Land.java`, `Score.java`, `Block.java`

### 📁 `manager/`
Handles in-game logic and mechanics:
- `CoinManager.java`, `EnemyManager.java`, `ControlsManager.java`, `SoundManager.java`

### 📁 `misc/`
Game settings, states, and constants:
- `DinoState.java`, `GameState.java`, `EnemyType.java`, `Controls.java`, `Animation.java`

### 📁 `util/`
- `Resource.java`: Manages image and audio resources

### 📁 `user_interface/`
GUI components of the game:
- `GameScreen.java`, `GameWindow.java`

---

##  Core OOP Concepts Used

- **Encapsulation**: Keeps object properties and behaviors protected
- **Inheritance**: Common game behaviors shared via base classes
- **Abstraction**: High-level logic hidden behind clean interfaces
- **Polymorphism**: Reusable behaviors across multiple entities

---

## 🖼️ Screenshots

| Lightmode | Darkmode |
|----------|-------------|
| ![-](![image](https://github.com/user-attachments/assets/42341e4b-d20c-4154-a15c-20b086817b4e)
) | ![-](![image](https://github.com/user-attachments/assets/be49f7e6-0a62-4f4e-a58d-73870dbc0966)
) |

---

##  How to Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/dino-run.git
   cd dino-run-
