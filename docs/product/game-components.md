# SwordVerse Components
## 3 BASIC ACTIONS
Basic Actions have infinite Stack and no Cooldown.
### Slash
**- Cost:** 40

**- Duration:** 1/AS(Attack Speed) seconds

**- Description:** Deal (100% x STR) damage to the target.

**- Effect Logic:** 
`RESOLVE_ON_END`: **Deal** (100% x STR) damage.

### Defend
**- Cost:** 20

**- Duration:** 1/AS(Attack Speed) seconds

**- Description:** Defending yourself, ignore all **Slash** damage from the target at the end of the Action Execution.

**- Effect Logic:** 
`RESOLVE_ON_END`: Ignore all upcoming **Slash**.

### Shield
**- Cost:** 60

**- Duration:** 1 seconds

**- Description:** **Boost** the DEF stat by 100% while executing the Action.

**- Effect Logic:** 
`RESOLVE_DURING_ACTION`: **Boost** 100% DEF.

## 3 SECTS
### True Sword Sect - Chân Kiếm Phái

**Description:** The True Sword Sect represents disciplined mastery of conventional swordsmanship. Its intended style is balanced, sustainable, resilient, and adaptable.

**Starting Stats:** 
>| 1000:heart: | 30:shield: | 150:crossed_swords: | 1:zap: |

**Support Stats:** +100:heart: +10:crossed_swords: +5:shield:

**Techniques:**

### Flying Sword Sect - Phi Kiếm Phái

**Description:** The Flying Sword Sect emphasizes speed, deception, mobility, and extended combinations. Its intended style is swift, adaptive, and difficult to predict.

**Starting Stats:** 
>| 900:heart: | 25:shield: | 180:crossed_swords: | 1.25:zap: |

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
>| 900:heart: | 20:shield: | 200:crossed_swords: | 1.25:zap: |

**Support Stats:** +15:crossed_swords: +0.1:zap:

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
**- Cost:** 50/80/100

**- Duration:** 1s | **Cooldown:** 1s | **Stack:** 1

**- Description:** Grip the sword with both hands, then release a precise strike, **deal** (150/200/250% x STR) damage.

**- Effect Logic:** 

`RESOLVE_ON_END`: **Deal** (150/200/250% x STR) damage.

### 2. Rising Reprisal - Thừa Kình Liêu Trảm (True Sword Sect)
**- Cost:** 55/85/110

**- Duration:** 1s | **Cooldown:** 1s | **Stack:** 1

**- Description:** Assume a guarded sword stance, **boost** 10/20/30 DEF while executing. On Completion, unleash an upward slash that **deal** (120/160/210% x STR) damage. If taken damage while in the stance, **deal** additional (50% x STR) damage.

**- Effect Logic:**

`RESOLVE_DURING_EXECUTION`: **Boost** 10/20/30 DEF.

`RESOLVE_ON_END`: **Deal** (120/160/210% x STR) damage. If taken damage in (0.0, 1.0), **deal** additional (50% x STR) damage.

### 3. One Thought, Myriad Edges - Nhất Niệm Vạn Kiếm (True Sword Sect)
**- Cost:** 70/90/120

**- Duration:** 2s | **Cooldown:** 1s | **Stack:** 1

**- Description:** Focus the mind upon a single thought. Then, release a flurry of (AS x 2) strikes that **deal** (90/120/160% x STR) damage each strikes.

**- Effect Logic:**

`RESOLVE_DURING_EXECUTION`: **Deal** (90/120/160% x STR) damage for every (1 / (AS x 2)) seconds, with minimum 1 strike and maximum 20 strikes.

### 4. Tempered Harmony - Cương Nhu Hỗn Thành (True Sword Sect)
**Description:** Each round, whenever the performer deals damage to the opponent, **gain** 1 stack of **Intent**, up to 6 stacks. Each stack of **Intent** increases DEF by 5%/6%/7%. Upon reaching 6 stacks, immediately **gain** 10%/15%/20% STR.

### 5. Egoless Revelation - Vô Ngã Chứng Chân (True Sword Sect)
**- Cost:** 60/80/100

**- Duration:** 0.5s | **Cooldown:** 4s | **Stack:** 1

**- Description:** Enter the state of Egoless and **gain** 10%/20%/30% STR. If this is the 2nd activation, release a True Intent slash that **deal** damage equals (150%/200%/250% x STR) and additional (50% x STR) damage for each stack of *"Intent"*.

**- Effect Logic:**

`RESOLVE_ON_START`: **Gain** 10%/20%/30% STR.

`RESOLVE_ON_END`: If this is the 2nd activation, **deal** (150%/200%/250% x STR) damage and additional (50% x STR) damage for each stack of *"Intent"*.

### 6. White Rainbow Pierces the Sun - Bạch Hồng Quán Nhật (Flying Sword Sect)
**- Cost:** 40/60/90

**- Duration:** 1/AS(Attack Speed)s | **Cooldown:** 1s | **Stack:** 1

**- Description:** Send the sword flying towards the target, then pierce through the target, **deal** (120/150/190% x STR) damage. 
(This action is considered as a **Slash**.)

**- Effect Logic:** 

`RESOLVE_ON_END`: **Deal** (120/150/190% x STR) damage. Considered as a **Slash**.

### 7. Heavenly Sword Circuit - Kiếm Luân Chu Thiên (Flying Sword Sect)
**- Cost:** 60/90/130

**- Duration:** 0.5s | **Cooldown:** 4s | **Stack:** 1

**- Description:** Circulate and weave the power of the flying swords into an unbroken cycle. Then, **gain** 10/15/20% STR and 20/50/100% AS and recover 30/40/50 Qi.

**- Effect Logic:** 

`RESOLVE_ON_END`: **Gain** 10/15/20% STR, 20/50/100% AS and **Recover** 30/40/50 Qi.

### 8. River of Myriad Blades - Vạn Kiếm Trường Hà (Flying Sword Sect)
**- Cost:** 100/130/170

**- Duration:** (**X** x 0.3)s | **Cooldown:** 3s | **Stack:** 1

**- Description:** Converge the flying swords into an unbroken stream, then unleash a relentless assault that deals (110/130/160% x STR) damage with **X**(4 x AS rounded up) strikes. Each strike will apply 1 **Bleed** to the target.

**- Effect Logic:** 
`RESOLVE_DURING_EXECUTION`: **Deal** (110/130/160% x STR) damage and apply 1 **Bleed** for every 0.3 seconds, with minimum 1 strike. (Total strike count = 4 x AS rounded up)

### 9. Threefold Sundering - Tam Điệp Phá Cương (Flying Sword Sect)
**Description:** For every 3 consecutive **Slash**es, the 3th **Slash** will **deal** additional (50/70/100% x STR) damage and this **Slash** damage will become **Direct Damage**.

### 10. Myriad Blades Crown the Ascendant - Vạn Kiếm Triều Tiên (Flying Sword Sect)
**- Cost:** 150/180/230

**- Duration:** 1s | **Cooldown:** 6s | **Stack:** 1

**- Description:** Converge the flying swords to **gain** (5/7/10% HP) Barrier and empower them with the aura of **Ascendance**. While in **Ascendance**, the performer will **gain** 50%/90%/150% AS and whenever the performer **deal** damage successfully, apply 1 **Bleed** to the target. And in the end of Battle Phase, after all **Bleed** resolved, each successful **Bleed** will deal (7% x STR) damage to the target.

**- Effect Logic:** 

`RESOLVE_ON_START`: **Gain** 5/7/10% HP Barrier.

`RESOLVE_ON_END`: **Gain** **Ascendance**

While in Ascendance:
- **Gain** 50%/90%/150% AS.
- Whenever the performer **deal** damage successfully, apply 1 **Bleed** to the target.
- In the end of Battle Phase, before **Ascendance** removed and after all **Bleed** resolved, each successful **Bleed** will **deal** (7% x STR) damage to the target.

### 11. Ambush - Tập (Shadow Sword Sect)
**- Cost:** 100/120/150

**- Duration:** 1.5s | **Cooldown:** 1.5s | **Stack:** 1

**- Description:** Hide into the **Shadow**, gain 1 **Shade** and **boost** 10%/20%/30% STR. Then spring forth and unleash an assassination strike that **deal** (180%/210%/250% x STR) damage and if the target is under **Guard** or **DEFEND**, **deal** additional (60%/70%/80% x STR) damage.

**- Effect Logic:**

`RESOLVE_ON_START`: Gain 1 **Shade** then **Boost** 10%/20%/30% STR.

`RESOLVE_DURING_EXECUTION`: Boost **Shadow**.

`RESOLVE_ON_END`: **Deal** (180%/210%/250% x STR) damage. If the target is under **Guard** or **Shield**, **deal** additional (60%/70%/80% x STR) damage.

### 12. Phantasm - Huyễn (Shadow Sword Sect)
**- Cost:** 50/70/100

**- Duration:** 0.7s | **Cooldown:** 1.5s | **Stack:** 2

**- Description:** 
1st stack: Consume 1 **Shade** to unleash illusion strike that **deal** (150%/170%/200% x STR) damage.
2nd stack: Consume 2 **Shade**s to release a dazling strike that **deal** (200%/250%/300% x STR) damage then **gain** 5%/10%/15% STR.

**- Effect Logic:**
`RESOLVE_ON_START`: Check the current stacks:
1st: Consume 1 **Shade** to **deal** (150%/170%/200% x STR) damage.
2nd: Consume 2 **Shade**s to **deal** (200%/250%/300% x STR) damage then **gain** 5%/10%/15% STR.

### 13. Rend - Liệt (Shadow Sword Sect)
**- Cost:** 50/70/100

**- Duration:** 0.5s | **Cooldown:** 3s | **Stack:** 1

**- Description:** Instantly flash past the target, leaving behind a swift slash that deal (50%/70%/100% x STR) damage, then consume 1 **Shade** to reduce the target’s DEF by 13/16/20 for the rest of the round.

**- Effect Logic:**
`RESOLVE_ON_START`: **Deal** (50%/70%/100% x STR) damage, then consume 1 **Shade** to reduce the target’s DEF by 13/15/18.

### 14. Predation - Liệp (Shadow Sword Sect)

**Description:** At the start of each *Shadow Sword Action*, before effect triggered, if the opponent is below 50% Health, **gain** 1 stack of **Shade**. **Shade** has a maximum of 4 stacks.

Whenever an Action consumes **Shade**, **boost** all damage dealt by that Action by 6%/8%/10% for each stack consumed.

**- Effect Logic:**

`RESOLVE_ON_START` (Before consuming): Check the opponent’s current Health:

- At or above 50% Health: Gain no **Shade**.
- Below 50% Health: **Gain** 1 stack of **Shade**.

If the current Action consumes one or more stacks of **Shade**, **boost** its damage by 6%/8%/10% for each stack consumed.

### 15. Eclipse - Thực (Shadow Sword Sect)
**- Cost:** 150/200/250

**- Duration:** 1.5s | **Cooldown:** 7s | **Stack:** 1

**- Description:** Consume all stacks of **Shade** and accumulate the power of **Shadow**. Then, release a devastating strike that **deal** (100%/150%/200% x STR + 10% Opponent's HP loss) damage and additional (50% x STR) damage for each stack of **Shade** consumed. 

**- Effect Logic:**

`RESOLVE_ON_START`: Consume all stacks of **Shade**, store the amount as `CONSUMED_SHADE` and Boost **Shadow**

`RESOLVE_DURING_EXECUTION`: Boost **Shadow**.

`RESOLVE_ON_END`: **Deal** (100%/150%/200% x STR + 10% Opponent's HP loss) damage and additional (50% x STR x `CONSUMED_SHADE`) damage

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
**Direct Damage** will ignore the target's **DEF** and reduce the target's health directly.

## Recover
The target will increase an amount of X type of resource. The resource can be Qi, Health, or any other type of resource.

## Ending effect
**Ending effect** is an effect that will be triggered at the end of the Battle Phase. The effect will be executed in the order based on which effect is stacked first.

## Bleed
**Bleed** is an **Ending effect** that will cause the target to **take** (5% Performer STR) **Direct damage** for each stack in the end of Battle Phase. 

## Boost, Gain, Grant
**Boost**: The target receives a TEMPORARY effect during the *Action Execution*, from (0.0, 1.0]. The effect will be removed after the Action Execution ends.

**Gain**: The target receives a TEMPORARY effect during the *ROUND*. The effect will be removed after the end of Battle Phase (after all Battle End Effect resolved).

**Grant**: The target receives a PERMANENT effect during the Game.

## Barrier
**Barrier** is a stat that will absorb damage before the target's health is reduced.

## Shadow
**Shadow** is a special state that ignores all **Slash** from others.

<span style="color: orange;"></span>
