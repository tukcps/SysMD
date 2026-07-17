# SysMD Metamodel Generator

The SysMD Metamodel Generator derives parts of the SysMD implementation from the
official OMG KerML/SysML metamodel.

The goal is **not** to generate the complete compiler. Instead, it provides a
single source of truth for the metamodel and automatically generates parts of
the SysMD infrastructure.

## Motivation

Historically, SysMD contained manually maintained metamodel information:

- `ElementType`
- type hierarchies
- factories
- REST schemas
- badges
- ...

This information is largely contained in the official OMG metamodel and should
therefore not be duplicated manually.

The generator minimizes manual maintenance while keeping SysMD compatible with
future KerML and SysML versions.

## Architecture

```
OMG XMI
    │
    ▼
MOFXmiLoader
    │
    ▼
MOFMetaModel
    │
    ├── ElementTypeGenerator
    ├── HierarchyGenerator
    ├── FactoryGenerator
    ├── SchemaGenerator
    └── ...
```

The `MOFMetaModel` is intentionally independent from SysMD and represents only
the information contained in the official OMG metamodel.

Each generator produces exactly one artifact.

## Development Strategy

The migration follows a strict incremental strategy.

- Every commit must leave SysMD releasable.
- Existing public interfaces are preserved whenever possible.
- New generated code replaces manually maintained code step by step.
- Refactorings are postponed until after successful migration.

The migration therefore consists of many small, independently testable steps.

## Current Status

Implemented:

- ✓ Download official OMG XMI
- ✓ Read XMI documents
- ✓ Read package hierarchy
- ✓ Read metaclasses
- ✓ Read inheritance hierarchy

Planned:

- ElementType generator
- Hierarchy generator
- Factory generator
- REST/OpenAPI schema generator
- Documentation generator

## Design Principles

- Use the official OMG metamodel as the single source of truth.
- Keep XML parsing isolated in the loader.
- Keep generators independent of XML.
- Generate code whenever possible instead of maintaining duplicate information.
- Prefer many small commits over large migrations.
- SysMD should remain releasable throughout the migration.