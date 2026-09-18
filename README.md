# Calorie Bank

A bring‑your‑own‑key calorie tracker that treats the weight you want to lose as a **bank balance of stored energy**.

- **Opening balance** = (weight − goal weight) × 7 700 kcal.
- **BMR** (Mifflin–St Jeor) × activity factor = **TDEE**, your daily burn.
- Each day settles as `eaten − TDEE`; eat under your burn and the balance shrinks, over it and it grows.
- **Transactions**: every food item, exercise credit, daily settlement and weigh‑in adjustment is a ledger entry.
- **Estimated zero date**: from your 7‑day average pace and from your planned deficit.
- **Water tracker**: goal ≈ 35 ml/kg (+activity bump), quick‑add, 7‑day history.
- **AI estimates (BYOK)**: describe a meal or snap/pick a photo of it, get per‑item calories/macros. Claude (Anthropic) by default; any OpenAI‑compatible endpoint also works. Keys stay on‑device.

## Stack
Jetpack Compose · **Material 3 Expressive** (`material3` 1.5.0‑alpha28 via `compose-bom-alpha` 2026.09.00 — `MaterialExpressiveTheme`, wavy progress indicators, flexible top app bars, connected toggle button groups, `LoadingIndicator`) · Room (KSP) · Navigation Compose · AGP 9.4 built‑in Kotlin · compileSdk 37.1 · minSdk 26.

## Build
```bash
./gradlew :app:assembleDebug
```
