(ns clj-restmachine.type)

(defprotocol Resource
  "The protocol for a REST resource"
  (service-available? [this req])
  (known-method? [this req])
  (uri-too-long? [this req])
  (method-allowed? [this req])
  (malformed? [this req])
  (authorized? [this req])
  (forbidden? [this req])
  (unknown-or-unsupported-header? [this req])
  (unknown-content-type? [this req])
  (request-entity-too-large? [this req])
  (create-response [this req]))

(def default-resource
  {:service-available? (fn [_ _] true)
   :known-method? (fn [_ _] false)
   :uri-too-long? (fn [_ _] false)
   :method-allowed? (fn [_ _] true)
   :malformed? (fn [_ _] false)
   :authorized? (fn [_ _] true)
   :forbidden? (fn [_ _] false)
   :unknown-or-unsupported-header? (fn [_ _] false)
   :unknown-content-type? (fn [_ _] false)
   :request-entity-too-large? (fn [_ _] false)
   })
