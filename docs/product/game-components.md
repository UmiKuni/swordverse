# SwordVerse Components
## 3 BASIC ACTIONS
Basic Actions have infinite Stack and no Cooldown.

### Action Duration Types

Every Active Action declares exactly one Duration Type. Passive Actions have no Duration Type.

- `AS_SCALED` uses the implicit one-second standard duration: `effectiveDurationTicks = max(1, ceil(10 / asSnapshot))`.
- `FIXED` uses its configured duration unchanged by AS.
- `CONTROLLED` uses its documented Action-specific duration rule.

The server captures a strictly positive `asSnapshot` when every Active Action begins. It affects `AS_SCALED` Actions and any `CONTROLLED` Action whose documented rule uses AS. AS never modifies cooldown directly.

### Slash
**- Cost:** 40 | **Activation type:** Active

**- Duration Type:** `AS_SCALED`

**- Description:** Deal (100% x STR) damage to the target.

**- Effect Logic:** 
`RESOLVE_ON_END`: **Deal** (100% x STR) damage.

### Defend
**- Cost:** 20 | **Activation type:** Active

**- Duration Type:** `AS_SCALED`

**- Description:** Assume a defensive stance and **Boost** the DEF stat by 100% while executing the Action.

**- Effect Logic:** 
`RESOLVE_DURING_EXECUTION`: **Boost** 100% DEF.

### Shield
**- Cost:** 60 | **Activation type:** Active

**- Duration Type:** `FIXED` | **Duration:** 1 second

**- Description:** Create a shield that ignores the next incoming **Slash** damage instance while this Action executes.

**- Effect Logic:** 
`RESOLVE_DURING_EXECUTION`: Gain 1 execution-bound blocking charge. Ignore the next incoming **Slash** damage instance and consume the charge. Any unused charge expires when this Action ends.

## 3 SECTS
### True Sword Sect - Chân Kiếm Phái

**Description:** The True Sword Sect represents disciplined mastery of conventional swordsmanship. Its intended style is balanced, sustainable, resilient, and adaptable.

**Starting Stats:** 
>| 4000:heart: | 30:shield: | 150:crossed_swords: | 1:zap: |

**Support Stats:** +300:heart: +5:crossed_swords: +5:shield:

**Techniques:**

### Flying Sword Sect - Phi Kiếm Phái

**Description:** The Flying Sword Sect emphasizes speed, deception, mobility, and extended combinations. Its intended style is swift, adaptive, and difficult to predict.

**Starting Stats:** 
>| 3200:heart: | 25:shield: | 170:crossed_swords: | 1.25:zap: |

**Support Stats:** +0.25:zap:

**Techniques:**
- Active: 
- Active:
- Active:
- Passive:
- Ultimate:

### Shadow Sword Sect - Ảnh Kiếm Phái

**Description:** The Shadow Sword Sect emphasizes precision, misdirection, and calculated evasion. Its intended style is technical and rewards deliberate sequencing.

**Starting Stats:** 
>| 3500:heart: | 20:shield: | 180:crossed_swords: | 1:zap: |

**Support Stats:** +10:crossed_swords: +0.1:zap:

**Techniques:**
- Active: 
- Active:
- Active:
- Passive:
- Ultimate:

### Upcoming Sects: 
**Divine Sword Sect** (Thánh Kiếm Phái) & **Demonic Sword Sect** (Ác Kiếm Phái)

## 15 TECHNIQUES

### 1. Mindbound Edge - Liễm Thần Định Phong (True Sword Sect)
**- Cost:** 50/80/100 | **Activation type:** Active

**- Duration Type:** `FIXED` | **Duration:** 1s | **Cooldown:** 1s | **Stack:** 1

**- Description:** Grip the sword with both hands, then release a precise strike, **deal** (150/200/250% x STR) damage.

**- Effect Logic:** 

`RESOLVE_ON_END`: **Deal** (150/200/250% x STR) damage.

### 2. Rising Reprisal - Thừa Kình Liêu Trảm (True Sword Sect)
**- Cost:** 55/85/110 | **Activation type:** Active

**- Duration Type:** `FIXED` | **Duration:** 1s | **Cooldown:** 1s | **Stack:** 1

**- Description:** Assume a guarded sword stance, **boost** 20/30/50 DEF while executing. On Completion, unleash an upward slash that **deal** (120/160/210% x STR) damage. If taken damage while in the stance, **deal** additional (50% x STR) damage.

**- Effect Logic:**

`RESOLVE_DURING_EXECUTION`: **Boost** 20/30/50 DEF.

`RESOLVE_ON_END`: **Deal** (120/160/210% x STR) damage. If damage was taken during this Action's effective execution interval, **deal** additional (50% x STR) damage.

### 3. One Thought, Myriad Edges - Nhất Niệm Vạn Kiếm (True Sword Sect)
**- Cost:** 70/90/120 | **Activation type:** Active

**- Duration Type:** `CONTROLLED` | **Cooldown:** 1s | **Stack:** 1

**- Duration Rule:** `effectiveDurationTicks = configuredStrikeCount * 5`, where configured strike count is 3/4/5 at Action levels 1/2/3.

**- Description:** Focus the mind upon a single thought, then release 3/4/5 precise consecutive strikes that each **deal** (90/120/160% x STR) damage. After finishing, **gain** 10% Max HP Barrier for the rest of the round.

**- Effect Logic:**

`RESOLVE_DURING_EXECUTION`: **Deal** (90/120/160% x STR) damage for each of the configured 3/4/5 strikes. The server distributes the configured strikes deterministically across the AS-adjusted effective interval, with the final strike at the inclusive endpoint.

`RESOLVE_ON_END`: **Gain** 10% Max HP Barrier.

### 4. Tempered Harmony - Cương Nhu Hỗn Thành (True Sword Sect)
**Activation type:** Passive

**Description:** Each round, whenever the performer deals damage to the opponent, **gain** 1 stack of **Intent**, up to 6 stacks. Each stack of **Intent** increases DEF by 5%/6%/7%. Upon reaching 6 stacks, immediately **gain** 10%/15%/20% STR.

### 5. Egoless Revelation - Vô Ngã Chứng Chân (True Sword Sect)
**- Cost:** 60/80/100 | **Activation type:** Active - Ultimate

**- Duration Type:** `FIXED` | **Duration:** 0.5s | **Cooldown:** 4s | **Stack:** 1

**- Description:** Enter the state of Egoless and **gain** 10%/20%/30% STR. If this is the 2nd activation, release a True Intent slash that **deals** (150%/200%/250% x STR) damage and additional (50% x STR) damage for each stack of *"Intent"*.

**- Effect Logic:**

`RESOLVE_ON_START`: **Gain** 10%/20%/30% STR.

`RESOLVE_ON_END`: If this is the 2nd activation, **deal** (150%/200%/250% x STR) damage and additional (50% x STR) damage for each stack of *"Intent"*.

### 6. White Rainbow Pierces the Sun - Bạch Hồng Quán Nhật (Flying Sword Sect)
**- Cost:** 40/60/90 | **Activation type:** Active

**- Duration Type:** `FIXED` | **Duration:** 1s | **Cooldown:** 1s | **Stack:** 1

**- Description:** Send the sword flying towards the target, then pierce through the target, **deal** (120/150/190% x STR) damage. 
(This action is considered as a **Slash**.)

**- Effect Logic:** 

`RESOLVE_ON_END`: **Deal** (120/150/190% x STR) damage. Considered as a **Slash**.

### 7. Heavenly Sword Circuit - Kiếm Luân Chu Thiên (Flying Sword Sect)
**- Cost:** 60/90/130 | **Activation type:** Active

**- Duration Type:** `FIXED` | **Duration:** 0.5s | **Cooldown:** 4s | **Stack:** 1

**- Description:** Circulate and weave the power of the flying swords into an unbroken cycle. Then, **gain** 10/15/20% STR and 20/30/40% AS and recover 30/40/50 Qi.

**- Effect Logic:** 

`RESOLVE_ON_END`: **Gain** 10/15/20% STR, 20/30/40% AS and **Recover** 30/40/50 ROUND_QI.

### 8. River of Myriad Blades - Vạn Kiếm Trường Hà (Flying Sword Sect)
**- Cost:** 100/130/170 | **Activation type:** Active

**- Duration Type:** `CONTROLLED` | **Cooldown:** 3s | **Stack:** 1

**- Duration Rule:** `X = ceil(4 x asSnapshot)` and `effectiveDurationTicks = X * 3`.

**- Description:** Converge the flying swords into an unbroken stream, then unleash a relentless assault that deals (45/55/70% x STR) damage with `X = ceil(4 x asSnapshot)` strikes. Each strike applies 1 **Bleed** to the target.

**- Effect Logic:** 
`RESOLVE_DURING_EXECUTION`: **Deal** (45/55/70% x STR) damage and apply 1 **Bleed** every 3 ticks (`periodIntervalTicks = 3`), including the inclusive endpoint. Calculate `X` once at Action start; the server uses the controlled runtime duration of `X * 3` ticks and does not divide it by AS again.

### 9. Threefold Sundering - Tam Điệp Phá Cương (Flying Sword Sect)
**Activation type:** Passive

**Description:** For every 3 consecutive **Slash**es, the 3rd **Slash** will **deal** additional (50/70/100% x STR) damage and this **Slash** damage will become **Direct Damage**.

### 10. Myriad Blades Crown the Ascendant - Vạn Kiếm Triều Tiên (Flying Sword Sect)
**- Cost:** 150/180/230 | **Activation type:** Active - Ultimate

**- Duration Type:** `FIXED` | **Duration:** 1s | **Cooldown:** 6s | **Stack:** 1

**- Description:** Converge the flying swords to **gain** (5/7/10% Max HP) Barrier and empower them with the aura of **Ascendance**. While in **Ascendance**, the performer **gains** 30%/50%/70% AS and applies 1 additional **Bleed** whenever the performer successfully deals damage. At `BATTLE_END`, after Bleed resolves, each Bleed stack attributed to the performer that resolved deals (7% x STR) damage to the target.

**- Effect Logic:** 

`RESOLVE_ON_START`: **Gain** 5/7/10% HP Barrier.

`RESOLVE_ON_END`: **Gain** 30%/50%/70% AS and **Ascendance**

While in Ascendance:
- Whenever the performer successfully **deals** damage, apply 1 additional **Bleed** to the target.
- At `BATTLE_END`, store only this performer's resolved Bleed stacks in `RESOLVED_BLEED_COUNT`, then **deal** (7% x `RESOLVED_BLEED_COUNT` x STR) damage.

### 11. Ambush - Tập (Shadow Sword Sect)
**- Cost:** 100/120/150 | **Activation type:** Active

**- Duration Type:** `FIXED` | **Duration:** 1.5s | **Cooldown:** 1.5s | **Stack:** 1

**- Description:** Hide in **Shadow**, gain 1 **Shade**, and **boost** 10%/20%/30% STR. Then spring forth and unleash an assassination strike that **deals** (150/170%/200% x STR) damage and, if the target is under **Shield** or **Defend**, **deals** additional (60%/70%/80% x STR) damage.

**- Effect Logic:**

`RESOLVE_ON_START`: Gain 1 **Shade** then **Boost** 10%/20%/30% STR.

`RESOLVE_DURING_EXECUTION`: Boost **Shadow**.

`RESOLVE_ON_END`: **Deal** (150/170/200% x STR) damage. If the target is under **Shield** or **Defend**, **deal** additional (60%/70%/80% x STR) damage.

### 12. Phantasm - Huyễn (Shadow Sword Sect)
**- Cost:** 50/70/100 | **Activation type:** Active

**- Duration Type:** `FIXED` | **Duration:** 0.7s | **Cooldown:** 1.5s | **Stack:** 2

**- Description:** 
1st stack: Consume 1 **Shade** to unleash an illusion strike that **deals** (150%/170%/200% x STR) damage.
2nd stack: Consume 2 **Shade**s to release a dazzling strike that **deals** (200%/250%/300% x STR) damage, then **gain** 5%/10%/15% STR.

**- Effect Logic:**
`RESOLVE_ON_START`: Check the current stacks:
1st: Consume 1 **Shade** to **deal** (150%/170%/200% x STR) damage.
2nd: Consume 2 **Shade**s to **deal** (200%/250%/300% x STR) damage then **gain** 5%/10%/15% STR.

### 13. Rend - Liệt (Shadow Sword Sect)
**- Cost:** 50/70/100 | **Activation type:** Active

**- Duration Type:** `FIXED` | **Duration:** 0.5s | **Cooldown:** 3s | **Stack:** 1

**- Description:** Instantly flash past the target, leaving behind a swift slash that **deals** (50%/70%/100% x STR) damage, then consume 1 **Shade** to reduce the target's DEF by 10/13/17 for the rest of the round.

**- Effect Logic:**
`RESOLVE_ON_START`: **Deal** (50%/70%/100% x STR) damage, then consume 1 **Shade** to reduce the target’s DEF by 10/13/17.

### 14. Predation - Liệp (Shadow Sword Sect)
**Activation type:** Passive

**Description:** When a *Shadow Sword Action* emits `ACTION_STARTED`, before its `RESOLVE_ON_START` Effects, **gain** 1 stack of **Shade** if the opponent is below 50% Health. **Shade** has a maximum of 4 stacks.

Whenever an Action consumes **Shade**, **boost** all damage dealt by that Action by 6%/8%/10% for each stack consumed.

**- Effect Logic:**

At `ACTION_STARTED`, before the Active Action's `RESOLVE_ON_START` Effects, check the opponent's current Health:

- At or above 50% Health: Gain no **Shade**.
- Below 50% Health: **Gain** 1 stack of **Shade**.

If the current Action consumes one or more stacks of **Shade**, **boost** its damage by 6%/8%/10% for each stack consumed.

### 15. Eclipse - Thực (Shadow Sword Sect)
**- Cost:** 150/200/250 | **Activation type:** Active - Ultimate

**- Duration Type:** `FIXED` | **Duration:** 1.5s | **Cooldown:** 7s | **Stack:** 1

**- Description:** Consume all stacks of **Shade** and accumulate the power of **Shadow**. Then, release a devastating strike that **deals** (100%/150%/200% x STR + 10% target's Missing HP) damage and additional (50% x STR) damage for each stack of **Shade** consumed.

**- Effect Logic:**

`RESOLVE_ON_START`: Consume all stacks of **Shade**, store the amount as `CONSUMED_SHADE` and Boost **Shadow**

`RESOLVE_DURING_EXECUTION`: Boost **Shadow**.

`RESOLVE_ON_END`: **Deal** (100%/150%/200% x STR + 10% target's Missing HP) damage and additional (50% x STR x `CONSUMED_SHADE`) damage

# Keywords
## Consume
**Consume** is the act of using up a resource to trigger an effect. The effect will only be triggered if the resource is available. If the resource is not available, the effect will not be triggered.

## Deal
The target will receive X damage from the performer. Note that this is not the final amount that will reduce the target's health. It is calculated by the formula:
> Damage Deal = Damage x Multiplier (from Passive, Effects,...).

## Damage Taken
The damage taken is the amount that will reduce the target's health. It is calculated by the formula: 
> Damage Taken = Damage Deal x 100/(100 + DEF).

## Direct Damage
**Direct Damage** will ignore the target's **DEF**, but it is still absorbed by Barrier.

## Recover
The target will increase an amount of X type of resource. The resource can be Qi, Health, or any other type of resource.

## Ending Effect
An **Ending Effect** triggers at `BATTLE_END` in deterministic application order.

## Bleed
**Bleed** is an **Ending Effect**. Each stack retains its source player and, at `BATTLE_END`, deals (5% x source STR read when the stack resolves) as **Direct Damage**. A damage instance is successfully dealt when it is greater than zero and is not ignored; Barrier absorption does not invalidate that damage instance. Flying Active 3 applies one Bleed per strike, and Ascendance applies one additional Bleed when the same strike successfully deals damage.

## Missing HP
**Missing HP** is `target.hp.max - target.hp.current` at the time the Effect resolves.

## Boost, Gain, Grant
**Boost**: The target receives a TEMPORARY effect during the Action's effective execution interval. The effect is removed when the Action ends.

**Gain**: The target receives a TEMPORARY effect during the *ROUND*. The effect is removed after `BATTLE_END` resolves.

**Grant**: The target receives a PERMANENT effect during the Game.

## Barrier
**Barrier** is a stat that will absorb damage before the target's health is reduced.

## Shadow
**Shadow** is a special state that ignores all **Slash** from others.

