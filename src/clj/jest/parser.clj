(ns jest.parser
  (:require [clojure.core.typed :as t]
            [clojure.string :as str]
            [jest.compiler.NativeJestCompiler])
  (:import (jest.compiler NativeJestCompiler)))

(import 'jest.compiler.JestCompiler)


(defn validate-source-code
  "Validate jest source code, returning
  true if it is valid and false if invalid"
  [jest-code] (. JestCompiler (validateSourceCode jest-code)))


;(defn parse-source-code
;  "Parse a string representing a full
;   jest source file and return a list of
;   clojure expressions.
;   Runs correctness validation checks by
;   default "
;  ([jest-code] (. JestCompiler (parseSourceFile jest-code)))
;  ([jest-code validate?]
;   (if (or (not validate?) (validate-source-code jest-code))
;     (parse-source-code jest-code)
;     nil)))

;
;(defn parse-expression
;  "Parse a string representing a jest
;  expression and return a clojure expression"
;  [jest-expression]
;  (. JestCompiler (parseExpression jest-expression)))
;
;
;(defn add-additional-code
;  "Add additional source code necessary after
;  the direct translation from Jest->Clojure"
;  [clojure-source]
;  ;; Do we want to add anything from here:
;  ; https://github.com/clojure/core.typed/blob/master/module-check/src/main/clojure/clojure/core/typed/base_env.clj
;  (format
;  "(do
;     (require '[jest.core :refer :all])
;     (require '[clojure.core.typed :as t])
;     %s
;   )" clojure-source))
;



(defn jest->clojure
  "Take a string representing Jest source
  code and return a list of Clojure forms
  representing the execution representation
  of the given Jest code"
  [jest-src-str]
  (let [tree (. JestCompiler (compileSourceCodeToParseTree jest-src-str))
        parser (new NativeJestCompiler)]
    (.. parser (visit tree))))


(defn execute-jest
  "Takes a string of Kest source code,
  converts it into a list of clojure forms,
  and executes the clojure forms"
  [jest-source-str]
  (let [clojure-forms (jest->clojure jest-source-str)]
    (dorun (map eval clojure-forms))))


(defn validate-and-execute-jest
  "Takes a string of Jest source code,
  validates that the code is legitimate
  (scope checking, type checking),
  converts it to the Clojure execution
  representation, and evaluates that code"
  [jest-src-str]
  (if (validate-source-code jest-src-str)
    (do
      (execute-jest jest-src-str))
    (println "FUCK")))


;
;
;(defn get-clojure
;  "Convert the given jest code into clojure source.
;  Optionally include any additional clojure source
;  code to complete the Jest->Clojure translation"
;  ([jest-code] (get-clojure jest-code false))
;  ([jest-code add-additional?]
;     (let [raw-clojure-list (parse-source-code jest-code)
;           raw-clojure (str/join "\n" raw-clojure-list)]
;       (if add-additional?
;         (add-additional-code raw-clojure)
;         raw-clojure))))
;
;
;(defn eval-clojure [clojure]
;  "Read the given clojure code and evaluate it,
;  returning the value"
;  (eval (read-string clojure)))
;
;
;(defn do-eval-clojure [clojure]
;  "Read the given clojure code and evaluate it,
;  returning the value"
;  (eval-clojure (format "(do %s )" clojure)))


(defn type-check-clojure [clj]
  "Type check the given clojure source code"
  (let [clj-form (read-string clj)]
    (t/check-form* clj-form)))


;(defn print-jest [jest-code]
;  "Evaluate the given jest code and
;  return the value"
;  (doall (map println (parse-source-code jest-code))))


(defn eval-jest [jest-code]
  "Take a string representing jest source code and evaluate
  it, returning the terminal value."
  (last (do (map eval (jest->clojure jest-code)))))


;(defn execute-jest [jest-code]
;  "Take a list of strings representing jest source code statements
;  and evaluate them all sequentially."
;  (eval-clojure (get-clojure jest-code true)))
;
;
;(defn type-check-jest [jest-code]
;  "Type check the given jest source code.
;  It seems that we have to wrap the resulting
;  clojure in a 'do' loop to get it to work."
;  (let [clojure-source (get-clojure jest-code true)
;        clojure-form (read-string clojure-source)]
;    (t/check-form* clojure-form)))

