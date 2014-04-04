(defproject butler "0.1.0-SNAPSHOT"
  :description "A minimal ClojureScript library for Web Workers"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]]
  :plugins [[lein-cljsbuild "1.0.3"]]
  :profiles {:dev {:plugins [[com.cemerick/austin "0.1.4"]]}}
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
                           :pretty-print true}}}})
