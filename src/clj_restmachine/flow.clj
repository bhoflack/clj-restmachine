(ns clj-restmachine.flow
  (:use [clj-restmachine.type]))

(defn generate-response
  [data status]
  {:status status
   :body data
   })

(defn b4
  [r req flow]
  (if (request-entity-too-large? r req)
    [flow (generate-response "Request Entity Too Large" 413)]
    [(conj flow :b4) (create-response r req)]))

(defn b5
  [r req flow]
  (if (unknown-content-type? r req)
    [flow (generate-response "Unsupported Media Type" 415)]
    (b4 r req (conj flow :b5))))

(defn b6
  [r req flow]
  (if (unknown-or-unsupported-header? r req)
    [flow (generate-response "Not implemented" 501)]
    (b5 r req (conj flow :b6))))

(defn b7
  [r req flow]
  (if (forbidden? r req)
    [flow (generate-response "Forbidden" 403)]
    (b6 r req (conj flow :b7))))

(defn b8
  [r req flow]
  (if (authorized? r req)
    (b7 r req (conj flow :b8))
    [flow (generate-response "Unauthorized" 401)]))

(defn b9
  [r req flow]
  (if (malformed? r req)
    [flow (generate-response "Bad request" 400)]
    (b8 r req (conj flow :b9))))

(defn b10
  [r req flow]
  (if (method-allowed? r req)
    (b9 r req (conj flow :b10))
    [flow (generate-response "Method not allowed" 405)]))

(defn b11
  [r req flow]
  (if (uri-too-long? r req)
    [flow (generate-response "Request URI Too Long" 414)]
    (b10 r req (conj flow :b11))))

(defn b12
  [r req flow]
  (if (known-method? r req)
    (b11 r req (conj flow :b12))
    [flow (generate-response "Not Implemented" 501)]))

(defn b13
  [r req flow]
  (if (service-available? r req)
    (b12 r req (conj flow :b13))
    [flow (generate-response "Service Unavailable" 503)]))

(defn run-flow
  "Run the rest machine flow,  keep track of the decisions taken."
  [r req]
  (b13 r req []))

(defn run-flow*
  "Run the rest machine flow,  without keeping track of the decisions taken."
  [r req]
  (-> (run-flow r req)
      (second)))
