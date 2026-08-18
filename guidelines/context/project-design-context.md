# IAHomeLab — Project Design Context

> Historical design context for IAHomeLab.
>
> This document preserves concepts and architectural decisions discussed during early design sessions so they are not lost. It is **not** the active backlog and does not imply that every concept must be implemented.
>
> The GitHub Issues remain the source of truth for planned work. Current implementation decisions always take precedence over this historical context.

## Product intent

IAHomeLab is a personal laboratory for discovering, understanding, building, experimenting with, measuring and evolving reproducible AI systems.

The project is intentionally generic enough to support several AI families over time:

- LLMs
- RAG
- agents
- vision
- OCR
- speech-to-text / text-to-speech / audio
- multimodal systems

The goal is not to build a generic CRUD application. The useful workflow is to turn technological watch, ideas and claims into reproducible experiments and retained knowledge.

## Long-term workflow explored during design

```text
Source(s)
   ↓
Idea / Claim
   ↓
Experiment
   ↓
SetupVersion
   ↓
Variants
   ↓
Git Workspace
   ↓
Execution
   ↓
Runs
   ↓
Results / Artifacts
   ↓
Evaluators / Metrics / Findings
   ↓
Promotion / retained knowledge
```

This is a target workflow, not the V1 implementation plan.

## Early large domain model

An early design explored a much larger model, around thirty entities, in order to test how far the domain could eventually go.

That model included concepts around:

- research sources and papers
- ideas and claims
- setup definitions and versions
- generic component instances
- connections between components
- experiments
- variants
- input sets
- workspaces and Git references
- executions / runs
- results
- artifacts
- evaluators
- metrics
- findings
- promotion / comparison concepts

The project deliberately moved away from implementing that whole model up front.

## V1 simplification

The design was reduced to a small core, roughly 9–12 concepts, to avoid premature complexity.

The main candidates retained for the first useful product slice are:

```text
Source / Paper
Setup
SetupVersion
ComponentInstance
Connection
Experiment
Variant
Workspace
Finding
```

Possible later additions:

```text
InputSet
Metric
Run
Artifact
Evaluator
```

Automated execution and evaluation are explicitly later concerns.

## Core conceptual decisions

### Source / Research

A source represents material found during technological watch and research.

Potential source forms include:

- web article
- paper
- Git repository
- documentation
- video or other external resource if later useful

The purpose is not merely bookmarking. A source should eventually be able to lead to an idea, claim, experiment or finding.

### Setup

A `Setup` represents a logical AI system or configuration that can evolve over time.

Examples:

- a basic LLM prompt pipeline
- a RAG pipeline
- an agent
- an OCR chain
- a vision pipeline
- an audio pipeline
- a multimodal chain

### SetupVersion

A `SetupVersion` freezes a reproducible version of a setup.

The important distinction is:

```text
Setup        = logical system over time
SetupVersion = immutable/reproducible snapshot used by an experiment
```

### ComponentInstance

A setup version may be made from generic component instances rather than hard-coding a separate entity for each AI technology.

Examples of components could eventually include:

- model
- prompt
- retriever
- embedding model
- vector store
- reranker
- tool
- OCR engine
- speech model
- vision model

The exact component taxonomy was intentionally left open.

### Connection

A `Connection` describes how component instances are linked within a setup version.

This allows different AI architectures to be represented without redesigning the relational model for each new technology.

### Experiment

An `Experiment` represents a question or hypothesis being tested.

It should reference a reproducible setup version and eventually allow comparisons between alternatives.

### Variant

A `Variant` represents a controlled modification relative to an experiment/baseline.

An early option was to keep changes in JSONB where this is useful instead of building a large relational model for every possible parameter variation.

### Workspace

A `Workspace` links an experiment to the code actually used.

Relevant information may eventually include:

```text
repository
branch
commit
working directory / project reference
```

The intent is reproducibility: an experiment should be traceable to the exact code version used.

### Finding

A `Finding` captures retained knowledge from research or experiments.

The project should ultimately answer questions such as:

> What have I already learned about this technique?

rather than only retaining raw execution history.

Possible properties discussed include:

- conclusion / free text
- positive / negative / neutral interpretation
- confidence
- tags
- links to experiments or sources

## Deferred execution model

A larger automated execution model was discussed but postponed.

Possible later concepts:

```text
Experiment
   ↓
Run
   ↓
Input / Output
   ↓
Artifacts
   ↓
Metrics / Evaluators
```

Possible recorded execution data:

- duration
- token usage
- cost
- outputs
- artifacts
- metric values
- evaluator results

The V1 should not require an automated runner to be useful.

## Current implementation philosophy

The project currently favors:

- simple Spring Boot module/package structure
- PostgreSQL + Flyway
- real PostgreSQL integration tests through Testcontainers
- conventional JPA repositories/services/controllers
- DTOs at the HTTP boundary
- `/api/v1` as the API prefix
- incremental domain design rather than implementing the historical target model all at once

## Security context

The V1 security direction is:

```text
local authentication
HTTP session
CSRF enabled
USER / ADMIN roles
```

OAuth2 / OIDC is an evolution path, not required for the first version.

The internal identity model separates:

```text
AppUser
   ↓ 1..N
UserIdentity
```

This permits future identities such as Google, GitHub, Keycloak or another OIDC/OAuth2 provider without replacing the application user model.

See `guidelines/docs/security.md` for the implementation-oriented security documentation.

## Backlog relationship

The current roadmap is tracked in GitHub Issues, notably:

```text
#1 Foundation & Developer Experience
#2 Authentication & Security V1
#3 Research & Sources
#4 AI Setup & Versioning
#5 Experiments & Variants
#6 Git Workspace Integration
#7 Findings & Knowledge
#8 Automated Runs & Evaluation
```

Those issues define what is actually planned and validated.

This context document exists only to preserve the richer design space so useful ideas can be recovered later without forcing them into the V1.

## Working rule

When revisiting an old concept from this document:

1. treat it as design context, not an already-approved requirement;
2. compare it with the current GitHub issue and current code;
3. simplify it if a smaller model solves the immediate need;
4. only update an issue checklist after explicit user validation.
