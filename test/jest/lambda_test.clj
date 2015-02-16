(ns jest.lambda-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest lambda-test-1
  (test-code
   "def myFunc = #(%+%);"
   ["(def myFunc #(+ % %))"]))

(deftest lambda-test-2
  (test-code
   "def myFunc = #( % + (%*%) );"
   ["(def myFunc #(+ % (* % %)))"]))

(deftest lambda-test-3
  (test-code
   "def myFunc = #( %1 + (%2*%3) );"
   ["(def myFunc #(+ %1 (* %2 %3)))"]))
