(ns parser.parse
  (:require [clojure.core.typed :as t]
            [clojure.string :as str]
            [parser.tools :as tools]))


(defn parse-statement
  "Parse a single statement of text"
  [statement-text]
  (let [ast (or (tools/parse-val-statement statement-text)
                (tools/parse-whitespace-statement statement-text))]
    (if ast ast
        (throw (Exception. (format "Failed to parse statement: %s" statement-text))))))


(defn strip-comments
  "Remove comments"
  [line]
  (str/replace line #"[//,#].*" ""))


(defn strip-ignored
  "Break into lines,
  remove comments from each line,
  remove empty lines,
  and concatenate again."
  [text]

  (->> text
      (#(str/split % #"\n"))
      (map strip-comments)
      (remove clojure.string/blank?)
      (str/join "")))


(defn parse
  "Parse a text string and return the AST"
  [text]
  (let [cleaned (strip-ignored text)
        statements (str/split cleaned #";")]
    (println "Statements: ")
    (println statements)
    (map parse-statement statements)))
