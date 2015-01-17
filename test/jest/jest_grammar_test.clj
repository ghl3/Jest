(ns jest.jest-grammar-test
  (:require [clojure.test :refer :all]))

(import 'jest.grammar.JestParser)
(import 'jest.grammar.JestCompiler)

(deftest assignment-test
  (let [program "val foo = 10;"
        ast (. JestCompiler (compile program))]
    (is (.. ast getType) (. JestParser ID))
    (is (.. ast (getChild 0) getType) (. JestParser INTEGER_NUMBER))
    ))

(deftest func-test
  (let [program "foobar(a, b, c);"
        ast (. JestCompiler (compile program))]
    (is (.. ast getType) (. JestParser ID))
    (is (.. ast (getChild 0) getType) (. JestParser ID))
    (is (.. ast (getChild 0) getType) (. JestParser ID))
    (is (.. ast (getChild 0) getType) (. JestParser ID))
    ))

(deftest code-test
  (let [program "val foo = 10;"
        code (. JestCompiler (getCode program))]
    (is (= code ["(def foo 10)"]))))
