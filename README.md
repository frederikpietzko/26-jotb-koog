# J On The Beach 2026 - Ditch Python and build Enterprise-Ready AI Agents on the JVM with Koog

Slides (built with [Slidev](https://sli.dev)) and live-coding demo for my talk at J On The Beach 2026.

**Slides: <https://frederikpietzko.github.io/26-jotb-koog/>**

- `slides.md`, `pages/` - the deck
- `demo/` - Kotlin + Spring Boot travel policy service, the Koog agent is written live (see [`demo/README.md`](demo/README.md))
- `ide/templates/demo.xml` - IntelliJ live templates used during the live coding

## Running the slides

- `pnpm install`
- `pnpm run dev`
- visit <http://localhost:3030>

## Running the demo

The demo calls OpenAI through Spring AI. Set these values in
[`demo/src/main/resources/application.yml`](demo/src/main/resources/application.yml):

| Property | Value |
| --- | --- |
| `spring.ai.openai.api-key` | Your OpenAI API key |
| `spring.ai.openai.base-url` | `https://api.openai.com` (or the URL of another OpenAI-compatible endpoint) |
| `spring.ai.openai.responses.model` | A model that supports the Responses API, default `gpt-5-mini` |

By default `api-key` and `base-url` read `${OPENAI_API_KEY}` and `${OPENAI_BASE_URL}`.

```shell
cd demo
./gradlew bootRun
```

See [`demo/README.md`](demo/README.md) for the seeded data and the optional Langfuse tracing setup.

## Deployment

Every push to `main` builds the deck and publishes it to GitHub Pages via
[`.github/workflows/deploy.yml`](.github/workflows/deploy.yml). In the GitHub repository, set
**Settings → Pages → Source** to **GitHub Actions** once. The workflow builds with
`--base /<repository-name>/`, so the URL follows the repository name.

## IntelliJ setup

Open the repository root in IntelliJ IDEA - the Gradle project in `demo/` is linked via `.idea/gradle.xml`.

The live templates (abbreviations `agent`, `basic`, `toolset`, `nodes`, `edges`, `basicSubgraph`, `judge`,
`refine`, `edgesfinal`, `tripstatusupdate`, `langfuse`, `peristance`; group `demo`, Kotlin context) are
not picked up from the project automatically. Install them once with IntelliJ closed:

```shell
cp ide/templates/demo.xml ~/Library/Application\ Support/JetBrains/IntelliJIdea2026.2/templates/
```

Replace `IntelliJIdea2026.2` with the IDE version used on stage.
