(ns jest.parser
  (:require [clojure.core.typed :as t]
            [clojure.string :as str]))

(import 'jest.grammar.JestCompiler)


(defn parse-source-file [code]
  "Parse a string representing a full
   jest source file and return a list of
   clojure expressions."
  (. JestCompiler (parseSourceFile code)))


(defn parse-expression [code]
  "Parse a string representing a jest
  expression and return a clojure expression"
  (. JestCompiler (parseExpression code)))


(defn create-ast
  "Get the AST"
  [program]
  (. JestCompiler (compile program)))


(defn get-clojure [jest-code]
  "Converge the given jest code into clojure source"
  (str/join "\n" (parse-source-file jest-code)))


(defn eval-clojure [clojure]
  "Read the given clojure code and evaluate it,
  returning the value"
  (eval (read-string clojure)))


(defn do-eval-clojure [clojure]
  "Read the given clojure code and evaluate it,
  returning the value"
  (eval-clojure (format "(do %s )" clojure)))


(defn print-jest [jest-code]
  "Evaluate the given jest code and
  return the value"
  (doall (map println (parse-source-file jest-code))))


(defn eval-jest [jest-code]
  "Take a string representing jest source code and evaluate
  it, returning the terminal value."
  (do-eval-clojure (get-clojure jest-code)))


(defn execute-jest [jest-code]
  "Take a list of strings representing jest source code statements
  and evaluate them all sequentially."
  (doall (map eval-clojure (parse-source-file jest-code))))
