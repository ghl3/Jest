(ns jest.method-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest method-test-0
  (test-code
   "val g = a.foobar();"
   ["(def g (foobar a))"]))

(deftest method-test-1
  (test-code
   "val g = a.foobar(b);"
   ["(def g (foobar a b))"]))

(deftest method-test-2
  (test-code
   "val g = x.foobar(a,b);"
   ["(def g (foobar x a b))"]))

(deftest method-test-3
  (test-code
   "val g = y.foobar(a, b, c);"
   ["(def g (foobar y a b c))"]))

