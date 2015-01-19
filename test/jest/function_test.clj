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
