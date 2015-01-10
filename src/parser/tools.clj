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
             [[match variable value]] (list 'def variable value)
             :else nil)
      nil)))
