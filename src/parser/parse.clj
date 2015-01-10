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
;;  (first (str/split line #"[//,#]")))


(defn strip-ignored
  "Break into lines,
  remove comments from each line,
  remove empty lines,
  and concatenate again."
  [text]

  (let [lines (str/split text #"\n")
        no-comments (map strip-comments lines)
        non-empty-lines (remove clojure.string/blank? no-comments)]
    (str/join "" non-empty-lines)))


(defn parse
  "Parse a text string and return the AST"
  [text]
  (let [cleaned (strip-ignored text)
        statements (str/split cleaned #";")]
    (println "Statements: ")
    (println statements)
    (map parse-statement statements)))
;;  (let [stripped-text (strip text)
;;        (map parse-statement (str/split stripped-text #";"))))

