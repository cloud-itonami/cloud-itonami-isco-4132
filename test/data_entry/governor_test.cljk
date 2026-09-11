(ns data-entry.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-entry.store :as store]
            [data-entry.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-dataset! st {:dataset-id "dataset-1" :name "Q3 Client Records"})
    st))

(deftest ok-on-clean-enter
  (let [st (fresh-store)
        proposal {:op :enter :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:dataset-id "dataset-1"} {} proposal st)]
    (is (:ok? v))
    (is (not (:hard? v)))
    (is (not (:escalate? v)))))

(deftest hard-on-unregistered-dataset
  (let [st (fresh-store)
        proposal {:op :enter :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:dataset-id "no-such-dataset"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-dataset (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        proposal {:op :enter :effect :direct-write :confidence 0.9 :stake :low}
        v (governor/check {:dataset-id "dataset-1"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest escalates-on-regulated-record-entry
  (let [st (fresh-store)
        proposal {:op :enter-regulated-record :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:dataset-id "dataset-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-validation-failure-override
  (let [st (fresh-store)
        proposal {:op :override-validation-failure :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:dataset-id "dataset-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-low-confidence
  (let [st (fresh-store)
        proposal {:op :enter :effect :propose :confidence 0.2 :stake :low}
        v (governor/check {:dataset-id "dataset-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest store-records-and-ledger-append-only
  (let [st (fresh-store)]
    (store/commit-record! st {:dataset-id "dataset-1" :op :verify})
    (store/append-ledger! st {:disposition :commit})
    (is (= 1 (count (store/records-of st "dataset-1"))))
    (is (= 1 (count (store/ledger st))))))
