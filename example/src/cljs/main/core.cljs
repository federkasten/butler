(ns butler.example.main.core
  (:require [butler.core :as butler]))

(enable-console-print!)

(def ^:const worker-script-path "js/butler_example_worker.js")

(defn print-str-handler [str]
  (println str))

(defn print-arr-handler [_ {:keys [array-buffer]}]
  (println (js/Float32Array. array-buffer)))

;; Main thread handlers definition.
(def handlers {:print-str print-str-handler
                   :print-arr print-arr-handler})

;; Create a web worker with script path and handlers.
(def example-butler (butler/butler worker-script-path handlers))

;; To specify worker-side handler, give a key as second argument.
;; Third argument is copied and passed as an argument to worker-side handler.
(butler/work! example-butler :request-uppercase "foo")

;; We can use standard clojurescript object as argument.
(butler/work! example-butler :request-repeat {:repeated "bar" :times 5})

;; To use zero-copy transfering, give a map as fourth argument.
;; Its vals must be 'Transferable' object.
(butler/work! example-butler :request-increment nil
              {:array-buffer (.-buffer (js/Float32Array. #js [1 2 3 4 5 6]))})

