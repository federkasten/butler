# butler

Bring Web Workers to ClojureScript

- Simple API
- Suitable for Clojure data structure
- Support transferable objects

## Usage

Add the following dependency to your `project.clj`:

```
[butler "0.2.0"]
```

### Example

Create web workers and define its response handlers in main thread, and tell to workers via `work!`;

```clj
(require '[butler.core :as butler])

(enable-console-print!)

;; Create a web worker with script and handlers.
(def example-butler (butler/butler "path/to/worker.js" {:foo (fn [res]
                                                               (println res))}))

(butler/work! example-butler :request-foo "foo")
```

In worker threads, define handlers of requests using `serve!` and send results to the main thread using `bring`;

```clj
(require '[butler.core :as butler])

(defn run-foo [req]
  ;; some heavy heavy jobs
  (butler/bring! :foo "hello"))

(def handlers {:request-foo run-foo})

(butler/serve! handlers)
```

See [example](https://github.com/federkasten/butler/tree/master/example) for more details.

## License

Copyright [Takashi AOKI][tak.sh] and other contributors.

Licensed under the [Apache License, Version 2.0][apache-license-2.0].

[tak.sh]: http://tak.sh
[apache-license-2.0]: http://www.apache.org/licenses/LICENSE-2.0.html
