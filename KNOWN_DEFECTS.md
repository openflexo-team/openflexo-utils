# openflexo-utils — Known defects

Bugs of openflexo-utils that are known. Each entry says what is verified, how to reproduce it, and how to work around it. Features and evolutions go to `BACKLOG.md`, not here.

Identifiers are stable and never reused: `UTILS-D-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

## Toolbox

### UTILS-D-1 — `FlexoProperties` no longer stores its entries sorted on Java 9 and later  ·  `DONE`

**Symptom.** A dictionary of the localization (`*.dict`) saved by the application is written in the order of a hash table: adding four entries to `DianaLocalization/English.dict` changed more than 500 lines.

**Verified.** `FlexoProperties` sorted by overriding `keys()`, which `Properties.store` used up to Java 8. Since Java 9 it writes the elements of `entrySet()`.

**Done.** `FlexoProperties.entrySet()` gives the entries sorted by key while the properties are being stored; `TestFlexoProperties` fails on the former code. A file saved by an older version is rewritten sorted the first time it is saved.
