(ns jest.types.generic-function-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all])
  (:import (jest.compiler Exceptions$GenericInferenceException)))

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
    Exceptions$GenericInferenceException))


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
    "defn <T> getSelf(t: T) -> T {t};
     defn <T> applyIt(func: (T) -> T, t: T) -> T {
         func(t);
     };
     applyIt(getSelf, 5);"
    ['(clojure.core/defn getSelf [t] t)
     '(clojure.core/defn applyIt [func t] (func t))
     '(applyIt getSelf 5)]
    true))


(deftest func-test-5
  (test-code
    "defn numToString(x: Number) -> String {\"foobar\"};
     defn <T, U> applyIt(func: (T) -> U, t: T) -> U {
         func(t);
     };
     applyIt(numToString, 5);"
    ['(clojure.core/defn numToString [x] "foobar")
     '(clojure.core/defn applyIt [func t] (func t))
     '(applyIt numToString 5)]
    true))

(deftest func-test-6
  (test-code-exception
    "defn numToString(x: Number) -> String {\"foobar\"};
     defn <T, U> applyIt(func: (T) -> U, t: T, tt: T) -> U {
         func(t);
     };
     applyIt(numToString, 5, \"Foo\");"
    Exceptions$GenericInferenceException))
