# Implementazione Struttura Dati KMP (Passo 1)

Il primo passo consiste nel migrare le classi di entità e le funzioni di utilità pura dal progetto Android esistente (`GestBraccianti`) al modulo `shared` del progetto KMP (`GestBracciantiW`).

## User Review Required
- Le classi verranno spostate in `com.aistudio.gestbracciantiw`.
- Le funzioni di utilità che usano `java.text.*` dovranno essere adattate per KMP. Utilizzeremo `kotlinx-datetime` o formatter multiplatform se necessario, ma per ora manterremo la logica pura.

## Open Questions
- Necessità di gestire i formattatori di data (`SimpleDateFormat`) che sono specifici di Java/Android. Proporrò di usare `kotlinx-datetime` o di astrarre i formattatori.

## Proposed Changes

### [shared] (Modulo Multiplatform)

#### [NEW] [Entità](file:///C:/Users/mgatt/AndroidStudioProjects/GestBracciantiW/shared/src/commonMain/kotlin/com/aistudio/gestbracciantiw/data/entity/)
- `Worker.kt`
- `HarvestYear.kt`
- `WorkLog.kt`
- `Plantation.kt`
- `WorkerYearConfig.kt`
- `WorkerGroup.kt`
- `WorkerGroupCrossRef.kt`

#### [NEW] [Modelli](file:///C:/Users/mgatt/AndroidStudioProjects/GestBracciantiW/shared/src/commonMain/kotlin/com/aistudio/gestbracciantiw/data/model/)
- `WorkerWithRate.kt`
- `WorkerYearStats.kt`

#### [NEW] [Utilità](file:///C:/Users/mgatt/AndroidStudioProjects/GestBracciantiW/shared/src/commonMain/kotlin/com/aistudio/gestbracciantiw/ui/utils/)
- `TimeUtils.kt` (Versione KMP)
- `NumberUtils.kt` (Versione KMP)
- `StringUtils.kt` (Versione KMP)

## Verification Plan

### Automated Tests
- Compilazione del modulo `shared` per `android`, `js` e `wasmJs`.
- Creazione di un test in `commonTest` per verificare i calcoli di `TimeUtils` e `NumberUtils`.
