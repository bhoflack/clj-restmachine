(ns clj-restmachine.flow-test
  (:use clojure.test)
  (:require clj-restmachine.type
            clj-restmachine.flow))

(deftype UserResource [])

(extend UserResource
  clj-restmachine.type/Resource
  (merge clj-restmachine.type/default-resource
         {:known-method? (fn [_ req] (= :get (:request-method req)))
          :create-response (fn [_ req] {:status 200
                                        :body "hello world"
                                        })
          }))

(deftype ProtectedResource [])

(extend ProtectedResource
  clj-restmachine.type/Resource
  (merge clj-restmachine.type/default-resource
         {:known-method? (fn [_ _] true)
          :authorized? (fn [_ {:keys [headers]}]
                         (contains? headers :Authorization))
          :create-response (fn [_ req] {:status 200
                                        :body "hello world"
                                        })}))

(deftype NotExistResource [])

(extend NotExistResource
  clj-restmachine.type/Resource
  (merge clj-restmachine.type/default-resource
         {:known-method? (fn [_ _] true)
          :resource-exists? (fn [_ _] false)
          :create-response (fn [_ req] {:status 200
                                        :body "hello world"
                                        })}))

(deftest runflow-test
  (let [req {:request-method :get}
        resource (UserResource.)
        [flow response] (clj-restmachine.flow/run-flow resource req)]
    (is (= [:b13 :b12 :b11 :b10 :b9 :b8 :b7 :b6 :b5 :b4 :b3] flow))
    (is (= {:status 200 :body "hello world"}
           response))))

(deftest unknown-method-test
  (let [req {:request-method :put}
        resource (UserResource.)
        [flow response] (clj-restmachine.flow/run-flow resource req)]
    (is (= [:b13] flow))
    (is (= {:status 501 :body "Not Implemented"}))))

(deftest authorize-test
  (let [req {:request-method :put
             :headers []}
        resource (ProtectedResource.)
        [flow response] (clj-restmachine.flow/run-flow resource req)]
    (is (= [:b13 :b12 :b11 :b10 :b9] flow))
    (is (= {:status 401 :body "Unauthorized"}))))

(deftest exists-test
  (let [req {:request-method :get
             :headers []}
        resource (NotExistResource.)
        [flow response] (clj-restmachine.flow/run-flow resource req)]
    (is (= [:b13 :b12 :b11 :b10 :b9 :b8 :b7 :b6 :b5 :b4] flow))
    (is (= {:status 404 :body "Resource not found"}))))
