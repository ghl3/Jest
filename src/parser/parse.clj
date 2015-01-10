(ns parser.parse
  (:require [clojure.core.typed :as t]
            [clojure.string :as str]
            [parser.tools :as tools]))


(defn parse-statement
  "Parse a single statement of text"
  [statement-text]
  (or (tools/parse-val-statement statement-text)))


(defn parse
  "Parse a text string and return the AST"
  [text]
  (map parse-statement (str/split text #";")))

