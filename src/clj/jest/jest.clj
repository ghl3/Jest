(ns jest.jest
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [jest.parser :refer :all])
  (:gen-class))


(def cli-options
  [
   ["-v" "--verbose" :flag true]
   ["-c" "--clojure" "Print the Clojure representation of the Jest source code" :flag true]
   ["-t" "--type-check" "Type check the Jest code" :flag true]
   ["-h" "--help"]])


(defn usage [options-summary]
  (->> ["This is the main Jest executable."
        ""
        "Usage: jest /path/to/source/file.jst [options]"
        ""
        "Options:"
        options-summary
        ""
        "Please refer to the hosted source for more information: https://github.com/ghl3/Jest"]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))


(defn exit [status msg]
  (println msg)
  (System/exit status))


(defn line-break [n]
  (str/join (repeat n "=")))


(defn verbose-print-jest-source [source-code]
  "Print helpful information based on the
  input source code we're running"

  (println "\n" "Raw input source code: \n")
  (println (line-break 20))
  (println source-code)
  (println (line-break 20))

  (println "\n" "Translated into Clojure: \n")
  (println (line-break 20))
  (print-jest source-code)
  (println (line-break 20))

  (println "\n" "Program Output: \n"))


(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Read the main file and parse it
    ;; Print the results

    (cond
     (:help options) (exit 0 (usage summary)))


    (let [source-file (first arguments)
          source-code (slurp source-file)]

      ;; Print some helpful output for testing/debugging
      (cond
       (:verbose options) (exit 0 (verbose-print-jest-source source-code))
       (:clojure options) (exit 0 (get-clojure source-code)))

      ;; Type check if requested
      (cond (:type-check options) (do (println "Checking types") (type-check-jest source-code)))

      ;; Run all the things!
      (println "Running")
      (execute-jest source-code))))
