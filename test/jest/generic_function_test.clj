(ns jest.generic-function-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all])
  (:import (jest Exception$InconsistentGenericTypes)))

(deftest func-test-0
  (test-code
    "defn <T> sum(x: T, y: T) {x+y};"
    ['(clojure.core/defn sum [x y] (+ x y))]))


(deftest func-test-1
  (test-code
    "defn <T> addNumbers(x: T, y: T) -> Number {x+y}; addNumbers(3, 4);"
    ['(clojure.core/defn addNumbers [x y] (+ x y)) '(addNumbers 3 4)]
    true))


(deftest func-test-2
  (test-code-exception
    "defn <T> addNumbers(x: T, y: T) -> Number {x+y}; addNumbers(3, \"four\");"
    Exception$InconsistentGenericTypes))


(deftest func-test-3
  (test-code
    "defn doubleNumber(x: Number) -> Number {x+x};
     defn applyToFive(func: (Number) -> Number) -> Number {func(5)};
     applyToFive(doubleNumber);"
    ['(clojure.core/defn doubleNumber [x] (+ x x))
     '(clojure.core/defn applyToFive [func] (func 5))
     '(applyToFive doubleNumber)]
    true))


(deftest func-test-4
  (test-code
    "defn <T> addNumbers(x: T, y: T) -> T {x + y};
     defn applyToNums(func: (Number, Number) -> Number) -> Number {
         func(5.0, 3.0);
     };
     applyToNums(addNumbers);"
    ['(clojure.core/defn doubleNumber [x] (+ x x))
     '(clojure.core/defn applyToFive [func] (func 5))
     '(applyToFive doubleNumber)]
    true))