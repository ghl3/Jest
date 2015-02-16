(ns jest.assignment-scope-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest assignment-scope-test-1
  (test-code
   "{val a = 10; a;}"
   ["(let [ a 10 ] a)"]))

(deftest assignment-scope-test-2
  (test-code
   "val a = 10; a;"
   ["(let [ a 10 ] a)"]))

(deftest assignment-scope-test-3
  (test-code
   "{val a = 10; a; val b = 20; b;}"
   ["(let [ a 10 ] a (let [ b 20 ] b))"]))

(deftest assignment-scope-test-4
  (test-code
   "val a = 10; a; val b = 20; b;"
   ["(let [ a 10 ] a (let [ b 20 ] b))"]))


(deftest func-scope-test-1
  (test-code
   "defn foo(x, y) {val z = x+y; z;};"
   ["(defn foo [ x y ] (let [ z (+ x y) ] z))"]))
