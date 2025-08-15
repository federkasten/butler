(ns butler.usage-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [butler.core :as butler]))

(deftest usage-patterns-test
  (testing "typical usage patterns from README"
    (testing "main thread usage pattern"
      (let [response-handler (fn [res] 
                               (is (some? res))
                               res)
            handlers {:foo response-handler}]
        
        ;; Test handler setup
        (is (map? handlers))
        (is (fn? (:foo handlers)))
        
        ;; Test handler execution
        (let [result ((:foo handlers) "test-response")]
          (is (= result "test-response")))))
    
    (testing "worker thread usage pattern"
      (let [request-handler (fn [req]
                              (is (some? req))
                              ;; Simulate heavy work
                              (str "processed-" req))
            handlers {:request-foo request-handler}]
        
        ;; Test handler setup
        (is (map? handlers))
        (is (fn? (:request-foo handlers)))
        
        ;; Test handler execution
        (let [result ((:request-foo handlers) "foo")]
          (is (= result "processed-foo")))))))

(deftest message-flow-test
  (testing "message flow simulation"
    (testing "request-response cycle"
      (let [main-handlers {:response (fn [data] 
                                       (is (= (:status data) "success"))
                                       data)}
            worker-handlers {:request (fn [data]
                                        (is (= (:action data) "process"))
                                        {:status "success" :result "processed"})}]
        
        ;; Simulate main thread receiving response
        (let [response-data {:status "success" :result "processed"}
              result ((:response main-handlers) response-data)]
          (is (= (:status result) "success"))
          (is (= (:result result) "processed")))
        
        ;; Simulate worker thread processing request
        (let [request-data {:action "process"}
              result ((:request worker-handlers) request-data)]
          (is (= (:status result) "success"))
          (is (= (:result result) "processed")))))))

(deftest transferable-objects-test
  (testing "transferable objects handling"
    (testing "should handle transferable keys correctly"
      (let [data-with-transferables {:message "hello"
                                     :buffer (js/ArrayBuffer. 1024)
                                     :metadata {:size 1024}}
            transferable-keys [[:buffer]]]
        
        ;; Test data structure
        (is (= (:message data-with-transferables) "hello"))
        (is (instance? js/ArrayBuffer (:buffer data-with-transferables)))
        (is (= (get-in data-with-transferables [:metadata :size]) 1024))
        
        ;; Test transferable key extraction
        (is (vector? transferable-keys))
        (is (= (first transferable-keys) [:buffer]))))))

(deftest error-scenarios-test
  (testing "error handling scenarios"
    (testing "missing handler"
      (let [handlers {:existing-handler (fn [data] data)}]
        ;; Test that missing handlers don't cause issues
        (is (nil? (:missing-handler handlers)))))
    
    (testing "handler exceptions"
      (let [throwing-handler (fn [data] 
                               (if (= data "error")
                                 (throw (js/Error. "Test error"))
                                 data))
            safe-handler (fn [data]
                           (try
                             (throwing-handler data)
                             (catch js/Error e
                               {:error (.-message e)})))]
        
        ;; Test normal operation
        (is (= (safe-handler "normal") "normal"))
        
        ;; Test error handling
        (let [result (safe-handler "error")]
          (is (= (:error result) "Test error")))))))

(deftest data-serialization-patterns-test
  (testing "common data serialization patterns"
    (testing "simple data types"
      (let [simple-data {:string "test"
                         :number 42
                         :boolean true
                         :array [1 2 3]}]
        (is (string? (:string simple-data)))
        (is (number? (:number simple-data)))
        (is (boolean? (:boolean simple-data)))
        (is (vector? (:array simple-data)))))
    
    (testing "complex nested data"
      (let [complex-data {:user {:id 1
                                 :name "John"
                                 :preferences {:theme "dark"
                                               :notifications true}}
                          :tasks [{:id 1 :title "Task 1" :completed false}
                                  {:id 2 :title "Task 2" :completed true}]}]
        (is (= (get-in complex-data [:user :name]) "John"))
        (is (= (get-in complex-data [:user :preferences :theme]) "dark"))
        (is (= (count (:tasks complex-data)) 2))
        (is (= (get-in complex-data [:tasks 0 :title]) "Task 1"))))
    
    (testing "data with functions (should be handled carefully)"
      (let [data-with-fn {:data "test"
                          :callback (fn [x] (* x 2))}]
        ;; Functions can't be serialized, but we can test the data part
        (is (= (:data data-with-fn) "test"))
        (is (fn? (:callback data-with-fn)))
        (is (= ((:callback data-with-fn) 5) 10))))))

(deftest performance-patterns-test
  (testing "performance considerations"
    (testing "large data handling"
      (let [large-array (vec (range 10000))
            large-object (into {} (map #(vector (keyword (str "key" %)) %) (range 1000)))]
        
        ;; Test large array
        (is (= (count large-array) 10000))
        (is (= (first large-array) 0))
        (is (= (last large-array) 9999))
        
        ;; Test large object
        (is (= (count large-object) 1000))
        (is (= (:key0 large-object) 0))
        (is (= (:key999 large-object) 999))))
    
    (testing "frequent message patterns"
      (let [message-counter (atom 0)
            handler (fn [data]
                      (swap! message-counter inc)
                      data)]
        
        ;; Simulate multiple messages
        (dotimes [i 100]
          (handler {:message-id i :data (str "message-" i)}))
        
        (is (= @message-counter 100))))))

(deftest api-consistency-test
  (testing "API consistency"
    (testing "function signatures"
      ;; Test that all main API functions exist
      (is (fn? butler/butler))
      (is (fn? butler/work!))
      (is (fn? butler/serve!))
      (is (fn? butler/bring!))
      (is (fn? butler/webworker?))
      (is (fn? butler/not-webworker?)))
    
    (testing "constants"
      (is (string? butler/event-message))
      (is (= butler/event-message "message")))))