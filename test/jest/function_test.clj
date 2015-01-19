(ns jest.function-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest func-test-0
  (test-code
   "foobar();"
   ["(foobar)"]))

(deftest func-test-1
  (test-code
   "foobar(a);"
   ["(foobar a)"]))

(deftest func-test-2
  (test-code
   "foobar(a,b);"
   ["(foobar a b)"]))

(deftest func-test-3
  (test-code
   "foobar(a, b, c);"
   ["(foobar a b c)"]))

(deftest map-test
  (test-code
   "val x = map(inc, myList);"
   ["(def x (map inc myList))"]))

(deftest func-def-1
  (test-code
   "defn myFunc(a, b, c) { a + b + c; };",
   ["(defn myFunc [ a b c ] (+ (+ a b) c))"]))

(deftest func-def-2
  (test-code
   "defn myFunc(a, b, c) {
      val x = a + b + c;
      x * 2;
      };",
   ["(defn myFunc [ a b c ]
(def x (+ (+ a b) c))
(* x 2))"]))
