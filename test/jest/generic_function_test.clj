(ns jest.generic-function-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest func-test-0
  (test-code
    "defn <T> sum(x: T, y: T) {x+y};"
    ['(clojure.core/defn sum [x y] (+ x y))]))
