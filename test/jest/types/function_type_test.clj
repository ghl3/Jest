(ns jest.types.function-type-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]
            [jest.parser :refer :all])
  (:import (jest.compiler Exceptions$FunctionParameterTypeMismatch)))


(deftest func-type-test-1
  (test-code-eval
    "defn square(x: Number) -> Number {x*x}; square(3.0);"
    ['(clojure.core/defn square [x] (* x x)) '(square 3.0)]
    9.0))


(deftest func-type-test-2
  (test-code-valid
    "def myList = range(0, 100, 10);
    def incremented = map(inc, myList);
    defn square(x: Number) -> Number {
         x*x;}
    def squared = map(square, incremented);
    println(squared);
    def halfRange = (squared.first() + squared.last()) / 2;
    println(halfRange);"))


(deftest func-type-mismatch-1
  (test-code-exception
    "defn square(x: Number) -> Number {x*x}; square(\"three\");"
    Exceptions$FunctionParameterTypeMismatch))

