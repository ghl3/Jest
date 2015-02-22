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

