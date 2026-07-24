# Ken vs An Match Simulation

## Starting State

The match has started. Main Sects are already locked.

| Player | Main Sect | Support Sect | HP | DEF | STR | AS |
|---|---|---|---:|---:|---:|---:|
| Ken | Shadow Sword | Shadow Sword | 3500.00 | 20 | 190 | 1.10 |
| An | Flying Sword | Flying Sword | 3200.00 | 25 | 170 | 1.50 |

```text
damageTaken = damageDeal x 100 / (100 + targetDEF)
```

The ledger retains full decimal values and displays them to two decimal places. Action Qi costs in this simulation use `ON_EXECUTION_START` payment timing.

## Pre-Match Action Selection

| Phase | Ken | An | Resolution |
|---|---|---|---|
| Basic selection | Slash, Defend | Slash, Shield | Both players confirm; both Basic loadouts are revealed. |
| Main Technique selection | Ambush, Phantasm, Eclipse | White Rainbow, River, Ascendant | Both players confirm; both Main loadouts are revealed. |
| Support selection | Shadow Sword; Predation | Flying Sword; Heavenly Sword Circuit | Both players confirm; both Support loadouts are revealed. |

| Player | BASIC_1 | BASIC_2 | MAIN_1 | MAIN_2 | MAIN_3 | SUPPORT |
|---|---|---|---|---|---|---|
| Ken | Slash | Defend | Ambush | Phantasm | Eclipse | Predation |
| An | Slash | Shield | White Rainbow | River | Ascendant | Heavenly Sword Circuit |

## Round 1

### Renewal

Round 1 has no Ascension Phase. Renewal grants 150 Round Qi to each player.

| Player | roundQi | reserveQi | Basic Actions | Sect Techniques | Stat levels |
|---|---:|---:|---|---|---|
| Ken | 150 | 0 | Slash L1, Defend L1 | Ambush L0, Phantasm L0, Eclipse L0, Predation L0 | HP L1, STR L1, DEF L1, AS L1 |
| An | 150 | 0 | Slash L1, Shield L1 | White Rainbow L0, River L0, Ascendant L0, Heavenly Sword Circuit L0 | HP L1, STR L1, DEF L1, AS L1 |

### Action Strategy

| Player | Confirmed queue | Qi cost | Effective duration |
|---|---|---:|---:|
| Ken | Defend, Slash, Slash | 100 | 30 ticks |
| An | Slash, Shield, Slash | 140 | 24 ticks |

### Battle

| Tick | Ken | An | Resolution | HP ledger |
|---:|---|---|---|---|
| 0 | Defend starts, `(0, 10]` | Slash starts, `(0, 7]` | Ken pays 20 Qi. An pays 40 Qi. | Ken 3500.00; An 3200.00. |
| 1 | Defend charge active | Slash continues | Defend gains one Slash-blocking charge. | No HP change. |
| 7 | Defend remains active | Slash ends; Shield starts, `(7, 17]` | An Slash deals `170`; Defend ignores it and consumes its charge. An pays 60 Qi. Shield sets An DEF to 50. | Ken 3500.00; An 3200.00. |
| 10 | Defend ends; Slash starts, `(10, 20]` | Shield remains active | Ken pays 40 Qi. | No HP change. |
| 17 | Slash continues | Shield ends; Slash starts, `(17, 24]` | An pays 40 Qi. An DEF returns to 25. | No HP change. |
| 20 | First Slash ends; second Slash starts, `(20, 30]` | Second Slash continues | Ken Slash deals `190 x 100 / 125 = 152`. Ken pays 40 Qi. | Ken 3500.00; An 3048.00. |
| 24 | Second Slash continues | Second Slash ends | An Slash deals `170 x 100 / 120 = 141.666666...`. | Ken 3358.33; An 3048.00. |
| 30 | Second Slash ends | Queue complete | Ken Slash deals `152`. | Ken 3358.33; An 2896.00. |

### Battle End

No Battle-end Effects are present.

| Player | roundQi | reserveQi | HP |
|---|---:|---:|---:|
| Ken | 50 | 0 | 3358.33 |
| An | 10 | 0 | 2896.00 |

## Round 2

### Renewal

| Player | Transferred to reserveQi | Discarded Qi | Round Qi grant | roundQi | reserveQi |
|---|---:|---:|---:|---:|---:|
| Ken | 50 | 0 | 200 | 200 | 50 |
| An | 10 | 0 | 200 | 200 | 10 |

### Ascension

| Player | Learning Point 1 | Learning Point 2 | Newly learned Actions |
|---|---|---|---|
| Ken | Ambush L0 -> L1 | Phantasm L0 -> L1 | Ambush, Phantasm |
| An | White Rainbow L0 -> L1 | River L0 -> L1 | White Rainbow, River |

### Action Strategy

Ken removes Defend for 10 ticks from a 30-tick queue. An removes the first Slash for 7 ticks from a 24-tick queue.

| Player | Retained occurrences | Inserted occurrences | Confirmed queue | Qi cost |
|---|---|---|---|---:|
| Ken | Slash, Slash | Ambush | Ambush, Slash, Slash | 180 |
| An | Shield, Slash | White Rainbow | White Rainbow, Shield, Slash | 140 |

### Battle

| Tick | Ken | An | Resolution | HP ledger |
|---:|---|---|---|---|
| 0 | Ambush starts, `(0, 15]` | White Rainbow starts, `(0, 10]` | Ken pays 100 Qi: `roundQi 200 -> 100`. An pays 40 Qi: `roundQi 200 -> 160`. | Ken 3358.33; An 2896.00. |
| 1 | Ambush start Effects | White Rainbow continues | Ambush gains 1 Shade, raises Ken STR to `190 x 1.10 = 209`, and applies Shadow through tick 15. | No HP change. |
| 10 | Ambush remains active | White Rainbow ends; Shield starts, `(10, 20]` | White Rainbow deals `120% x 170 = 204` Slash damage; Shadow ignores it. An pays 60 Qi: `roundQi 160 -> 100`; Shield sets An DEF to 50. | Ken 3358.33; An 2896.00. |
| 15 | Ambush ends; first Slash starts, `(15, 25]` | Shield remains active | Ambush deals `210% x 209 = 438.9`. An takes `438.9 x 100 / 150 = 292.6`. Ken pays 40 Qi: `roundQi 100 -> 60`. | Ken 3358.33; An 2603.40. |
| 20 | First Slash continues | Shield ends; Slash starts, `(20, 27]` | An pays 40 Qi: `roundQi 100 -> 60`. An DEF returns to 25. | No HP change. |
| 25 | First Slash ends; second Slash starts, `(25, 35]` | Slash continues | Ken Slash deals `152`. Ken pays 40 Qi: `roundQi 60 -> 20`. | Ken 3358.33; An 2451.40. |
| 27 | Second Slash continues | Slash ends | An Slash deals `141.666666...`. | Ken 3216.67; An 2451.40. |
| 35 | Second Slash ends | Queue complete | Ken Slash deals `152`. | Ken 3216.67; An 2299.40. |

### Battle End

| Player | roundQi | reserveQi | HP |
|---|---:|---:|---:|
| Ken | 20 | 50 | 3216.67 |
| An | 60 | 10 | 2299.40 |

## Round 3

### Renewal

| Player | Transferred to reserveQi | Discarded Qi | Round Qi grant | roundQi | reserveQi |
|---|---:|---:|---:|---:|---:|
| Ken | 20 | 0 | 250 | 250 | 70 |
| An | 60 | 0 | 250 | 250 | 70 |

### Ascension

| Player | Learning Point 1 | Learning Point 2 | Newly learned Actions |
|---|---|---|---|
| Ken | Eclipse L0 -> L1 | Predation L0 -> L1 | Eclipse, Predation |
| An | Ascendant L0 -> L1 | Heavenly Sword Circuit L0 -> L1 | Ascendant, Heavenly Sword Circuit |

### Action Strategy

Ken removes one 10-tick Slash from a 35-tick queue. An removes the final 7-tick Slash from a 27-tick queue.

| Player | Retained occurrences | Inserted occurrences | Confirmed queue | Qi cost |
|---|---|---|---|---:|
| Ken | Ambush, Slash | Defend | Defend, Ambush, Slash | 160 |
| An | White Rainbow, Shield | Heavenly Sword Circuit | Heavenly Sword Circuit, White Rainbow, Shield | 160 |

### Battle

| Tick | Ken | An | Resolution | HP ledger |
|---:|---|---|---|---|
| 0 | Defend starts, `(0, 10]` | Heavenly Sword Circuit starts, `(0, 5]` | Ken pays 20 Qi: `roundQi 250 -> 230`. An pays 60 Qi: `roundQi 250 -> 190`. | Ken 3216.67; An 2299.40. |
| 1 | Defend charge active | Heavenly Sword Circuit continues | Defend gains one Slash-blocking charge. | No HP change. |
| 5 | Defend remains active | Heavenly Sword Circuit ends; White Rainbow starts, `(5, 15]` | An gains `+10% STR`, `+20% AS`, and recovers 30 Round Qi: `roundQi 190 -> 220`. White Rainbow pays 40 Qi: `roundQi 220 -> 180`. An STR is 187 and AS is 1.80 for the round. | No HP change. |
| 10 | Defend ends; Ambush starts, `(10, 25]` | White Rainbow continues | Ken pays 100 Qi: `roundQi 230 -> 130`. | No HP change. |
| 11 | Ambush start Effects | White Rainbow continues | An HP is above 50%, so Predation grants no Shade. Ambush gains 1 Shade, sets Ken STR to 209, and applies Shadow. | No HP change. |
| 15 | Ambush remains active | White Rainbow ends; Shield starts, `(15, 25]` | White Rainbow deals `120% x 187 = 224.4` Slash damage; Shadow ignores it. An pays 60 Qi: `roundQi 180 -> 120`; Shield sets An DEF to 50. | Ken 3216.67; An 2299.40. |
| 25 | Ambush ends; Slash starts, `(25, 35]` | Shield ends | Ambush deals `438.9`; An takes `292.6`. Ken pays 40 Qi: `roundQi 130 -> 90`. | Ken 3216.67; An 2006.80. |
| 35 | Slash ends | Queue complete | Ken Slash deals `152`. | Ken 3216.67; An 1854.80. |

### Battle End

| Player | roundQi | reserveQi | HP |
|---|---:|---:|---:|
| Ken | 90 | 70 | 3216.67 |
| An | 120 | 70 | 1854.80 |

## Round 4

### Renewal

| Player | Transferred to reserveQi | Discarded Qi | Round Qi grant | roundQi | reserveQi |
|---|---:|---:|---:|---:|---:|
| Ken | 80 | 10 | 300 | 300 | 150 |
| An | 80 | 40 | 300 | 300 | 150 |

### Ascension

| Player | Learning Point 1 | Learning Point 2 |
|---|---|---|
| Ken | No allocation | No allocation |
| An | No allocation | No allocation |

### Action Strategy

Ken removes Defend for 10 ticks from a 35-tick queue. An removes Heavenly Sword Circuit for 5 ticks from a 25-tick queue.

| Player | Retained occurrences | Inserted occurrences | Confirmed queue | Qi cost |
|---|---|---|---|---:|
| Ken | Ambush, Slash | Phantasm | Ambush, Phantasm, Slash | 190 |
| An | White Rainbow, Shield | Slash, River | White Rainbow, Shield, Slash, River | 240 |

### Battle

| Tick | Ken | An | Resolution | HP ledger |
|---:|---|---|---|---|
| 0 | Ambush starts, `(0, 15]` | White Rainbow starts, `(0, 10]` | Ken pays 100 Qi: `roundQi 300 -> 200`. An pays 40 Qi: `roundQi 300 -> 260`. | Ken 3216.67; An 1854.80. |
| 1 | Ambush start Effects | White Rainbow continues | An HP is above 50%, so Predation grants no Shade. Ambush gains 1 Shade, sets Ken STR to 209, and applies Shadow. | No HP change. |
| 10 | Ambush remains active | White Rainbow ends; Shield starts, `(10, 20]` | White Rainbow deals 204 Slash damage; Shadow ignores it. An pays 60 Qi: `roundQi 260 -> 200`; Shield sets An DEF to 50. | Ken 3216.67; An 1854.80. |
| 15 | Ambush ends; Phantasm starts, `(15, 22]` | Shield remains active | Ambush deals `438.9`; An takes `292.6`, falling below 50% HP. Ken pays 50 Qi: `roundQi 200 -> 150`. | Ken 3216.67; An 1562.20. |
| 16 | Phantasm start Effects | Shield remains active | Predation grants 1 Shade before Phantasm resolves. Phantasm has 2 Shade, consumes 1, and deals `(285 x 1.06) x 100 / 150 = 201.4`. | Ken Shade 1; An 1360.80. |
| 20 | Phantasm remains active | Shield ends; Slash starts, `(20, 27]` | An pays 40 Qi: `roundQi 200 -> 160`. An DEF returns to 25. | No HP change. |
| 22 | Phantasm ends; Slash starts, `(22, 32]` | Slash continues | Ken pays 40 Qi: `roundQi 150 -> 110`. | No HP change. |
| 27 | Slash continues | Slash ends; River starts, `(27, 45]` | An Slash deals `141.666666...`. River pays 100 Qi: `roundQi 160 -> 60`; `X = ceil(4 x 1.50) = 6`. | Ken 3075.00; An 1360.80. |
| 30 | Slash continues | River strike 1 | River deals `76.5`; Ken takes `63.75` and receives one Bleed stack. | Ken 3011.25; An 1360.80. |
| 32 | Slash ends | River continues | Ken Slash deals `152`. | Ken 3011.25; An 1208.80. |
| 33, 36, 39, 42, 45 | Queue complete | River strikes 2 through 6 | Each River strike deals `76.5`; Ken takes `63.75` and receives one Bleed stack. | Ken 2692.50 and has 6 Bleed stacks. |
| Battle end | Queue complete | River completes | Six Bleed stacks deal `6 x (5% x 170) = 51` Direct Damage. | Ken 2641.50; An 1208.80. |

## Round 5

### Renewal

| Player | Transferred to reserveQi | Discarded Qi | Round Qi grant | roundQi | reserveQi |
|---|---:|---:|---:|---:|---:|
| Ken | 0 | 110 | 350 | 350 | 150 |
| An | 0 | 60 | 350 | 350 | 150 |

### Ascension

| Player | Learning Point 1 | Learning Point 2 |
|---|---|---|
| Ken | No allocation | No allocation |
| An | No allocation | No allocation |

### Action Strategy

Ken removes the final 10-tick Slash from a 32-tick queue. An removes White Rainbow for 10 ticks from a 45-tick queue.

| Player | Retained occurrences | Inserted occurrences | Confirmed queue | Qi cost |
|---|---|---|---|---:|
| Ken | Ambush, Phantasm | Defend | Defend, Ambush, Phantasm | 170 |
| An | Shield, Slash, River | Ascendant | Ascendant, Shield, Slash, River | 350 |

### Battle

| Tick | Ken | An | Resolution | HP and Barrier ledger |
|---:|---|---|---|---|
| 0 | Defend starts, `(0, 10]` | Ascendant starts, `(0, 10]` | Ken pays 20 Qi: `roundQi 350 -> 330`. An pays 150 Qi: `roundQi 350 -> 200`. | Ken 2641.50; An 1208.80; An Barrier 0.00. |
| 1 | Defend charge active | Ascendant start Effects | Ascendant gains `5% x 3200 = 160` Barrier. | An Barrier 160.00. |
| 10 | Defend ends; Ambush starts, `(10, 25]` | Ascendant ends; Shield starts, `(10, 20]` | An gains `+30% AS` and Ascendance; AS is 1.95. An pays 60 Qi: `roundQi 200 -> 140`. Ken pays 100 Qi: `roundQi 330 -> 230`. | No HP change. |
| 11 | Ambush start Effects | Shield remains active | An is below 50% HP. Predation grants 1 Shade, then Ambush gains 1 Shade and raises Ken STR to 209. | Ken Shade 2. |
| 20 | Ambush remains active | Shield ends; Slash starts, `(20, 26]` | An pays 40 Qi: `roundQi 140 -> 100`. | No HP change. |
| 25 | Ambush ends; Phantasm starts, `(25, 32]` | Slash continues | Ambush base damage is `313.5`; An takes `313.5 x 100 / 125 = 250.8`. The 160 Barrier absorbs 160, so An loses 90.8 HP. Ken pays 50 Qi: `roundQi 230 -> 180`. | An 1118.00; Barrier 0.00. |
| 26 | Phantasm start Effects | Slash ends; River starts, `(26, 50]` | Predation grants 1 Shade. Phantasm consumes 1 Shade and deals `(285 x 1.06) x 100 / 125 = 241.68`. An Slash deals `141.666666...`. River pays 100 Qi: `roundQi 100 -> 0`; `X = ceil(4 x 1.95) = 8`. Ascendance applies one Bleed to Ken for the successful Slash. | Ken 2499.83; An 876.32; Ken Shade 2. |
| 29, 32, 35, 38, 41, 44, 47, 50 | Queue complete after tick 32 | River strikes | Each River strike deals `76.5 x 100 / 120 = 63.75`. Each successful strike applies one River Bleed and one Ascendance Bleed. | Ken loses 510.00 from River strikes and has 17 Bleed stacks. |
| Battle end | Queue complete | River completes | Seventeen Bleed stacks deal `17 x 8.5 = 144.5` Direct Damage. Ascendance follow-up deals `7% x 17 x 170 = 202.3` Direct Damage. | Ken 1643.03; An 876.32. |

## Round 6

### Renewal

| Player | Transferred to reserveQi | Discarded Qi | Round Qi grant | roundQi | reserveQi |
|---|---:|---:|---:|---:|---:|
| Ken | 0 | 180 | 400 | 400 | 150 |
| An | 0 | 0 | 400 | 400 | 150 |

### Ascension

| Player | Learning Point 1 | Learning Point 2 |
|---|---|---|
| Ken | No allocation | No allocation |
| An | No allocation | No allocation |

### Action Strategy

Ken removes Defend for 10 ticks from a 32-tick queue. An removes Ascendant for 10 ticks from a 50-tick queue.

| Player | Retained occurrences | Inserted occurrences | Confirmed queue | Qi cost |
|---|---|---|---|---:|
| Ken | Ambush, Phantasm | Eclipse | Ambush, Phantasm, Eclipse | 300 |
| An | Shield, Slash, River | White Rainbow | Shield, Slash, River, White Rainbow | 240 |

### Battle

| Tick | Ken | An | Resolution | HP and Barrier ledger |
|---:|---|---|---|---|
| 0 | Ambush starts, `(0, 15]` | Shield starts, `(0, 10]` | Ken pays 100 Qi: `roundQi 400 -> 300`. An pays 60 Qi: `roundQi 400 -> 340`. Shield sets An DEF to 50. | Ken 1643.03; An 876.32. |
| 1 | Ambush start Effects | Shield remains active | Predation grants 1 Shade because An is below 50% HP. Ambush gains 1 Shade and raises Ken STR to 209. | Ken Shade 2. |
| 10 | Ambush remains active | Shield ends; Slash starts, `(10, 17]` | An pays 40 Qi: `roundQi 340 -> 300`. An DEF returns to 25. | No HP change. |
| 15 | Ambush ends; Phantasm starts, `(15, 22]` | Slash continues | Ambush base damage is `313.5`; An takes `313.5 x 100 / 125 = 250.8`. Ken pays 50 Qi: `roundQi 300 -> 250`. | An 625.52. |
| 16 | Phantasm start Effects | Slash continues | Predation grants 1 Shade. Phantasm consumes 1 Shade and deals `(285 x 1.06) x 100 / 125 = 241.68`. | Ken Shade 2; An 383.84. |
| 17 | Phantasm remains active | Slash ends; River starts, `(17, 35]` | An Slash deals `141.666666...`. River pays 100 Qi: `roundQi 300 -> 200`; `X = ceil(4 x 1.50) = 6`. | Ken 1501.37; An 383.84. |
| 20 | Phantasm remains active | River strike 1 | River deals `63.75` and applies one Bleed. | Ken 1437.62. |
| 22 | Phantasm ends; Eclipse starts, `(22, 37]` | River continues | Ken pays 150 Qi: `roundQi 250 -> 100`. Eclipse applies Shadow throughout `(22, 37]`. | No HP change. |
| 23 | Eclipse start Effects | River strike 2 | Predation grants 1 Shade. Eclipse consumes all 3 Shade stacks and stores `CONSUMED_SHADE = 3`. River deals `63.75` and applies one Bleed. | Ken 1373.87; Ken Shade 0. |
| 26, 29, 32, 35 | Eclipse remains active | River strikes 3 through 6; White Rainbow starts at tick 35 | Each River strike deals `63.75` and applies one Bleed. An pays 40 Qi for White Rainbow at tick 35: `roundQi 200 -> 160`; its endpoint is tick 45. | Ken 1118.87 and has 6 Bleed stacks. |
| 37 | Eclipse ends | White Rainbow remains active | Missing HP is `3200 - 383.84 = 2816.16`. Eclipse damage is `(190 + 281.616 + (3 x 95)) x 1.18 = 892.80688`. An takes `892.80688 x 100 / 125 = 714.245504`. | Ken 1118.87; An 0.00. |

### Game Over

At tick 37, An reaches 0 HP. The match ends immediately.

| Field | Value |
|---|---|
| Winner | Ken |
| Loser | An |
| End round | 6 |
| End tick | 37 |
| Match-end reason | HP reached 0 |
| An incomplete occurrence | White Rainbow has started but does not resolve at tick 45. |
| Cancelled processing | Remaining timeline effects and all Battle-end Effects, including the six Bleed stacks. |
