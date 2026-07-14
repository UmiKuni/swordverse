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

#### 3. Pre-Matchup: Select Orders

Both players secretly choose a **Main Order**. Their selections are revealed simultaneously.

Next, each player chooses a **Support Order** and one support **Technique** from that Order. These selections are then revealed.

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

Once both players have confirmed a valid ACTION QUEUE, the match proceeds to the Battle Phase.

##### 4.4. :dart: BATTLE Phase

Each **Action** in the **ACTION QUEUE** is revealed and executed in order. The result of each Action is displayed in the **Battle Log**.

The Battle Phase continues until:

* Every Action in the ACTION QUEUE has been executed; or
* One of the players meets a **Match End Condition**.

If all Actions have been executed and neither player meets a Match End Condition, a new Round begins.

#### 5. :trophy: Match End Conditions

A player wins the match when one of the following conditions is met:

* The opponent's HP reaches 0.
* The opponent surrenders.
* The opponent remains disconnected for more than 5 minutes.

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
