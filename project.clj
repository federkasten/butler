(defproject butler "0.2.1-SNAPSHOT"
  :description "Bring Web Workers to ClojureScript"
  :url "https://github.com/federkasten/butler"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[com.cognitect/transit-cljs "0.8.280"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.12.1"]
                                  [org.clojure/clojurescript "1.11.132"]
                                  [doo "0.1.11"]]
                   :plugins [[lein-cljsbuild "1.1.8"]
                             [lein-doo "0.1.11"]]}}
  :source-paths ["src"]
  :cljsbuild {:builds
              {:dev
               {:id "dev"
                :source-paths ["src"]
                :jar true
                :compiler {:output-to "target/gen/butler_dev.js"
                           :optimizations :whitespace
                           :pretty-print true}}
               :prod
               {:id "prod"
                :source-paths ["src"]
                :jar true
                :compiler {:output-to "target/gen/butler.js"
                           :optimizations :advanced
                           :pretty-print true}}
               :test
               {:id "test"
                :source-paths ["src" "test"]
                :compiler {:output-to "target/test/butler_test.js"
                           :output-dir "target/test"
                           :main butler.test-runner
                           :target :nodejs
                           :optimizations :none}}}}
  :doo {:build "test"
        :alias {:default [:node]}}
  :signing {:gpg-key "me@tak.sh"})
