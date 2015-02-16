(ns jest.ast-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]
            [jest.parser :refer [create-ast]]))

(import 'jest.grammar.JestParser)
(import 'jest.compiler.JestCompiler)

;; (deftest compile-assignment-test
;;   (let [program "def foo = 10;"
;;         ast (create-ast program)]
;;     (is (.. ast getType) (. JestParser ID))
;;     (is (.. ast (getChild 0) getType) (. JestParser INTEGER))
;;     ))

;; (deftest compile-func-test
;;   (let [program "foobar(a, b, c);"
;;         ast (create-ast program)]
;;     (is (.. ast getType) (. JestParser ID))
;;     (is (.. ast (getChild 0) getType) (. JestParser ID))
;;     (is (.. ast (getChild 0) getType) (. JestParser ID))
;;     (is (.. ast (getChild 0) getType) (. JestParser ID))
;;     ))

