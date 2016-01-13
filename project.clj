(defproject butler "0.2.0"
  :description "ClojureScript library for Web Workers"
  :url "https://github.com/federkasten/butler"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[com.cognitect/transit-cljs "0.8.237"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.7.0"]
                                  [org.clojure/clojurescript "1.7.228"]]
                   :plugins [[lein-cljsbuild "1.1.1"]]}}
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
                           :pretty-print true}}}}
  :signing {:gpg-key "me@tak.sh"})
