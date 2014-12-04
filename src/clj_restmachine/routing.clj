(ns clj-restmachine.routing
  (:require [clj-restmachine.flow :refer [run-flow*]]))

(defn ^:private matches-part?
  [part matcher-part regexes ctx]
  (cond
   (and (= "" part) (nil? matcher-part)) [true ctx]
   (instance? String matcher-part) [(= matcher-part part) ctx] 
   (instance? clojure.lang.Keyword matcher-part) (if-let [r (get regexes matcher-part)]
                                        ; verify if the regex passes if we have a regex
                                                   (if-let [matching (re-matches r part)]
                                                     [true (assoc ctx matcher-part matching)]
                                                     [false ctx])
                                        ; always pass if there's no regex
                                                   [true (assoc ctx matcher-part part)])
   (nil? matcher-part) [false ctx]
   :else (throw (Exception. (str "Invalid handler type: " matcher-part)))))

(defn filter-routes
  [part routes path-parts]
  (loop [routes* routes
         matching-routes []]
    (if (empty? routes*)
      matching-routes
      (let [[matcher regexes handler ctx] (first routes*)
            remaining-matchers (rest matcher)
            matcher-part (first matcher)]
        (if (= :* matcher-part)
          (let [ctx* (assoc ctx :* (clojure.string/join "/" path-parts))]
            [[remaining-matchers regexes handler ctx* :finished]])
          (let [[r ctx*] (matches-part? part matcher-part regexes ctx)]
            (if r
              (recur (rest routes*) (conj matching-routes [remaining-matchers regexes handler ctx*]))
              (recur (rest routes*) matching-routes))))))))

(defn find-matching-route
  [routes {:keys [uri] :as request}]
  (loop [path-parts (.split (.substring uri 1) "/")
         matching-routes (map (fn [[m r res]] [m r res {}]) routes)]
    (let [first-route (first matching-routes)]
      (if (and (= 5 (count first-route))
               (= :finished (nth first-route 4)))
                                        ; Finish as soon you find a wildcard route
        (->> first-route
             (take 4)
             (into []))

        (if (empty? path-parts)
                                        ; Take the first route where the matcher is also empty
          (->> matching-routes
               (filter (fn [[matcher _ _ _]] (empty? matcher)))
               (first))
                                        ; Iterate over the next parth of the path,  keeping the routes that match the current part
          (let [part (first path-parts)]
            (recur (rest path-parts)
                   (filter-routes part matching-routes path-parts))))))))

(defn route
  "Route the request to the correct resource.

   Routes is a seq of a seq containing the matcher as the first part,
   the regular expressions for the matcher in the second argument and 
   the related handler as the third argument.

   You can create matchers for strings (where we compare the given part
   of the path to the string) and regexes (where we match the part of the 
   path to the regex).


   If the route contains keywords,  then the keyword and the related
   value will be available in the :path-parameters in the request.

   Example:

   (route 
     (list [[\"users\" :name] {:name #\"\\w+\"} user-resource)
           [[\"users\" :name \"products\"] {:name #\"\\w+\"} product-resource]))

   This will create a routing table that matches:
     - users/krusty to the user-resource
     - users/krusty/products to the product-resource"
  ([routes not-found-resource request]
   (let [[_ _ resource ctx] (find-matching-route routes request)
         request* (assoc request :path-parameters ctx)]
     (if resource 
       (run-flow* resource request*)
       not-found-resource)))
  ([routes request] (route routes {:status 404 :body "Not found"} request)))
