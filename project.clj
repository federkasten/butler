(defproject butler "0.1.0-SNAPSHOT"
  :description "A ClojureScript library for Web Workers"
  :url "https://github.com/federkasten/butler"
  :plugins [[lein-cljsbuild "1.0.3"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.6.0"]
                                  [org.clojure/clojurescript "0.0-2227"]]}}
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
