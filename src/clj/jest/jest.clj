(ns jest.jest
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [parser.parse :as parse]
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


(defn get-code [code]
  (. JestCompiler (getCode code)))


(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]

    ;; Read the main file and parse it
    ;; Print the results
    (let [code (slurp (first arguments))]
      (println code)
      (doall (map println (get-code code)))
      (doall (map (comp eval read-string) (get-code code)))
      )))
