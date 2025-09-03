# Vietnamese Air Defense — Educational Java Game

**Summary:**  
A Java 2D game simulating Vietnam's air defense battles (period 1946-1972). Players control different historical weapons/aircraft across various levels, destroy enemy aircraft, and receive short historical facts.

---

## Project Goals
- Transform from a "tank shooting blocks" game to a **historical game** (4 levels corresponding to 4 historical periods).
- Combine simple gameplay + educational content (historical facts and context).
- Easy maintenance: separated assets, modular code structure, documentation for devs and Copilot.

---

## Levels (Overview)
1. **Level 1 — Resistance War Against France (1946-1954)**  
   - Player: 37mm anti-aircraft gun (horizontal movement).  
   - Enemy: French aircraft (Morane, Spitfire, Bearcat, Dakota) .  
2. **Level 2 — Vietnam War (1965-1968)**  
   - Player: 100mm anti-aircraft gun (horizontal movement).  
   - Enemy: f105, f4 Phantom,b-26 (formation flying, dive bombing).  
3. **Level 3 — MiG-21 Air Combat (1969-1972)**  
   - Player: MiG-21 (4-directional movement).  
   - Enemy: f4 Phantom, f105 (dogfight behavior).  
4. **Level 4 — "Hanoi Air Defense" (1972)**  
   - Player: SAM-2 launcher (aim + shoot).  
   - Enemy: B-52 (high HP) + F-4 escort.

---

## Directory Structure (Proposed)
```
├── *.java                 # Source code files (Aircraft.java, GamePanel.java, etc.)
├── *.class               # Compiled Java files (Aircraft.class, GamePanel.class, etc.)
│
├── assets/               # Game resources
│   ├── image/           # Sprites and backgrounds
│   ├── bgm/            # Background music
│   └── sfx/            # Sound effects
│
├── lib/                 # External libraries
│   └── javafx-sdk-21.0.1/
│
├── docs/                # Documentation
│   ├── developer_guide.md    # Guide for developers
│   └── historical_facts.md   # Historical facts used in game
│
├── README.md           # Project overview and setup guide
├── run.bat            # Script to run the game
└── setup-javafx.bat   # Script to setup JavaFX
```

Note:
- `.java` files are the source code you edit
- `.class` files are automatically generated when you compile the Java code
- Keep both `.java` and `.class` files in the root directory for simplicity
- Documentation is organized in the `docs/` folder
- Use the `.bat` scripts to run and setup the game