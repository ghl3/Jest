(ns jest.parse-test
  (:require [clojure.test :refer :all]))

(import 'jest.grammar.JestParser)
(import 'jest.grammar.JestCompiler)

(deftest compile-assignment-test
  (let [program "val foo = 10;"
        ast (. JestCompiler (compile program))]
    (is (.. ast getType) (. JestParser ID))
    (is (.. ast (getChild 0) getType) (. JestParser INTEGER))
    ))

(deftest compile-func-test
  (let [program "foobar(a, b, c);"
        ast (. JestCompiler (compile program))]
    (is (.. ast getType) (. JestParser ID))
    (is (.. ast (getChild 0) getType) (. JestParser ID))
    (is (.. ast (getChild 0) getType) (. JestParser ID))
    (is (.. ast (getChild 0) getType) (. JestParser ID))
    ))

(deftest val-test
  (let [program "val foo = 10;"
        code (. JestCompiler (getCode program))]
    (is (= code ["(def foo 10)"]))))

(deftest multi-val-test
  (let [program "val foo = 10; \n val bar = 20;"
        code (. JestCompiler (getCode program))]
    (is (= code ["(def foo 10)" "(def bar 20)"]))))

(deftest func-test
  (let [program "foobar(a, b, c);"
        code (. JestCompiler (getCode program))]
    (is (= code ["(foobar a b c)"]))))

(deftest println-test
  (let [program "println(a);"
        code (. JestCompiler (getCode program))]
    (is (= code ["(println a)"]))))

(deftest func-def-test
  (let [program "defn foobar(a, b, c){ 10 };"
        code (. JestCompiler (getCode program))]
    (is (= code ["(defn foobar[ a b c] 10)"]))))

(deftest import-test
  (let [program "import foo.bar;"
        code (. JestCompiler (getCode program))]
    (is (= code ["(import 'foo.bar)"]))))
