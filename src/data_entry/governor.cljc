(ns data-entry.governor
  "DataEntryGovernor — the independent safety/traceability layer for
  the ISCO-08 4132 independent data-entry actor. Wired as its own
  `:govern` node in `data-entry.actor`'s StateGraph, downstream of
  `:advise` — the Advisor has no notion of dataset provenance or
  regulated-record/validation-override risk, so this MUST be a
  separate system able to reject a proposal (itonami actor pattern,
  per ADR-2607011000 / CLAUDE.md Actors section).

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. dataset provenance  — the request's dataset must be
       registered.
    2. no-actuation          — proposal :effect must be :propose.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off, per the
  README robotics-premise: entering data into a regulated financial or
  medical record system and overriding a validation failure always
  require human sign-off):
    3. :op :enter-regulated-record.
    4. :op :override-validation-failure.
    5. low confidence (< `confidence-floor`)."
  (:require [data-entry.store :as store]))

(def confidence-floor 0.6)
(def ^:private escalating-ops #{:enter-regulated-record :override-validation-failure})

(defn- hard-violations [{:keys [proposal]} dataset-record]
  (cond-> []
    (nil? dataset-record)
    (conj {:rule :no-dataset :detail "未登録 dataset"})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect は :propose のみ許可（直接書込禁止）"})))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `data-entry.store/Store`. Returns
  `{:ok? bool :violations [...] :confidence n :hard? bool :escalate? bool}`."
  [request context proposal store]
  (let [dataset-record (store/dataset store (:dataset-id request))
        hard (hard-violations {:proposal proposal} dataset-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        risky-op? (contains? escalating-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not risky-op?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? risky-op?))}))
