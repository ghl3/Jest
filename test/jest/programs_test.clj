(ns jest.programs-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest print-test
  (let [program (slurp "resources/print.jst")]
    (test-code
     program
     ['(def x 10) '(def y 20) '(println x) '(println y)])))


(deftest list-test
  (let [program (slurp "resources/list.jst")]
    (test-code
     program
     ['(def lst [1, 2, 3]) '(println lst) '(println (first lst)) '(println (clojure.core/get lst 2))])))

(deftest map-test
  (let [program (slurp "resources/map.jst")]
    (test-code
     program
     ['(def mp {:a 10 :b 20 :c 20}) '(def x (clojure.core/get mp :a)) '(def y (clojure.core/get mp :b)) '(println x y (+ x y))])))
