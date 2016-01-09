(ns jest.vector-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest vec-test-0
  (test-code
   "def x = [];"
   ['(def x [])]))


(deftest vec-test-1
  (test-code
   "def x = [1];"
   ['(def x [1])]))

(deftest vec-test-2
  (test-code
   "def x = [1, 2];"
   ['(def x [1, 2])]))


(deftest vec-get-test
  (test-code-eval
   "def x = [1, 2]; x.get(0);"
   ['(def x [1, 2]) '(get x 0)]
   1))

(deftest vec-first-test
  (test-code-eval
   "def x = [1, 2]; x.first();"
   ['(def x [1, 2]) '(first x)]
   1))

(deftest vec-last-test
  (test-code-eval
   "def x = [1, 2]; x.last();"
   ['(def x [1, 2]) '(last x)]
   2))
