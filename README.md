# cloud-itonami-isco-4132

Open Occupation Blueprint for **ISCO-08 4132**: Data Entry Clerks.

This repository designs a forkable OSS business for an independent data entry clerk: a document-scanning robot performs physical document feeding and OCR-assist scanning under a governor-gated actor, so the practice keeps its own entry and verification records instead of renting a closed data-entry SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a document-scanning robot performs physical document feeding and OCR-assist scanning under an actor that proposes
actions and an independent **Data Entry Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
entering data into a regulated financial or medical record system, or overriding a validation failure) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
client dataset + entry protocol + validation rules
        |
        v
Data Entry Advisor -> Data Entry Governor -> enter/verify, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `4132`). Required capabilities:

- :robotics
- :forms
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
