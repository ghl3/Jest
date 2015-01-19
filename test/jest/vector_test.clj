(ns jest.vector-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest vec-test-0
  (test-code
   "val x = [];"
   ["(def x [])"]))


(deftest vec-test-1
  (test-code
   "val x = [1];"
   ["(def x [1])"]))

(deftest vec-test-2
  (test-code
   "val x = [1, 2];"
   ["(def x [1, 2])"]))


(deftest vec-get-test
  (test-code-eval
   "val x = [1, 2]; x.get(0);"
   ["(def x [1, 2])" "(get x 0)"]
   1))
