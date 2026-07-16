# ⚔️ SwordVerse

> A 1v1 online web game featuring round-based progression, auto-combat mechanics, and a sword-themed fantasy world.

## Overview

| Information         | Details                 |
| ------------------- | ----------------------- |
| **Name**            | SwordVerse              |
| **Current Version** | 1.0                     |
| **Game Mode**       | Online 1v1              |
| **Genre**           | Round-based auto-combat |

## 📖 Handbook

### :book: Story

Enter a world where mastering the sword is the path to ultimate power.

To become a **SwordMaster**, you must master your sword techniques, create flashy combos, unleash devastating one-shot slashes, and defeat powerful swordsmen.

### :video_game: How to Play

#### 1. Authenticate

Create an account and log in to the game.

#### 2. Host or Join a Room

Host a room and send the room code to your friend. Once both players are ready, the matchup begins.

The host can remove a player from the room before the match starts, and any player can leave on their own. A match cannot begin while either player is disconnected — both must be present, ready, and connected.

#### 3. Pre-Matchup: Select Orders

Both players secretly choose a **Main Order**. Their selections are revealed simultaneously.

Next, each player chooses a **Support Order** and one support **Technique** from that Order. These selections are then revealed.

Your Support Order can be the same as your Main Order, but your Support Technique can't be one you already have from your Main Order.

Each Order provides unique characteristics, including:

* A set of **Stats**:

| Stat                      | Abbreviation | Description                                                |
| ------------------------- | :----------: | ---------------------------------------------------------- |
| :crossed_swords: Strength |      STR     | Determines how much damage you can deal to your opponent.  |
| :heart: Health Points     |      HP      | Represents your life. If it reaches 0, you lose the match. |
| :shield: Defense          |      DEF     | Reduces the damage received from your opponent.            |
| :zap: Attack Speed        |      AS      | Represents how quickly you can attack your opponent.       |
| :sparkles: Mana Points    |      MP      | A resource used to activate Actions.                       |
| :crystal_ball: Qi Points  |      QP      | A special resource used to activate certain Actions.       |

* A set of **Techniques** that can be used during the match. Each Technique has a unique effect and consumes either MP or QP.

Your final **Action List** consists of:

> **Basic Actions** + **3 Main Techniques** + **1 Support Technique**

#### 4. Match Start

After both players have selected their Orders, all selected Orders are revealed and the match begins.

A match consists of multiple **Rounds**. Each Round proceeds through the following **four Phases**.

##### 4.1. :hourglass_flowing_sand: RENEWAL Phase

* Resolve all active **Effects**.
* Convert all remaining **MP** into **QP** using a conversion ratio determined by the player's Order.
* Fully restore **MP**.

##### 4.2. :star: ASCENSION Phase

* At the beginning of each Round, both players receive **2 Learning Points (LP)**.
* Players secretly allocate their LP to upgrade **Stats**, learn a new **Technique**, or level up an existing Technique.
* After both players confirm their LP allocation, the updated Stats and Techniques are revealed simultaneously.
* The match then proceeds to the next Phase.

If you don't confirm before time runs out, whatever allocation you had set (or nothing, if you hadn't set anything) locks in as-is — unused Learning Points for that Round are lost, they don't carry over.

##### 4.3. :scroll: ACTION STRATEGY Phase

Each player has an **ACTION QUEUE**. Actions placed in this queue are automatically executed during the Battle Phase.

During each Round, players must fill their ACTION QUEUE with the required number of Actions. Queue configuration is performed in the following order:

1. Remove up to **1 existing Action** from the ACTION QUEUE.
2. Add new Actions from the player's **Techniques** or **Basic Actions**.
3. Place each new Action at the beginning, end, or between existing Actions in the queue.

| Round | ACTION QUEUE Size |
| :---: | :---------------: |
|   1   |         2         |
|   2   |         3         |
|   3   |         4         |
|   4   |         5         |
|   5   |         6         |
|   6+  |         7         |

The same Action can be placed in the queue more than once, as long as you'll be able to afford it each time it comes up.

Once both players have confirmed a valid ACTION QUEUE, the match proceeds to the Battle Phase.

If you don't confirm a full queue before time runs out, whatever you had queued locks in as-is — any empty slots are treated as automatic misses once the Battle reaches them.

##### 4.4. :dart: BATTLE Phase

Both players' Actions resolve together, step by step through their queues: the Action at position 1 of your queue and position 1 of your opponent's queue happen at the same time, then position 2, and so on, until both queues are empty. There's no more "whose turn is it" — every step involves both players at once. The result of each step is displayed in the **Battle Log**.

The outcome of each step depends on **both** Actions together, not either one alone:

* If both players attack, both take damage.
* If one player attacks and the other counters, only the attacker takes damage — the counterer takes none.
* If one player attacks and the other guards, neither takes damage.

These are just a few examples — Techniques can do far more than attack, counter, or guard (healing, resource restoration, ignoring Defense, and other special effects all exist), and how any two Actions interact is decided case by case, not by a fixed set of categories.

If a player can't afford their queued Action at a step (or is disabled by an active Effect), only their own Action fails for that step — it's treated as if they did nothing, while their opponent's Action still resolves normally.

The Battle Phase continues until:

* Every Action in the ACTION QUEUE has been executed; or
* One of the players meets a **Match End Condition**.

If all Actions have been executed and neither player meets a Match End Condition, a new Round begins.

#### 5. :trophy: Match End Conditions

A player wins the match when one of the following conditions is met:

* The opponent's HP reaches 0.
* The opponent surrenders.
* The opponent remains disconnected for more than 5 minutes.

You can surrender at any point once the match has begun. The 5-minute disconnect timer starts the instant a player disconnects, no matter which Phase the match is currently in.

If both players' HP reach 0 at the same time, the match ends in a draw — neither player wins.

When one of these conditions is met, the match ends immediately and the result is displayed on the **Match Result Board**.

## 🛠️ Technology

### Game Client

* **React.js** + **Typescript** — Game component library and user interface + type safety.
* **Redux Toolkit** — Global state management.
* **RTK Query** — API requests and server-state management.
* **React Router** — Client-side routing.
* **STOMP** + **SockJS** — WebSocket communication.

### Game Server

* **Spring Boot** — REST API development.
* **Spring WebSocket** — Real-time WebSocket communication.

## 🚀 How to Run

1. Clone the repository.
2. Install the required dependencies for both the client and server.
3. Run the startup commands for the client and server.