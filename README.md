# Introduction
A PvP round-bases auto-combat game with Sword Universe theme.

**Current Version: 1.0**

# Game Mechanics
## :book: Story
You are diving into a World where swords is what you need to be Master of. To become a SwordMaster, you need to master your sword skils to create a Flashy combo or a One-shot slash and defeat a strong Swordman.
## :video_game: How to Play
### 1. Authenticate
Create an account and log in.

### 2. Host & Join Mode
Host a room and send your room code to your friend. Both of you ready for a Battle!

### 3. Select your Orders
Select a **Main Order** and a **Support Order** from a Order List that match with your play style! 

Each Order give you a unique characteristic:
- A **Status** includes:
  
| Status | Shorten | Description |
| ---| --- | --- |
| :crossed_swords: Strength | STR | Damage you can deal to your opponent. |
| :heart: Health Point| HP | Your life. If it reaches 0, you lose the Match. |
| :shield: Defense | DEF | Reduce the damage taken from your opponent. |
| :zap: Attack Speed | AS| Represents how fast you can attack your opponent. |
| :sparkles: Mana Point | MP| A resource that you can use to active an Action. |
| :crystal_ball: Qi Point | QP | A special resource that you active some special Actions. |

- A set of Techniques that you can use in the Match. Each Technique has a unique effect and cost (using MP or QP). 

### 4. Match Start
After both players select their Orders, they can see each other Orders they have followed and the Match begins !

A Match is running through many Rounds, and each Round consists of 3 Phases:
1. **Renewal Phase:** Players convert 

A Match ends immediately when one of these conditions is met:
- One of the players' HP reaches 0
- A Player surrenders
- A Player disconnects over 5 minutes


# Technology
For game-client:
- React.js for game components library
- Redux Toolkit for global state management + RTK Query for API calls
- React Router for routing
- STOMP for WebSocket communication

For game-server:
- Spring Boot for REST API
- Spring WebSocket for WebSocket communication

# How to run
1. Clone the repository
2. Install dependencies for both client and server:
3. Run command

