(ns jest.function-type-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest func-type-test-1
  (test-code-eval
    "defn square(x) -> Number {x*x}; square(3.0);"
    ['(clojure.core/defn square [x] (* x x)) '(square 3.0)]
    9.0))
