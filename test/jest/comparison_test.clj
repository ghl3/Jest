(ns jest.comparison-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest comparison-test-1
  (test-code-eval
  "10 > 5;"
  ['(> 10 5)]
  true))

(deftest comparison-test-2
  (test-code-eval
  "10 < 5;"
  ['(< 10 5)]
  false))

(deftest comparison-test-3
  (test-code-eval
  "10 >= 10;"
  ['(>= 10 10)]
  true))

(deftest comparison-test-4
  (test-code-eval
  "10 <= 10;"
  ['(<= 10 10)]
  true))

(deftest comparison-test-5
  (test-code-eval
  "10 == 10;"
  ['(= 10 10)]
  true))


(deftest comparison-test-6
  (test-code-eval
  "10 + 20 + 1 > 30;"
  ['(> (+ (+ 10 20) 1) 30)]
  true))


(deftest comparison-test-6
  (test-code-eval
   "def x = 12;
    def y = 12;
    x == y;"
   ['(def x 12)
    '(def y 12)
    '(= x y)]
   true))
