(ns jest.let-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest let-test-1
  (test-code-eval
   "let (val x=10) { x+5 };"
   ["(let [x 10] (+ x 5))"]
   15))

(deftest let-test-2
  (test-code-eval
   "let (val x=10; val y=20) { x+y };"
   ["(let [x 10 y 20] (+ x y))"]
   30))

(deftest let-test-semicolon-2
  (test-code-eval
   "let (val x=10; val y=20;) { x+y };"
   ["(let [x 10 y 20] (+ x y))"]
   30))
