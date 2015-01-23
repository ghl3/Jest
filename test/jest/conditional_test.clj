(ns jest.conditional-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest if-test-1
  (test-code-eval
   "if (true) {10;} else {20;};"
   ["(if true 10 20)"]
   10))

(deftest if-test-2
  (test-code-eval
   "if (nil) {10} else {20};"
   ["(if nil 10 20)"]
   20))
