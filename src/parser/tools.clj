(ns parser.tools
  (:require[clojure.core.match :refer [match]]))

(def val-statement-regex #"\s*val\s+(\w+)\s*=\s*(\w+)\s*")

(defn parse-val-statement
  "Parse the setting of a val of the form:
  val x = 10;"
  [text]

  (let [matcher (re-matcher val-statement-regex text)]
    (println text)
    (if (re-find matcher)
      (match [(re-groups matcher)]
             [[match variable value]] (read-string (format "(def %s %s)" variable value)) ;;'(def variable value)
             :else nil)
      nil)))


(def whitespace-regex #"\s*//.*")

(defn parse-whitespace-statement
  "Parse the setting of a val of the form:
  val x = 10;"
  [text]
  (println (format "Parsing for whitepspace: %s" text))
  (if (re-matches whitespace-regex text) '() nil))
