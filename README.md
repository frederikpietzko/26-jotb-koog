# J On The Beach 2026 - Ditch Python and build Enterprise-Ready AI Agents on the JVM with Koog

Slides (built with [Slidev](https://sli.dev)) and live-coding demo for my talk at J On The Beach 2026.

- `slides.md`, `pages/` - the deck
- `demo/` - Kotlin + Spring Boot travel policy service, the Koog agent is written live (see [`demo/README.md`](demo/README.md))
- `ide/templates/demo.xml` - IntelliJ live templates used during the live coding

## Running the slides

- `pnpm install`
- `pnpm run dev`
- visit <http://localhost:3030>

## IntelliJ setup

Open the repository root in IntelliJ IDEA - the Gradle project in `demo/` is linked via `.idea/gradle.xml`.

The live templates (abbreviations `agent`, `basic`, `toolset`, `nodes`, `edges`, `basicSubgraph`, `judge`,
`refine`, `edgesfinal`, `tripstatusupdate`, `langfuse`, `peristance`; group `demo`, Kotlin context) are
not picked up from the project automatically. Install them once with IntelliJ closed:

```shell
cp ide/templates/demo.xml ~/Library/Application\ Support/JetBrains/IntelliJIdea2026.2/templates/
```

Replace `IntelliJIdea2026.2` with the IDE version used on stage.
