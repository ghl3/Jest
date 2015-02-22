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
