(ns jest.jest-grammar-test
  (:require [clojure.test :refer :all]))

(import 'jest.grammar.JestParser)
(import 'jest.grammar.JestCompiler)

(deftest a-test
  (let [program "val foo = 10;"
        ast (. JestCompiler (compile program))]
    (is (.. ast (getChild 0) getType) (. JestParser SALUTATION))
    (is (.. ast (getChild 1) getType) (. JestParser ENDSYMBOL))
    ))
