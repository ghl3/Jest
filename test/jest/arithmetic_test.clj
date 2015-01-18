(ns jest.arithmetic-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]
            :verbose))

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
