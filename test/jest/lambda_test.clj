(ns jest.lambda-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest lambda-test-1
  (test-code
   "val myFunc = #(%+%);"
   ["(def myFunc #(+ % %))"]))
