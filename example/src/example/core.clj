(ns example.core
  (:require clj-restmachine.type
            clj-restmachine.routing
            [ring.adapter.jetty :refer [run-jetty]]))

(deftype PingResource [])
(deftype UserResource [])
(deftype ProductResource [])

(extend PingResource
  clj-restmachine.type/Resource
  (merge clj-restmachine.type/default-resource
         {:known-method? (fn [_ {:keys [request-method]}] (= :get request-method))          
          :create-response (fn [_ _] {:status 200 :body "ping"})}))

(extend UserResource
  clj-restmachine.type/Resource
  (merge clj-restmachine.type/default-resource
         {:known-method? (fn [_ {:keys [request-method]}] (= :get request-method))          
          :create-response (fn [_ {:keys [path-parameters]}] {:status 200
                                                              :body (str "Hi, " (:name path-parameters))})}))

(extend ProductResource
  clj-restmachine.type/Resource
  (merge clj-restmachine.type/default-resource
         {:known-method? (fn [_ {:keys [request-method]}] (= :get request-method))
          :create-response (fn [_ {:keys [path-parameters]}] {:status 200
                                                              :body (str "Hi, " (:name path-parameters) " you selected product " (:product-id path-parameters))})}))


(def app
  (partial clj-restmachine.routing/route
           (list [["_ping"] [] (PingResource.)]
                 [["users" :name] {:name #"\w+"} (UserResource.)]
                 [["users" :name "products" :product-id] {:name #"\w+" :product-id #"\d+"} (ProductResource.)])))

(defn -main []
  (run-jetty app {:port 8080 :join? false}))
