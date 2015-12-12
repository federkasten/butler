(defproject butler-example "0.1.0-SNAPSHOT"
  :description "An example project for butler"
  :url "https://github.com/federkasten/butler/example"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.189"]
                 [butler "0.2.0-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "1.1.1"]]
  :source-paths ["src/cljs/main" "src/cljs/worker"]
  :clean-targets ^{:protect false} [:target-path
                                    :compile-path
                                    "www/js/butler_example.js"
                                    "www/js/butler_example.js.map"
                                    "www/js/butler_example_dev"
                                    "www/js/butler_example_worker.js"
                                    "www/js/butler_example_worker.js.map"
                                    "www/js/butler_example_worker_dev"
                                    "www/js/butler_example_prod"
                                    "www/js/butler_example_worker_prod"]
  :cljsbuild {:builds
              {:dev
               {:id "dev"
                :source-paths ["src/cljs/main"]
                :jar true
                :compiler {:output-to "www/js/butler_example.js"
                           :output-dir "www/js/butler_example_dev"
                           :source-map "www/js/butler_example.js.map"
                           :optimizations :whitespace
                           :pretty-print true}}
               :dev-worker
               {:id "dev-worker"
                :source-paths ["src/cljs/worker"]
                :jar true
                :compiler {:output-to "www/js/butler_example_worker.js"
                           :output-dir "www/js/butler_example_worker_dev"
                           :source-map "www/js/butler_example_worker.js.map"
                           :optimizations :whitespace
                           :pretty-print true}
                }
               :prod
               {:id "prod"
                :source-paths ["src/cljs/main"]
                :jar true
                :compiler {:output-to "www/js/butler_example.js"
                           :output-dir "www/js/butler_example_prod"
                           :source-map "www/js/butler_example.js.map"
                           :optimizations :advanced
                           :pretty-print false}}
               :prod-worker
               {:id "prod-worker"
                :source-paths ["src/cljs/worker"]
                :jar true
                :compiler {:output-to "www/js/butler_example_worker.js"
                           :output-dir "www/js/butler_example_worker_prod"
                           :source-map "www/js/butler_example_worker.js.map"
                           :optimizations :advanced
                           :pretty-print false}}}})
