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

(deftest method-chain-test-1
  (test-code
   "y.foobar().bar();"
   ["(bar (foobar y))"]))

(deftest method-chain-test-2
  (test-code
   "y.foobar().bar().baz();"
   ["(baz (bar (foobar y)))"]))

(deftest method-chain-test-3
  (test-code
   "y.foobar().bar(10).baz();"
   ["(baz (bar (foobar y) 10))"]))

(deftest method-chain-test-4
  (test-code
   "y.foobar(2).bar(10).baz();"
   ["(baz (bar (foobar y 2) 10))"]))

(deftest method-chain-test-5
  (test-code
   "y.foobar(2).bar(10, 20, 15).baz(true);"
   ["(baz (bar (foobar y 2) 10 20 15) true)"]))


 (deftest method-chain-test-6
   (test-code
    "range(0, 100).first();"
    ["(first (range 0 100))"]))


 (deftest method-chain-test-7
   (test-code
    "[1, 2, 3].get(1);"
    ["(get [1, 2, 3] 1)"]))


 (deftest method-pipe-test-1
   (test-code
    "y->filter(even?);"
    ["(filter even? y)"]))


