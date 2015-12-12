An example project for butler

# How to run

To run this example, do the followings and access to http://localhost:8000.

```shell
lein cljsbuild once dev dev-worker
(cd www && python -m SimpleHTTPServer)
```
