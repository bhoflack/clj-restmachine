(ns clj-restmachine.routing
  (:require [clj-restmachine.flow :refer [run-flow*]]))

(defn matches-part?
  [part [matcher _]]
  (let [matcher-part (first matcher)]
    (cond
     (instance? String matcher-part) (= matcher-part part)
     (instance? java.util.regex.Pattern matcher-part) (re-matches matcher-part part)
     (nil? matcher-part) false
     :else (throw (Exception. "Invalid handler type")))))

(defn find-matching-route
  [routes uri]
  (loop [path-parts (.split uri "/")
         matching-routes routes]
    (if (empty? path-parts)
      ; Take the first route where the matcher is also empty
      (->> matching-routes
           (filter (fn [[matcher _]] (empty? matcher)))
           (first))
      ; Iterate over the next parth of the path,  keeping the routes that match the current part
      (let [part (first path-parts)]
        (recur (rest path-parts)
               (->> matching-routes
                    (filter (partial matches-part? part))
                    (map (fn [[matcher handler]]
                           [(rest matcher) handler]))))))))

(defn route
  "Route the request to the correct resource."
  ([routes {:keys [uri] :as request} not-found-resource]
   (if-let [resource (find-matching-route routes uri)]
     (run-flow* (second resource) request)
     not-found-resource))
  ([routes request] (route routes request {:status 404 :body "Not found"})))