(ns data-entry.store
  "SSoT for the ISCO-08 4132 independent data-entry sole-proprietor
  actor. Store is a protocol injected into the `data-entry.actor`
  StateGraph — `MemStore` is the default, deterministic, zero-dep
  backend; a Datomic/kotoba-server-backed implementation can be
  swapped in without touching the actor or governor (itonami actor
  pattern, per ADR-2607011000 / CLAUDE.md Actors section).

  Domain:

    dataset  — a registered client dataset (:dataset-id, :name)
    record   — a committed operating record under a dataset (entry,
               verification, regulated-record entry, validation-
               failure override) — written ONLY via commit-record!,
               never mutated in place
    ledger   — an append-only audit trail of every proposal/verdict/
               disposition, regardless of outcome (commit or hold)")

(defprotocol Store
  (dataset [s dataset-id])
  (records-of [s dataset-id])
  (ledger [s])
  (register-dataset! [s dataset])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (dataset [_ dataset-id] (get-in @a [:datasets dataset-id]))
  (records-of [_ dataset-id] (filter #(= dataset-id (:dataset-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-dataset! [s dataset]
    (swap! a assoc-in [:datasets (:dataset-id dataset)] dataset) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:datasets {} :records [] :ledger []} seed)))))
