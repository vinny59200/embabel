# Embabel + Spring Boot + Wikidata (no auth, no real LLM)

A tiny **Spring Boot 4** API that uses **Embabel** as an *agentic orchestrator* to query **Wikidata** (public, free, no API key) and return a clean “definition” payload.

The goal of this project is to demonstrate **agent-style orchestration** (planning + action chaining) on a real, reproducible data source—**without requiring followers to install local models** or configure paid cloud credentials.

---

## What this API does

Call:

```bash
curl --request GET \
  --url 'http://localhost:8080/api/wiki/define?term=kafka'
```

Get:

```json
{
  "term": "kafka",
  "entityId": "Q16235208",
  "label": "Apache Kafka",
  "description": "open source data stream processing platform",
  "wikidataUrl": "https://www.wikidata.org/wiki/Q16235208",
  "wikipediaUrl": "https://en.wikipedia.org/wiki/Apache_Kafka"
}
```

---

## Why Embabel here?

Yes, this could be implemented with a single REST call.

But Embabel adds something different: a **structured, observable, composable execution flow**.

In this demo, Embabel:

- **Plans** the steps needed to reach a goal (`DefinitionResult`)
- **Executes** actions in order
- **Binds** intermediate objects (`WikidataEntityId`, `WikidataEntityDetails`)
- Produces **clear logs** showing the chain of reasoning *as a plan*, even with *zero* LLM usage

That’s the point: **agentic orchestration is valuable beyond “LLM chat”.**

---

## Architecture (high level)

```text
HTTP GET /api/wiki/define?term=...
        |
        v
WikiController
        |
        v
WikiService  (invokes Embabel goal)
        |
        v
Embabel AgentPlatform plans actions to produce DefinitionResult
        |
        +--> WikidataDefinitionAgent.findEntityId(term)  -> WikidataEntityId(Q...)
        |
        +--> WikidataDefinitionAgent.fetchDetails(Q...)  -> WikidataEntityDetails(label, desc, wikiTitle)
        |
        +--> WikidataDefinitionAgent.build(...)          -> DefinitionResult(JSON)
```

Wikidata calls are performed via Spring Boot’s `RestClient`:

- `/w/api.php?action=wbsearchentities...` to find the best matching Q-id
- `/wiki/Special:EntityData/{id}.json` to fetch labels, descriptions, and sitelinks

---

## Tech stack

- Java **25**
- Spring Boot **4.0.3**
- Embabel agent platform **0.3.4**
- Spring Web + RestClient
- Spring AI interfaces (**only to provide a noop `ChatModel`**)

---

## Prerequisites

- JDK 25 installed and available in your `PATH`
- Maven (or just use the provided Maven Wrapper)

---

## Run it

### 1) Clone

```bash
git clone https://github.com/vinny59200/embabel.git
cd embabel
```

### 2) Start the app

With Maven Wrapper (recommended):

```bash
./mvnw spring-boot:run
```

Windows:

```bat
mvnw.cmd spring-boot:run
```

The API starts on:

- `http://localhost:8080`

---

## Use it

### Define a term

```bash
curl --request GET \
  --url 'http://localhost:8080/api/wiki/define?term=kafka'
```

Try a few more:

```bash
curl --request GET --url 'http://localhost:8080/api/wiki/define?term=spring%20boot'
curl --request GET --url 'http://localhost:8080/api/wiki/define?term=java'
curl --request GET --url 'http://localhost:8080/api/wiki/define?term=quarkus'
```

### When a term is not found

The API returns **HTTP 404** with:

- `No Wikidata entity found for term: <term>`

---

## Configuration

`src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: wikidemo

server:
  port: 8080

embabel:
  models:
    default-llm: noop
```

### Why `default-llm: noop`?

Embabel expects a “default LLM” model to exist at startup.  
This demo intentionally uses **no real LLM**, so it registers a **noop** model and points Embabel to it.

---

## Code tour (key files)

- `WikiController`  
  Exposes the endpoint:

  `GET /api/wiki/define?term=...`

- `WikiService`  
  Invokes Embabel with a goal type (`DefinitionResult`).

- `WikidataDefinitionAgent`  
  The Embabel agent with three actions:
  1. `findEntityId(DefinitionRequest)` -> `WikidataEntityId`
  2. `fetchDetails(WikidataEntityId)` -> `WikidataEntityDetails`
  3. `build(...)` -> `DefinitionResult` (**goal achieved**)

- `WikidataRepository`  
  Calls Wikidata using Spring’s `RestClient` and maps minimal JSON DTOs.

- `NoopChatModel` + `WikiDemoApplication.noopLlm()`  
  Registers a fake LLM so Embabel can boot with `default-llm: noop`.

---

## Sample Embabel logs (what to look for)

You should see a plan like:

```text
formulated plan:
  findEntityId -> fetchDetails -> build
```

Then:

- “executing action …”
- “object bound it:WikidataEntityId”
- “object bound it:WikidataEntityDetails”
- “object bound it:DefinitionResult”
- “goal … achieved”

This is one of the most useful things for demos: you can *show the orchestration* clearly.

---

## Troubleshooting

### `Default LLM ... not found in available models: []`

Fix:

- Keep `embabel.models.default-llm: noop`
- Ensure `WikiDemoApplication` registers the noop LLM bean (`noopLlm()`)

### `Jackson2ObjectMapperBuilder ... could not be found`

This project uses:

- `spring-boot-jackson2`

(And intentionally excludes `spring-boot-starter-json` from `spring-boot-starter-web`.)

### `RestClient$Builder ... could not be found`

Fix:

- Ensure the dependency is present: `spring-boot-starter-restclient`

---

## Ideas to extend the demo

- Add a **SPARQL** action (Wikidata Query Service) to answer richer questions.
- Add **disambiguation**: return top N hits and let the user pick a Q-id.
- Add **language support** (`?lang=fr` -> labels/descriptions in French).
- Add **caching** (Caffeine) to avoid hitting Wikidata repeatedly.
- Swap the noop LLM for a real one and add an action that:
  - summarizes the Wikidata description
  - converts it to bullet points
  - generates a short “definition” suitable for documentation

---

## License

This repository currently doesn’t ship a `LICENSE` file.  
If you plan to reuse or fork it publicly, consider adding an explicit license.

---

## Author

Vincent Vauban  
GitHub: `vinny59200`
