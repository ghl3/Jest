(ns jest.programs-test
  (:require [clojure.test :refer :all]))

(import 'jest.grammar.JestParser)
(import 'jest.grammar.JestCompiler)


(deftest func-resource-test-jst-test
  (let [program (slurp "resources/print.jst")
        code (. JestCompiler (getCode program))]
    (is (= code ["(def x 10)" "(def y 20)" "(println x)" "(println y)"]))))
