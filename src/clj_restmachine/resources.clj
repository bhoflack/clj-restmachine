(ns clj-restmachine.resources
  (:require [ring.util.response :as ring]
            [ring.util.mime-type :as mime]))

(deftype FileResource [path root])
(deftype FilesResource [path root])

(defn file-resource [path root] (FileResource. path root))

(defn files-resource [path root] (FilesResource. path root))

(defn- add-mime-type [response path]
  (if-let [mime-type (mime/ext-mime-type path)]
    (ring/content-type response mime-type)
    response))

(extend FileResource
  clj-restmachine.type/Resource
  (merge clj-restmachine.type/default-resource
         {:known-method? (fn [_ {:keys [request-method]}] (= :get request-method))
          :create-response (fn [this _] (ring/file-response (.path this) {:root (.root this)}))}))

(extend FilesResource
  clj-restmachine.type/Resource
  (merge clj-restmachine.type/default-resource
         {:known-method? (fn [_ {:keys [request-method]}] (= :get request-method))
          :create-response (fn [this {:keys [path-parameters]}]
                             (let [file-path (:* path-parameters)]
                               (when-let [response (ring/file-response file-path {:root (.root this)})]
                                 (add-mime-type response file-path))))}))

