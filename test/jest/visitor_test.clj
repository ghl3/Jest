(ns jest.visitor-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(import 'jest.compiler.JestCompiler)


(defn test-visitor
  [source expected-lines]
  (let [clojure (. JestCompiler compileToClojureVisitor source)]
    (test-jest-vs-clojure clojure expected-lines)))


(deftest import-test-1
  (test-visitor
   "import foo;"
   ["(import 'foo)"]))

(deftest def-test-1
  (test-visitor
    "def foo=:bar;"
    ["(def foo :bar)"]))

(deftest def-test-2
  (test-visitor
    "import foo;\n def foo=:bar;"
    ["(import 'foo)"
     "(def foo :bar)"]))

(deftest func-test-1
  (test-visitor
    "defn foo(x) { x+12; };"
    ["(defn foo [ x ] (+ x 12))"]))


(deftest sum-test-1
  (test-visitor
    "10 + 17;"
    ["(+ 10 17)"]))


(deftest expression-test-1
  (test-visitor
    "def x = (12 + 13) / (12 - 15);"
    ["(def x (/ (+ 12 13) (- 12 15)))"]))


(deftest method-test-1
  (test-visitor
    "def g = a.foobar();"
    ["(def g (foobar a))"]))


(deftest method-test-1
  (test-visitor
    "y.foobar().bar();"
    ["(bar (foobar y) )"]))


(deftest lambda-test-1
  (test-visitor
    "def myFunc = #(%+%);"
    ["(def myFunc #(+ % %))"]))


(deftest function-call-test-1
  (test-visitor
    "foo(a, b, c);"
    ["(foo a b c)"]))