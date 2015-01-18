(ns jest.arithmetic-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]
            :verbose))

(deftest product-test
  (test-code
   "val x = 1 * 2 * 3;"
   ["(def x (* 1 2 3))"]))

(deftest quotent-test
  (test-code
   "val x = 1 / 2 / 3;"
   ["(def x (/ 1 2 3))"]))
