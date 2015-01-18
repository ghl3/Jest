(ns jest.parse-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest val-test
  (test-code
   "val foo = 10;"
   ["(def foo 10)"]))

(deftest multi-val-test
  (test-code
   "val foo = 10; \n val bar = 20;"
   ["(def foo 10)" "(def bar 20)"]))

(deftest func-test-1
  (test-code
   "foobar(a);"
   ["(foobar a)"]))

(deftest func-test-2
  (test-code
   "foobar(a,b);"
   ["(foobar a b)"]))

(deftest func-test-3
  (test-code
   "foobar(a, b, c);"
   ["(foobar a b c)"]))

(deftest println-test
  (test-code
   "println(a);"
   ["(println a)"]))

(deftest func-def-test
  (test-code
   "defn foobar(a, b, c){ 10 };"
   ["(defn foobar[ a b c] 10)"]))

(deftest import-test
  (test-code
   "import foo.bar;"
   ["(import 'foo.bar)"]))

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


(deftest sum-test
  (test-code
   "val list = [1, 2, 3];\nprintln(sum(list));"
   ["(def list [1, 2, 3])" "(println (sum list))"]))
