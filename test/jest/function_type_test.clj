(ns jest.function-type-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]
            [jest.parser :refer :all])
  (:import (jest Exception$FunctionParameterTypeMismatch)))


(deftest func-type-test-1
  (test-code-eval
    "defn square(x: Number) -> Number {x*x}; square(3.0);"
    ['(clojure.core/defn square [x] (* x x)) '(square 3.0)]
    9.0))


(deftest func-type-mismatch-1
  (test-code-exception
    "defn square(x: Number) -> Number {x*x}; square(\"three\");"
    Exception$FunctionParameterTypeMismatch))

