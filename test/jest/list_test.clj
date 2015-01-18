(ns jest.list-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest list-test-1
  (test-code
   "val list = [1,2,3];"
   ["(def list [1, 2, 3])"]))

(deftest list-test-2
  (test-code
   "val list = [1, 2, 3];"
   ["(def list [1, 2, 3])"]))

(deftest list-test-3
  (test-code
   "val list = [1, 2, 3 ];"
   ["(def list [1, 2, 3])"]))

(deftest list-test-4
  (test-code
   "val list = [1, 2, 3 ];"
   ["(def list [1, 2, 3])"]))

(deftest list-test-5
  (test-code
   "\nval list = [1, 2, 3 ];\n"
   ["(def list [1, 2, 3])"]))


(deftest map-get-1
  (test-code
   "val list = [1, 2, 3, 4, 5]; val x = list[3];"
   ["(def list [1, 2, 3, 4, 5])" "(def x (get list 3))"]))
