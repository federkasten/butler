(ns butler
  (:require [cljs.reader :refer [read-string]]))

(defn webworker? []
  (undefined? (.-document js/self)))

(def not-webworker? (complement webworker?))

(defn butler
  [script handlers]
  (let [w (new js/Worker script)]
    (.addEventListener w "message" (fn [e]
                                     (let [o (read-string (.-data e))
                                           handler (get handlers (:name o))]
                                       (when-not (nil? handler)
                                         (handler (:data o))))))
    {:worker w}))

;; for owner

(defn work!
  ([b name data]
     (.postMessage (:worker b)
                   (pr-str {:name name
                            :data data})))
  ([b name]
     (work! b name nil)))

;; for butler

(defn serve!
  [handlers]
  (.addEventListener js/self "message" (fn [e]
                                         (let [o (read-string (.-data e))
                                               handler (get handlers (:name o))]
                                           (when-not (nil? handler)
                                             (handler (:data o)))))))

(defn bring!
  [name data]
  (.postMessage js/self
                (pr-str {:name name
                         :data data})))
