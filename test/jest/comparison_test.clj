(ns jest.comparison-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest comparison-test-1
  "10 > 5;"
  ["(> 10 5)"]
  true)

(deftest comparison-test-2
  "10 < 5;"
  ["(< 10 5)"]
  false)

(deftest comparison-test-3
  "10 >= 10;"
  ["(>= 10 10)"]
  true)

(deftest comparison-test-4
  "10 <= 10;"
  ["(<= 10 10)"]
  false)

(deftest comparison-test-5
  "10 == 10;"
  ["(= 10 10)"]
  true)
