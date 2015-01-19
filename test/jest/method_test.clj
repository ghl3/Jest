(ns jest.method-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest method-test-1
  (test-code
   "a.foobar(b);"
   ["(foobar a b)"]))

(deftest method-test-2
  (test-code
   "x.foobar(a,b);"
   ["(foobar x a b)"]))

(deftest method-test-3
  (test-code
   "y.foobar(a, b, c);"
   ["(foobar y a b c)"]))

