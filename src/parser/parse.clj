(ns parser.parse
  (:require [clojure.core.typed :as t]
            [clojure.string :as str]))

(defn parse-statement
  "Parse a single statement of text"
  [statement-text]
  '(:var :x :eq :10))


(defn parse
  "Parse a text string and return the AST"
  [text]
  (map parse-statement (str/split text #";")))


