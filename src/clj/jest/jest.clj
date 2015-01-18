(ns jest.jest
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            ;;[parser.parse :as parse]
            )
(:gen-class))

(import 'jest.grammar.JestCompiler)

(def cli-options
  [
   ;; A non-idempotent option
   [nil "-v" "--verbose" "Print verbose information"]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])


(defn usage [options-summary]
  (->> ["This is my program. There are many like it, but this one is mine."
        ""
        "Usage: program-name [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  start    Start a new server"
        "  stop     Stop an existing server"
        "  status   Print a server's status"
        ""
        "Please refer to the manual page for more information."]
       (str/join \newline)))


;; TODO: Move the next three functions to the parser
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


(defn print-jest [jest-code]
  "Evaluate the given jest code and
  return the value"
  (doall (map println (parse-source-file jest-code))))


(defn eval-jest [jest-code]
  "Evaluate the given jest code and
  return the value"
  (doall (map (comp eval read-string) (parse-source-file jest-code))))


(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Read the main file and parse it
    ;; Print the results
    (let [code (slurp (first arguments))]
      (println code)
      (print-jest code)
      (eval-jest code))))
