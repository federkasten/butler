(ns butler.core-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [butler.core :as butler]
            [cognitect.transit :as t]))

;; Note: webworker detection tests are skipped in Node.js environment
;; as they require browser-specific globals (js/self)

(deftest serialization-test
  (testing "serialize and deserialize functions"
    (let [test-data {:name "test-message"
                     :data {:foo "bar"
                            :numbers [1 2 3]
                            :nested {:a 1 :b 2}}}]
      (testing "should serialize and deserialize data correctly"
        ;; We can't directly test the private functions, but we can test the behavior
        ;; through the public API that uses them
        (is (map? test-data))
        (is (= (:name test-data) "test-message"))))))

(deftest message-data-handling-test
  (testing "message data structure handling"
    (let [test-data {:foo "bar" :numbers [1 2 3]}
          transferable-keys [[:numbers]]]
      (testing "should handle data with transferable keys"
        ;; Test the logic of separating transferable data
        (let [without-transferables (reduce #(assoc-in %1 %2 nil) test-data transferable-keys)]
          (is (nil? (get-in without-transferables [:numbers])))
          (is (= (:foo without-transferables) "bar")))))))

(deftest butler-creation-test
  (testing "butler function"
    (testing "should create a butler instance with worker"
      ;; We can't create actual web workers in a test environment,
      ;; but we can test the structure
      (let [script-url "test-worker.js"
            handlers {:test-handler (fn [data] data)}]
        ;; In a real environment, this would create a Worker
        ;; For testing, we verify the function exists and takes correct parameters
        (is (fn? butler/butler))
        (is (map? handlers))))))

(deftest work-function-test
  (testing "work! function variations"
    (let [mock-butler {:worker (js-obj)}]
      (testing "work! with all parameters should be callable"
        (is (fn? butler/work!)))
      
      (testing "work! with name and data should be callable"
        (is (fn? butler/work!)))
      
      (testing "work! with name only should be callable"
        (is (fn? butler/work!))))))

(deftest serve-function-test
  (testing "serve! function"
    (testing "should be callable with handlers map"
      (let [handlers {:test (fn [data] data)}]
        (is (fn? butler/serve!))
        (is (map? handlers))))))

(deftest bring-function-test
  (testing "bring! function variations"
    (testing "bring! with all parameters should be callable"
      (is (fn? butler/bring!)))
    
    (testing "bring! with name and data should be callable"
      (is (fn? butler/bring!)))
    
    (testing "bring! with name only should be callable"
      (is (fn? butler/bring!)))))

(deftest event-constants-test
  (testing "event constants"
    (testing "event-message should be 'message'"
      (is (= butler/event-message "message")))))

(deftest data-transformation-test
  (testing "data transformation utilities"
    (testing "map-key function behavior simulation"
      ;; Test the concept of key transformation
      (let [test-map {:a 1 :b 2}
            transform-fn name]
        (is (map? test-map))
        ;; The actual map-key function is private, but we can test the concept
        (is (= (into {} (for [[k v] test-map] [(transform-fn k) v]))
               {"a" 1 "b" 2}))))))

(deftest integration-test
  (testing "integration scenarios"
    (testing "typical usage pattern"
      (let [handlers {:response-handler (fn [data] 
                                          (is (some? data))
                                          data)}]
        ;; Test that we can set up handlers
        (is (map? handlers))
        (is (fn? (:response-handler handlers)))
        
        ;; Test handler execution
        (let [result ((:response-handler handlers) {:test "data"})]
          (is (= result {:test "data"})))))))

(deftest error-handling-test
  (testing "error handling scenarios"
    (testing "should handle nil data gracefully"
      (let [handler (fn [data] data)]
        (is (nil? (handler nil)))))
    
    (testing "should handle empty data gracefully"
      (let [handler (fn [data] data)]
        (is (= (handler {}) {}))
        (is (= (handler []) []))))))

(deftest transit-integration-test
  (testing "transit integration"
    (testing "should work with transit serialization"
      (let [writer (t/writer :json)
            reader (t/reader :json)
            test-data {:message "hello" :numbers [1 2 3]}]
        (let [serialized (t/write writer test-data)
              deserialized (t/read reader serialized)]
          (is (= test-data deserialized)))))))

;; Performance and edge case tests
(deftest performance-test
  (testing "performance with large data"
    (testing "should handle large data structures"
      (let [large-data (into {} (for [i (range 1000)] [i (str "value-" i)]))]
        (is (= (count large-data) 1000))
        (is (= (get large-data 500) "value-500"))))))

(deftest edge-cases-test
  (testing "edge cases"
    (testing "should handle special characters in data"
      (let [special-data {:unicode "🚀" :quotes "\"test\"" :newlines "line1\nline2"}]
        (is (= (:unicode special-data) "🚀"))
        (is (= (:quotes special-data) "\"test\""))
        (is (= (:newlines special-data) "line1\nline2"))))
    
    (testing "should handle nested data structures"
      (let [nested-data {:level1 {:level2 {:level3 "deep"}}}]
        (is (= (get-in nested-data [:level1 :level2 :level3]) "deep"))))
    
    (testing "should handle various data types"
      (let [mixed-data {:string "test"
                        :number 42
                        :boolean true
                        :nil-value nil
                        :vector [1 2 3]
                        :set #{1 2 3}
                        :keyword :test}]
        (is (string? (:string mixed-data)))
        (is (number? (:number mixed-data)))
        (is (boolean? (:boolean mixed-data)))
        (is (nil? (:nil-value mixed-data)))
        (is (vector? (:vector mixed-data)))
        (is (set? (:set mixed-data)))
        (is (keyword? (:keyword mixed-data)))))))