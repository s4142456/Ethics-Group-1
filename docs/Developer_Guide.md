Every subsection contains recommendations, but the problems are real. Please be creative in implementing solutions and feel confident to use AI for your ideas.

### 1. Sprites & Backgrounds - Who?
- [ ] Remove backgrounds from sprites (transparent PNG).
- [ ] Add historical backgrounds for each level:
  - Level 1 → Dien Bien Phu
  - Level 2 → North Vietnam
  - Level 3 → Hanoi skies
  - Level 4 → Hanoi during Operation Linebacker II

### 2. Enemy AI & Behavior - Who? - most difficult
- [ ] Set up enemy aircraft patterns:
  - Level 1: low-flying, minimal bombing.
  - Level 2: formation flying, dive bombing attacks.
  - Level 3: dogfighting, target locking, rapid-fire shooting.
  - Level 4: slow-moving B-52s with high HP, F-4 escorts.
- [ ] Improve bullet speed and enemy firing frequency.

### 3. Historical Elements - Who?
- [ ] Fill historical_facts.md with facts (aircraft shot down, battles, years...).
- [ ] Display brief historical info at level start.
- [ ] Optional: Show fact popup when shooting down enemy.

### 4. Sound Effects - Who?
- [ ] Replace with lightweight, loopable .wav background music.
- [ ] Use compact explosion .wav files (≤ 300 KB).
- [ ] Add sound effects for:
  - Shooting
  - Explosions
  - Aircraft engines
  - Air defense alarm (level 4)

### 5. Documentation - A person with least effort
- [ ] Add Javadoc for each class:
  - `Game.java` → main loop
  - `LevelManager.java` → level & difficulty management
  - `Enemy.java` → AI, behavior
  - `Weapon.java` → weapon logic
  - `SoundManager.java` → sound processing
- [ ] Add inline `// TODO` for Copilot suggestions.