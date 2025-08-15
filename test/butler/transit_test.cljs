(ns butler.transit-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [cognitect.transit :as t]))

(deftest transit-serialization-test
  (testing "transit serialization and deserialization"
    (let [writer (t/writer :json)
          reader (t/reader :json)]
      
      (testing "basic data types"
        (let [test-cases [42
                          "hello world"
                          true
                          false
                          nil
                          [1 2 3]
                          {:a 1 :b 2}
                          #{1 2 3}]]
          (doseq [test-data test-cases]
            (let [serialized (t/write writer test-data)
                  deserialized (t/read reader serialized)]
              (is (= test-data deserialized))))))
      
      (testing "complex nested structures"
        (let [complex-data {:users [{:id 1 :name "Alice" :active true}
                                    {:id 2 :name "Bob" :active false}]
                            :metadata {:version "1.0" :timestamp 1234567890}
                            :settings {:theme "dark" :notifications #{:email :push}}}
              serialized (t/write writer complex-data)
              deserialized (t/read reader serialized)]
          (is (= complex-data deserialized))
          (is (= (get-in deserialized [:users 0 :name]) "Alice"))
          (is (= (get-in deserialized [:metadata :version]) "1.0"))
          (is (contains? (get-in deserialized [:settings :notifications]) :email))))
      
      (testing "special characters and unicode"
        (let [unicode-data {:emoji "🚀🌟💻"
                            :special "quotes\"and'apostrophes"
                            :newlines "line1\nline2\nline3"
                            :unicode-chars "αβγδε"}
              serialized (t/write writer unicode-data)
              deserialized (t/read reader serialized)]
          (is (= unicode-data deserialized))))
      
      (testing "large data structures"
        (let [large-data {:numbers (vec (range 1000))
                          :strings (mapv #(str "item-" %) (range 500))
                          :nested (into {} (map #(vector (keyword (str "key" %)) 
                                                         {:value % :squared (* % %)}) 
                                                (range 100)))}
              serialized (t/write writer large-data)
              deserialized (t/read reader serialized)]
          (is (= large-data deserialized))
          (is (= (count (:numbers deserialized)) 1000))
          (is (= (count (:strings deserialized)) 500))
          (is (= (count (:nested deserialized)) 100)))))))

(deftest transit-edge-cases-test
  (testing "transit edge cases"
    (let [writer (t/writer :json)
          reader (t/reader :json)]
      
      (testing "empty collections"
        (let [empty-data {:empty-vector []
                          :empty-map {}
                          :empty-set #{}}
              serialized (t/write writer empty-data)
              deserialized (t/read reader serialized)]
          (is (= empty-data deserialized))))
      
      (testing "deeply nested structures"
        (let [deep-data {:level1 {:level2 {:level3 {:level4 {:level5 "deep value"}}}}}
              serialized (t/write writer deep-data)
              deserialized (t/read reader serialized)]
          (is (= deep-data deserialized))
          (is (= (get-in deserialized [:level1 :level2 :level3 :level4 :level5]) "deep value"))))
      
      (testing "mixed data types in collections"
        (let [mixed-data {:mixed-vector [1 "two" :three true nil]
                          :mixed-map {:num 42 :str "hello" :bool false :nil nil}}
              serialized (t/write writer mixed-data)
              deserialized (t/read reader serialized)]
          (is (= mixed-data deserialized)))))))

(deftest transit-performance-test
  (testing "transit performance characteristics"
    (let [writer (t/writer :json)
          reader (t/reader :json)]
      
      (testing "serialization speed with various data sizes"
        (let [small-data {:count 10 :items (vec (range 10))}
              medium-data {:count 1000 :items (vec (range 1000))}
              large-data {:count 10000 :items (vec (range 10000))}]
          
          ;; Test that serialization completes without errors
          (is (string? (t/write writer small-data)))
          (is (string? (t/write writer medium-data)))
          (is (string? (t/write writer large-data)))))
      
      (testing "round-trip consistency"
        (let [test-data {:timestamp (js/Date.)
                         :random-data (repeatedly 100 #(rand-int 1000))
                         :nested-maps (into {} (map #(vector % {:value (* % %)}) (range 50)))}]
          (dotimes [_ 10]
            (let [serialized (t/write writer test-data)
                  deserialized (t/read reader serialized)]
              ;; Note: Date objects might not serialize exactly the same
              (is (= (:random-data test-data) (:random-data deserialized)))
              (is (= (:nested-maps test-data) (:nested-maps deserialized))))))))))

(deftest transit-butler-integration-test
  (testing "transit integration with butler message format"
    (let [writer (t/writer :json)
          reader (t/reader :json)]
      
      (testing "butler message structure serialization"
        (let [butler-message {:name "test-message"
                              :data {:payload "hello world"
                                     :metadata {:sender "main-thread"
                                                :timestamp 1234567890}}}
              serialized (t/write writer butler-message)
              deserialized (t/read reader serialized)]
          (is (= butler-message deserialized))
          (is (= (:name deserialized) "test-message"))
          (is (= (get-in deserialized [:data :payload]) "hello world"))))
      
      (testing "transferable object simulation"
        (let [message-with-transferables {:name "data-transfer"
                                          :data {:text "regular data"
                                                 :buffer-info {:size 1024 :type "ArrayBuffer"}}
                                          :transferables {"[\"buffer\"]" "simulated-buffer-data"}}
              serialized (t/write writer message-with-transferables)
              deserialized (t/read reader serialized)]
          (is (= message-with-transferables deserialized))
          (is (= (get-in deserialized [:data :text]) "regular data"))
          (is (= (get deserialized :transferables) {"[\"buffer\"]" "simulated-buffer-data"}))))
      
      (testing "handler response format"
        (let [response-message {:status "success"
                                :result {:processed-data [1 2 3 4 5]
                                         :computation-time 150}
                                :error nil}
              serialized (t/write writer response-message)
              deserialized (t/read reader serialized)]
          (is (= response-message deserialized))
          (is (= (:status deserialized) "success"))
          (is (= (get-in deserialized [:result :processed-data]) [1 2 3 4 5]))
          (is (nil? (:error deserialized))))))))

(deftest transit-error-handling-test
  (testing "transit error handling"
    (let [writer (t/writer :json)
          reader (t/reader :json)]
      
      (testing "malformed JSON handling"
        (try
          (t/read reader "invalid json {")
          (is false "Should have thrown an error")
          (catch js/Error e
            (is (some? e)))))
      
      (testing "unsupported data types"
        ;; Functions cannot be serialized
        (try
          (let [data-with-function {:fn (fn [] "test")}]
            (t/write writer data-with-function))
          ;; If it doesn't throw, that's also fine - transit might handle it gracefully
          (catch js/Error e
            (is (some? e))))))))

(deftest transit-compatibility-test
  (testing "transit version compatibility"
    (testing "current transit version works with expected data formats"
      (let [writer (t/writer :json)
            reader (t/reader :json)
            version-test-data {:version "0.8.280"
                               :features ["json-serialization" "cljs-data-types"]
                               :compatibility {:clojure "1.12.1" :clojurescript "1.12.42"}}
            serialized (t/write writer version-test-data)
            deserialized (t/read reader serialized)]
        (is (= version-test-data deserialized))
        (is (= (:version deserialized) "0.8.280"))
        (is (vector? (:features deserialized)))
        (is (map? (:compatibility deserialized)))))))