(ns jest.grammar-test
  (:require [clojure.test :refer :all]))

(import 'jest.grammar.S001HelloWordParser)
(import 'jest.grammar.S001HelloWordCompiler)

(deftest a-test
  (let [ast (. S001HelloWordCompiler (compile "Hello world!"))]
    (is (.. ast (getChild 0) getType) (. S001HelloWordParser SALUTATION))
    (is (.. ast (getChild 1) getType) (. S001HelloWordParser ENDSYMBOL))
    ))
