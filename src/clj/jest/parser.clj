(ns jest.parser
  (:require [clojure.core.typed :as t]
            [clojure.string :as str]))

(import 'jest.compiler.JestCompiler)


(defn parse-source-file
  "Parse a string representing a full
   jest source file and return a list of
   clojure expressions.
   Runs correctness validation checks by
   default "
  ([code] (. JestCompiler (parseSourceFile code)))
  ([code validate?] (. JestCompiler (parseSourceFile code validate?))))


(defn parse-expression [code]
  "Parse a string representing a jest
  expression and return a clojure expression"
  (. JestCompiler (parseExpression code)))


(defn create-ast
  "Get the AST"
  [program]
  (. JestCompiler (compileSourceCodeToAst program)))


(defn add-additional-code
  "Add additional source code necessary after
  the direct translation from Jest->Clojure"
  [clojure-source]
  ;; Do we want to add anything from here:
  ; https://github.com/clojure/core.typed/blob/master/module-check/src/main/clojure/clojure/core/typed/base_env.clj
  (format
  "(do
     (require '[jest.core :refer :all])
     (require '[clojure.core.typed :as t])
     %s
   )" clojure-source))


(defn get-clojure
  "Convert the given jest code into clojure source.
  Optionally include any additional clojure source
  code to complete the Jest->Clojure translation"
  ([jest-code] (get-clojure jest-code false))
  ([jest-code add-additional?]
     (let [raw-clojure-list (parse-source-file jest-code)
           raw-clojure (str/join "\n" raw-clojure-list)]
       (if add-additional?
         (add-additional-code raw-clojure)
         raw-clojure))))


(defn eval-clojure [clojure]
  "Read the given clojure code and evaluate it,
  returning the value"
  (eval (read-string clojure)))


(defn do-eval-clojure [clojure]
  "Read the given clojure code and evaluate it,
  returning the value"
  (eval-clojure (format "(do %s )" clojure)))


(defn type-check-clojure [clj]
  "Type check the given clojure source code"
  (let [clj-form (read-string clj)]
    (t/check-form* clj-form)))


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
  (eval-clojure (get-clojure jest-code true)))


;;(defn type-check-jest [jest-code]
;;  "Type check the given jest source code"
;;  (type-check-clojure (parse-source-file jest-code)))


(defn type-check-jest [jest-code]
  "Type check the given jest source code.
  It seems that we have to wrap the resulting
  clojure in a 'do' loop to get it to work."
  (let [clojure-source (get-clojure jest-code true)
        clojure-form (read-string clojure-source)]
    (t/check-form* clojure-form)))
