(ns butler.example.worker.core
  (:require [clojure.string :as string]
            [butler.core :as butler]))

(enable-console-print!)

(defn uppercase-handler [str]
  (butler/bring! :print-str (string/upper-case str)))

(defn repeat-handler [{:keys [repeated times]}]
  (butler/bring! :print-str (apply str (repeat times repeated))))

(defn increment-handler [_ {:keys [array-buffer]}]
  (let [arr (js/Float32Array. array-buffer)]
    (doseq [i (range (.-length arr))]
      (aset arr i (inc (aget arr i))))
    (butler/bring! :print-arr nil {:array-buffer (.-buffer arr)})))

(def handlers {:request-uppercase uppercase-handler
               :request-repeat repeat-handler
               :request-increment increment-handler})

(butler/serve! handlers)

