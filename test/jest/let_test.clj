(ns jest.let-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest let-test-1
  (test-code-eval
   "let (val x=10) { x+5 };"
   ["(let [x 10] (+ x 5))"]
   15))
