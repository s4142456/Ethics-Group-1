Every subsection contains recommendations, but the problems are real. Please be creative in implementing solutions and feel confident to use AI for your ideas.

### 0. Kai transform Space Invaders to Vietnam Air Denfense. With the support of bro Long, Kai create this framework.

### 1. Sprites & Backgrounds - Tri
- [x] Remove backgrounds from sprites (transparent PNG).
- [x] Add historical backgrounds for each level:
  - Level 1 → Dien Bien Phu
  - Level 2 → North Vietnam
  - Level 3 → Hanoi skies
  - Level 4 → Hanoi during Operation Linebacker II

### 2. Enemy AI & Behavior - bro Long - most difficult
- [x] Set up enemy aircraft patterns:
  - Level 1: low-flying, minimal bombing.
  - Level 2: formation flying, dive bombing attacks.
  - Level 3: dogfighting, target locking, rapid-fire shooting.
  - Level 4: slow-moving B-52s with high HP, F-4 escorts.
- [x] Improve bullet speed and enemy firing frequency.

### 3. Historical Elements - Nam cannot commit and don't give the way to solve problems. So, Khoa take responsibility for this task.
- [x] Fill historical_facts.md with facts (aircraft shot down, battles, years...).
- [x] Display brief historical info at level start.
- [x] Optional: Show fact popup when shooting down enemy.

### 4. Sound Effects - Khoa
- [x] Replace with lightweight, loopable .wav background music.
- [x] Use compact explosion .wav files (≤ 300 KB).
- [x] Add sound effects for:
  - Shooting
  - Explosions
  - Aircraft engines
  - Air defense alarm (level 4)

### 5. Documentation and other tasks : Kai, Khoa, bro Long
- [x] Add Javadoc for each class:
  - `Game.java` → main loop
  - `LevelManager.java` → level & difficulty management
  - `Enemy.java` → AI, behavior
  - `Weapon.java` → weapon logic
  - `SoundManager.java` → sound processing
- [x] Add inline `// TODO` for Copilot suggestions.
- Fix bugs....