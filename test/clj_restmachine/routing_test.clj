(ns clj-restmachine.routing-test
  (:use clojure.test)
  (:require clj-restmachine.type
            [clj-restmachine.routing :refer [route]]))

; The resources
(deftype UserResource [])
(deftype ProductResource [])
(deftype HelloResource [])

(extend UserResource
  clj-restmachine.type/Resource
  (merge clj-restmachine.type/default-resource
         {:known-method? (fn [_ {:keys [request-method]}] (= :get request-method))
          :unknown-content-type? (fn [_ {:keys [headers]}] (-> (get headers "Request-Type") (not= "application/edn")))
          :create-response (fn [_ {:keys [path-parameters]}] {:status 200
                                                              :body (pr-str {:name (:name path-parameters)})})}))

(extend ProductResource
  clj-restmachine.type/Resource
  (merge clj-restmachine.type/default-resource
         {:known-method? (fn [_ {:keys [request-method]}] (= :get request-method))
          :unknown-content-type? (fn [_ {:keys [headers]}] (not= "application/edn" (get headers "Request-Type")))
          :create-response (fn [_ req] {:status 200
                                        :body (pr-str [{:product :a} {:product :b}])})}))


(extend HelloResource
  clj-restmachine.type/Resource
  (merge clj-restmachine.type/default-resource
         {:known-method? (fn [_ {:keys [request-method]}] (= :get request-method))
          :create-response (fn [_ req] {:status 200
                                        :body "hello world"})}))

(def user-resource (UserResource.))
(def product-resource (ProductResource.))
(def hello-resource (HelloResource.))

(def routes
  (list [["users" :name] {:name #"\w+"} user-resource]
        [["users" :name "products"] {:name #"\w+"} product-resource]
        [["hello" :name] [] hello-resource]))

(deftest route-test
  (let [request {:uri "users/brecht"
                 :request-method :get
                 :headers {"Request-Type" "application/edn"}}
        response (route routes request)]
    (is (= 200 (:status response)))
    (is (= "{:name \"brecht\"}" (:body response)))))

(deftest match-everything-test
  (let [request {:uri "hello/brecht"
                 :request-method :get}
        response (route routes request)]
    (is (= 200 (:status response)))
    (is (= "hello world" (:body response)))))

(deftest product-test
  (let [request {:uri "users/krusty/products"
                 :request-method :get
                 :headers {"Request-Type" "application/edn"}}
        response (route routes request)]
    (is (= 200 (:status response)))
    (is (= "[{:product :a} {:product :b}]" (:body response)))))

(deftest part-of-a-route
  (let [request {:uri "users"}
        response (route routes request)]
    (is (= 404 (:status response)))))

(deftest not-found
  (let [request {:uri "users/brecht/blaat"
                 :request-method :get
                 }
        response (route routes request)]
    (is (= 404 (:status response)))))
