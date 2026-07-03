(ns data-entry.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-entry.actor :as actor]
            [data-entry.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-dataset! st {:dataset-id "dataset-1" :name "Q3 Client Records"})
    st))

(deftest commits-a-clean-low-risk-request
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:dataset-id "dataset-1" :op :enter :stake :low}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "dataset-1"))))))

(deftest holds-on-unregistered-dataset-without-committing
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:dataset-id "no-such-dataset" :op :enter :stake :low}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :done (:status result)))
    (is (nil? (get-in result [:state :record])))
    (is (empty? (store/records-of st "no-such-dataset")))
    (is (= :hold (:disposition (:state result))))))

(deftest interrupts-then-commits-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        ;; regulated-record entry always escalates (governor invariant)
        request {:dataset-id "dataset-1" :op :enter-regulated-record :stake :high}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "dataset-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (some? (get-in resumed [:state :record])))
      (is (= 1 (count (store/records-of st "dataset-1")))))))
