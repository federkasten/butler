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
  (let [deserialized (deserialize (aget (.-data e) "serialized"))
        name (keyword (:name deserialized))
        data (:data deserialized)
        transferables (map-key deserialize (js->clj (aget (.-data e) "transferables")))
        copied (reduce #(assoc-in %1 (first %2) (second %2)) data transferables)]
    (when-let [handler (get handlers name)]
      (handler copied))))

(defn- post-message! [worker name copying-data transferable-keys]
  (let [without-transferables (reduce #(assoc-in %1 %2 nil) copying-data transferable-keys)
        transferables (into {} (map (fn [key] [(serialize key) (get-in copying-data key)]) transferable-keys))
        serialized (serialize {:name name :data without-transferables})
        copying (clj->js {"serialized" serialized
                          "transferables" transferables})]
    (.postMessage worker copying (clj->js (or (vals transferables) [])))))

(defn butler
  [script handlers]
  (let [w (new js/Worker script)]
    (.addEventListener w event-message (partial message-handler handlers))
    {:worker w}))

;; for owner

(defn work!
  ([b name copying-data transferable-keys]
   (post-message! (:worker b) name copying-data transferable-keys))
  ([b name copying-data]
   (work! b name copying-data []))
  ([b name]
   (work! b name nil)))

;; for butler

(defn serve!
  [handlers]
  (.addEventListener js/self event-message (partial message-handler handlers)))

(defn bring!
  ([name copying-data transferable-keys]
   (post-message! js/self name copying-data transferable-keys))
  ([name copying-data]
   (bring! name copying-data []))
  ([name]
   (bring! name nil)))
