(ns jest.list-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest list-test-1
  (test-code
   "def list = [1,2,3];"
   ["(def list [1, 2, 3])"]))

(deftest list-test-2
  (test-code
   "def list = [1, 2, 3];"
   ["(def list [1, 2, 3])"]))

(deftest list-test-3
  (test-code
   "def list = [1, 2, 3 ];"
   ["(def list [1, 2, 3])"]))

(deftest list-test-4
  (test-code
   "def list = [1, 2, 3 ];"
   ["(def list [1, 2, 3])"]))

(deftest list-test-5
  (test-code
   "\ndef list = [1, 2, 3 ];\n"
   ["(def list [1, 2, 3])"]))


(deftest map-get-1
  (test-code
   "def list = [1, 2, 3, 4, 5]; def x = list[3];"
   ["(def list [1, 2, 3, 4, 5])" "(def x (get list 3))"]))
