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

 Lightmode 
 
![image](https://github.com/user-attachments/assets/02b822e8-5b4c-462c-b4fc-cd94b397af19)

 Darkmode 
 
![image](https://github.com/user-attachments/assets/7a372d2d-434e-4e43-aa46-05661f32d458)

---

##  How to Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/dino-run.git
   cd dino-run
