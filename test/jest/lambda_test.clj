(ns jest.lambda-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest lambda-test-1
  (test-code
   "def myFunc = (x) -> {x+x};"
   ['(def myFunc (clojure.core/fn [x] (+ x x)))]))

(deftest lambda-test-2
  (test-code
   "def myFunc = (x) -> {x + (x*x)};"
   ['(def myFunc (clojure.core/fn [x] (+ x (* x x))))]))

(deftest lambda-test-3
  (test-code
   "def myFunc = (x, y, z) -> {x + (y*z)};"
   ['(def myFunc (clojure.core/fn [x y z] (+ x (* y z))))]))
