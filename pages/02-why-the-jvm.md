---
layout: intro
class: section-intro
---

# Why agents belong on the JVM

## the hard part was never the model call

<!--
- Not bashing Python - it earned its place
- Short version: what changed, what a production agent needs
-->

---
class: graphic-slide
---

# LLMs moved the heavy lifting into the datacenter

<div class="flow">
  <div class="flow-node"><ph-laptop /><span>your application</span></div>
  <div class="flow-arrow flow-arrow-labelled"><span>HTTPS</span></div>
  <div class="flow-node flow-node-wide">
    <div class="stack-logos">
      <simple-icons-openai />
      <simple-icons-anthropic />
      <logos-google-gemini-icon />
      <logos-mistral-ai-icon />
    </div>
    <span>thousands of GPUs</span>
  </div>
</div>

<!--
- Python won on the math libs - C and C++ underneath
- LLMs too big to run locally
  - datacenters, plain HTTP APIs
- "Python has the math libs" argument gone
- Left on our side: classic enterprise software
  - calling services, JSON, validation, failure handling
-->

---
class: graphic-slide
---

# Agents in production need what Java has delivered for decades

<div class="pillar-grid">
  <div class="pillar"><ph-books /><span>context</span></div>
  <div class="pillar"><ph-lifebuoy /><span>reliability</span></div>
  <div class="pillar"><ph-brackets-curly /><span>type safety</span></div>
  <div class="pillar"><ph-lightning /><span>performance</span></div>
  <div class="pillar"><ph-chart-line-up /><span>observability</span></div>
  <div class="pillar"><ph-plugs-connected /><span>integration</span></div>
</div>

<!--
- Demo agent: prompt + API key
- Production agent needs more
  - context - data, domain model, business rules
  - reliability - retries, timeouts, transactions
  - type safety - output checked by the compiler
  - performance - concurrent network calls, virtual threads
  - observability - what the agent did and why
  - integration - systems we already run
- Nothing new - Java's job for decades
-->

---
class: graphic-slide
---

<div class="pillar-tag"><ph-plugs-connected />integration</div>

# Put the agent inside the system, not next to it

<div class="compare">
  <div class="compare-card compare-bad">
    <div class="compare-flow">
      <logos-java /><ph-arrow-right /><simple-icons-modelcontextprotocol /><ph-arrow-right /><logos-python />
    </div>
    <span>an MCP server and a Python agent per legacy system</span>
  </div>
  <div v-click class="compare-card compare-good reveal">
    <div class="compare-flow">
      <logos-java /><ph-plus /><ph-robot />
    </div>
    <span>the agent lives in the Java project you already have</span>
  </div>
</div>

<!--
- Python way
  - MCP server per legacy system
  - separate Python agent
  - two stacks, deployments, teams
- [click] Agent inside the Java project
  - same build, tests, ops, people
  - tools = plain method calls
  - same transactions, security, DB connections
-->

---
class: graphic-slide
---

# Most AI projects fail because they automate the wrong thing

<div class="compare">
  <div class="compare-card compare-bad">
    <ph-chats-circle class="compare-icon" />
    <span>replacing human interaction</span>
  </div>
  <div v-click class="compare-card compare-good reveal">
    <ph-arrows-clockwise class="compare-icon" />
    <span>boring, repetitive, time-consuming processes</span>
  </div>
</div>

<!--
- Automating human interaction = usually wrong
  - chatbots replacing support, sales, advisors
- [click] Boring, repetitive, time-consuming processes
  - already running in Java
- Next: one of those processes
-->
