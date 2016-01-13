(ns butler.example.worker.core
  (:require [clojure.string :as string]
            [butler.core :as butler]))

(enable-console-print!)

(defn uppercase-handler [str]
  (butler/bring! :print-string (string/upper-case str)))

(defn repeat-handler [{:keys [repeated times]}]
  (butler/bring! :print-string (apply str (repeat times repeated))))

(defn add-handler [{:keys [array-buffer nested-map]}]
  (let [array (js/Float32Array. array-buffer)
        nested (js/Float32Array. (nested-map :nested-buffer))]
    (doseq [i (range (.-length array))]
      (aset array i (+ (aget array i) (aget nested i))))
    (butler/bring! :print-array {:array-buffer (.-buffer array)} [[:array-buffer]])))

(def handlers {:request-uppercase uppercase-handler
               :request-repeat repeat-handler
               :request-add add-handler})

(butler/serve! handlers)
