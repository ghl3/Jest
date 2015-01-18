(ns jest.utils
  (:require [clojure.test :refer :all]))

(import 'jest.grammar.JestParser)
(import 'jest.grammar.JestCompiler)

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


(defn create-ast
  "Get the AST"
  [program]
  (. JestCompiler (compile program)))
