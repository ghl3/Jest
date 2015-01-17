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


(defn test-code
  "Test that the given jest code
  compiles into the given clojure code"
  [jest clojure]
  (let [code-list (. JestCompiler (getCode jest))]
    (println "\nJest: ")
    (println jest)
    (println "Clojure: ")
    (doall (map println code-list))
    (println "")
    (is (= code-list clojure))))


(deftest val-test
  (test-code
   "val foo = 10;"
   ["(def foo 10)"]))

(deftest multi-val-test
  (test-code
   "val foo = 10; \n val bar = 20;"
   ["(def foo 10)" "(def bar 20)"]))

(deftest func-test
  (test-code
   "foobar(a, b, c);"
   ["(foobar a b c)"]))

(deftest println-test
  (test-code
   "println(a);"
   ["(println a)"]))

(deftest func-def-test
  (test-code
   "defn foobar(a, b, c){ 10 };"
   ["(defn foobar[ a b c] 10)"]))

(deftest import-test
  (test-code
   "import foo.bar;"
   ["(import 'foo.bar)"]))

(deftest list-test-1
  (test-code
   "val list = [1,2,3];"
   ["(def list [1, 2, 3])"]))

(deftest list-test-2
  (test-code
   "val list = [1, 2, 3];"
   ["(def list [1, 2, 3])"]))

(deftest list-test-3
  (test-code
   "val list = [1, 2, 3 ];"
   ["(def list [1, 2, 3])"]))

(deftest list-test-4
  (test-code
   "val list = [1, 2, 3 ];"
   ["(def list [1, 2, 3])"]))

(deftest list-test-5
  (test-code
   "\nval list = [1, 2, 3 ];\n"
   ["(def list [1, 2, 3])"]))

