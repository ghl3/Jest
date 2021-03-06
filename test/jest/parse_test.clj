(ns jest.parse-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest val-test
  (test-code
   "def foo = 10;"
   ['(def foo 10)]))

(deftest multi-val-test
  (test-code
   "def foo = 10; \n def bar = 20;"
   ['(def foo 10) '(def bar 20)]))

(deftest println-test
  (test-code
   "println(a);"
   ['(println a)]))

(deftest func-def-test
  (test-code
   "defn foobar(a, b, c){ 10; };"
   ['(clojure.core/defn foobar [ a b c ] 10)]))

(deftest import-test
  (test-code
   "import foo.bar;"
   ['(clojure.core/import foo.bar)]))

(deftest sum-test
  (test-code
   "def list = [1, 2, 3];\nprintln(sum(list));"
   ['(def list [1, 2, 3]) '(println (sum list))]))

(deftest symbol-test
  (test-code
   "def x = :mySymbol;"
   ['(def x :mySymbol)]))
