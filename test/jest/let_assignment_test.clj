(ns jest.let-assignment-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest assignment-scope-test-1
  (test-code
   "{let a = 10; a;}"
   ['(clojure.core/let [ a 10 ] a)]))

(deftest assignment-scope-test-2
  (test-code
   "let a = 10; a;"
   ['(clojure.core/let [ a 10 ] a)]))

(deftest assignment-scope-test-3
  (test-code
   "{let a = 10; a; let b = 20; b;}"
   ['(clojure.core/let [ a 10 ] a (clojure.core/let [ b 20 ] b))]))

(deftest assignment-scope-test-4
  (test-code
   "let a = 10; a; let b = 20; b;"
   ['(clojure.core/let [ a 10 ] a (clojure.core/let [ b 20 ] b))]))


(deftest func-scope-test-1
  (test-code
   "defn foo(x, y) {let z = x+y; z;};"
   ['(clojure.core/defn foo [ x y ] (clojure.core/let [ z (+ x y) ] z))]))
