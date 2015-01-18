(ns jest.programs-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest print-test
  (let [program (slurp "resources/print.jst")]
    (test-code
     program
     ["(def x 10)" "(def y 20)" "(println x)" "(println y)"])))


(deftest list-test
  (let [program (slurp "resources/list.jst")]
    (test-code
     program
     ["(def list [1, 2, 3])" "(println list)" "(println (first list))"])))
