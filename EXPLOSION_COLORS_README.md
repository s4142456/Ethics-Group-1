💥 Explosion Color System
Overview
The game has been updated to display explosions in different colors corresponding to each aircraft type, creating visually diverse and easily recognizable effects.
🔴 Explosion Color Chart by Aircraft Type
Level 1 – First Indochina War (1946–1954)
- Morane: Bright Red (255, 100, 100) – Light fighter aircraft
- Spitfire: Reddish Orange (255, 150, 50) – Famous fighter aircraft
- Bearcat: Yellow (255, 200, 0) – Powerful aircraft
- Dakota: Violet Blue (150, 150, 255) – Transport aircraft
Level 2 – Early Vietnam War (1965–1968)
- F-105: Deep Red (255, 80, 80) – Tactical bomber
- F-4 Phantom: Purple (200, 100, 255) – Multirole aircraft
- B-26: Orange (255, 165, 0) – Medium bomber
Level 3 – Air Combat (1966–1972)
- F-4 Phantom: Purple (200, 100, 255) – Multirole aircraft
- F-105: Deep Red (255, 80, 80) – Tactical bomber
- Skyhawk: Bright Green (0, 255, 150) – Light attack aircraft
Level 4 – Linebacker II Campaign (1972)
- B-52: Intense Red (255, 50, 50) – Massive strategic bomber
- F-4 Phantom: Purple (200, 100, 255) – Escort aircraft
Player Explosion
- Player hit: Blue (100, 150, 255) – Easily distinguishable from enemy explosions
⚙️ Technical Specifications
Explosion Effects
- Duration: 12 frames (~0.2 seconds at 60 FPS)
- Size: Expands from 20px to 92px
- Alpha: Gradually fades from 255 to 0
- Two-layer effect:
- Outer layer: Main color with fading alpha
- Inner layer: Color brightened by +50 RGB units, visible during the first 8 frames
How It Works
- When a player’s bullet hits an enemy aircraft, the system identifies the aircraft type via spriteKey
- The method getExplosionColorForAircraft() returns the corresponding color
- An Explosion object is created with the identified color
- The explosion is rendered with a two-layer effect and fading alpha
🎯 Benefits
- Visual recognition: Players can easily identify which aircraft was shot down
- Immersion: Enhances realism and engagement
- Feedback: Provides instant visual response to player actions
- Color diversity: Each level has a unique palette matching its historical context
🚀 Expandability
- Easily add new colors for additional aircraft types
- Adjust colors based on player feedback
- Add custom particle or sound effects for each explosion type

Let me know if you'd like this formatted for documentation, presentation, or in-game UI!
