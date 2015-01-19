(ns jest.arithmetic-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest sum-test-1
  (test-code
   "val x = 1 + 2;"
   ["(def x (+ 1 2))"]))

(deftest sum-test-val-1
  (test-code-eval
   "1 + 2"
   "(+ 1 2)"
   3))

(deftest sum-test-2
  (test-code
   "val x = 1 + 2 + 3;"
   ["(def x (+ (+ 1 2) 3))"]))

(deftest product-test-1
  (test-code
   "val x = 1 * 2;"
   ["(def x (* 1 2))"]))

(deftest product-test-2
  (test-code
   "val x = 1 * 2 * 3;"
   ["(def x (* (* 1 2) 3))"]))

(deftest quotent-test-1
  (test-code
   "val x = 1 / 2;"
   ["(def x (/ 1 2))"]))

(deftest quotent-test-2
  (test-code
   "val x = 1 / 2 / 3;"
   ["(def x (/ (/ 1 2) 3))"]))

(deftest multiply-divide-test
  (test-code
   "val x = 1 * 2 / 3;"
   ["(def x (/ (* 1 2) 3))"]))

(deftest add-multiply-test
  (test-code
   "val x = 1 + 2 / 3;"
   ["(def x (+ 1 (/ 2 3)))"]))

(deftest func-test-1
  (test-code
   "val x = f(a, b, c) + 5;"
   ["(def x (+ (f a b c) 5))"]))

(deftest func-test-2
  (test-code
   "val x = f(a+b, b, c) + 5;"
   ["(def x (+ (f (+ a b) b c) 5))"]))


(deftest paren-test-1
  (test-code
   "val x = (a+b);;"
   ["(def x (+ a b))"]))

(deftest paren-test-2
  (test-code
   "val x = (a+b) + c;"
   ["(def x (+ (+ a b) c))"]))

(deftest paren-test-2
  (test-eval
   "((3+5) / 2)*10 - 5"
   35))
