# Food Chain Through Time

Turn-based, grid-based **food chain simulation game** implemented in **Java (Swing GUI)** for **COMP132: Advanced Programming (Fall 2025)**.

The player controls the **Predator** and tries to score by eating the **Prey** while avoiding the **Apex Predator**, across three eras (**Past / Present / Future**). Each era changes special move geometry, cooldown behavior, and visuals.

## Gameplay Overview
- **Turn order:** Prey (AI) → Predator (Player) → Apex (AI)  
- **Start options:** Era selection (Past/Present/Future), grid size (n×n), total rounds  
- **Moves:**  
  - Normal moves: cells highlighted (walk)  
  - Special move: era-dependent ability targets highlighted when cooldown is available  
- **Respawn:** eaten entities reappear at random empty locations to keep the board active  

## Era System (Past / Present / Future)
The game uses `past.txt`, `present.txt`, `future.txt` to load food chain names.
At game start, a random line from the selected era file is chosen and used to create:
**Apex Predator, Predator (player), Prey, Food**.

Because names change per selected chain, visuals also vary (image key is derived from standardized entity names).

## Scoring Rules
- **Prey eats Food:** Prey +3
- **Predator eats Prey:** Predator +3, Prey −1
- **Apex eats Predator or Prey:** Apex +1, eaten animal −1

Winner is determined at the end of the configured round limit by highest score (draw possible).

## Save / Load
- Save anytime via menu: `Choices → Save`
- Saved state is written to `savegame.txt` including:
  - Mode/Era, grid size, current round, max rounds
  - Full entity list with coordinates
  - Animal score and ability cooldown values
- Resume is available from the start screen to restore the saved session.

## Logging
The game records a detailed session log to `game_log.txt`, including:
- GAME_START info (era, total rounds)
- spawn locations, round begin/end markers, moves (AI + player)
- ability usage + cooldown start
- score changes, respawn events
- game over summary (winner/final state)

## Project Structure
Packages (high-level):
- `main` — entry point (`Main`)
- `gui` — `GameFrame`, `StartPanel`, `GamePanel`, `InfoPanel`
- `logic` — `GameEngine`, `Grid`, `Cell`, `AIController`
- `model` — `Entity`, `Food`, `GameState`
- `model.animals` — `Animal`
- `io` — `FileManager`, `GameLogger`, `SoundManager`
- `exceptions` — `InvalidMoveException`


