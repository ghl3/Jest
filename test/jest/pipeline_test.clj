(ns jest.pipeline-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest pipeline-test-1
  (test-code
   "range(0, 100, 1)
     ->filter(even?)
     ->map((x)->{x+x})
     ->take(20);"
   ['(take 20 (map (clojure.core/fn [x] (+ x x)) (filter even? (range 0 100 1))))]))


(deftest pipeline-test-2
  (test-code-eval
   "range(0, 100, 1)
     ->filter(even?)
     ->map((x)->{x+x})
     ->take(20);"
   ['(take 20 (map (clojure.core/fn [x] (+ x x)) (filter even? (range 0 100 1))))]
   '(0 4 8 12 16 20 24 28 32 36 40 44 48 52 56 60 64 68 72 76)))
