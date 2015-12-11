(ns butler.core
  (:require [cognitect.transit :as t]))

(defn webworker? []
  (undefined? (.-document js/self)))

(def not-webworker? (complement webworker?))

(def event-message "message")

(defn- deserialize
  [data]
  (let [r (t/reader :json)]
    (t/read r data)))

(defn- serialize
  [data]
  (let [w (t/writer :json)]
    (t/write w data)))

(defn- map-key [f m]
  (into {} (for [[k v] m] [(f k) v])))

(defn- message-handler [handlers e]
  (let [o (deserialize (.-serialized (.-data e)))
        transferables (map-key deserialize (js->clj (.-transferables (.-data e))))]
    (when-let [handler (get handlers (keyword (:name o)) nil)]
      (if (< 0 (count transferables))
        (handler (:data o) transferables)
        (handler (:data o))))))

(defn- post-message! [worker name copying-data transferable-map]
  (let [serialized-data (serialize {:name name :data copying-data})
        serialized-map (map-key serialize transferable-map)
        copying (clj->js {"serialized" serialized-data
                          "transferables" serialized-map})]
    (.postMessage worker copying (clj->js (vals transferable-map)))))

(defn butler
  [script handlers]
  (let [w (new js/Worker script)]
    (.addEventListener w event-message (partial message-handler handlers))
    {:worker w}))

;; for owner

(defn work!
  ([b name copying-data transferable-map]
   (post-message! (:worker b) name copying-data transferable-map))
  ([b name copying-data]
   (work! b name copying-data {}))
  ([b name]
   (work! b name nil)))

;; for butler

(defn serve!
  [handlers]
  (.addEventListener js/self event-message (partial message-handler handlers)))

(defn bring!
  ([name copying-data transferable-map]
   (post-message! js/self name copying-data transferable-map))
  ([name copying-data]
   (bring! name copying-data {})))

