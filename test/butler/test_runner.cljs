(ns butler.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [butler.core-test]
            [butler.usage-test]
            [butler.transit-test]))

(doo-tests 'butler.core-test
           'butler.usage-test
           'butler.transit-test)