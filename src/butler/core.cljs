(ns butler.core)

(defn webworker? []
  (undefined? (.-document js/self)))

(def not-webworker? (complement webworker?))

(def event-message "message")

(defn- deserialize
  [data]
  (js->clj (.parse js/JSON data) :keywordize-keys true))

(defn- serialize
  [data]
  (.stringify js/JSON (clj->js data)))

(defn butler
  [script handlers]
  (let [w (new js/Worker script)]
    (.addEventListener w event-message
                       (fn [e]
                         (let [o (deserialize (.-data e))]
                           (when-let [handler (get handlers (:name o) nil)]
                             (handler (:data o))))))
    {:worker w}))

;; for owner

(defn work!
  ([b name data]
   (.postMessage (:worker b)
                 (serialize {:name name
                             :data data})))
  ([b name]
   (work! b name nil)))

;; for butler

(defn serve!
  [handlers]
  (.addEventListener js/self event-message
                     (fn [e]
                       (let [o (deserialize (.-data e))]
                         (when-let [handler (get handlers (:name o) nil)]
                           (handler (:data o)))))))

(defn bring!
  [name data]
  (.postMessage js/self
                (serialize {:name name
                            :data data})))
