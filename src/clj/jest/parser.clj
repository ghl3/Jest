(ns jest.parser
  (:require [clojure.core.typed :as t]
            [jest.compiler.JestToClojureTranslator])
  (:import (jest.compiler JestToClojureTranslator)))

(import 'jest.compiler.JestCompiler)


(defn validate-source-code
  "Validate jest source code, returning
  true if it is valid and false if invalid"
  [jest-code] (. JestCompiler (isSourceCodeValid jest-code)))


(defn jest->clojure
  "Take a string representing Jest source
  code and return a list of Clojure forms
  representing the execution representation
  of the given Jest code"
  [jest-src-str]
  (let [tree (. JestCompiler (compileSourceCodeToParseTree jest-src-str))
        parser (new JestToClojureTranslator)]
    (.. parser (visit tree))))


(defn execute-jest
  "Takes a string of Kest source code,
  converts it into a list of clojure forms,
  and executes the clojure forms.
  Returns the value of the last executed form."
  [jest-source-str]
  (last (map eval (jest->clojure jest-source-str))))


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


(defn type-check-clojure [clj]
  "Type check the given clojure source code"
  (let [clj-form (read-string clj)]
    (t/check-form* clj-form)))

