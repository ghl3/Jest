(ns jest.jest
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [jest.parser :refer :all])
  (:gen-class))


(def cli-options
  [
   ["-v" "--verbose" :flag true]
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


(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Read the main file and parse it
    ;; Print the results

    (cond
     (:help options) (exit 0 (usage summary)))

    (let [source-file (first arguments)
          source-code (slurp source-file)]
      (if (:verbose options) (do
                               (println source-code)
                               (print-jest source-code)) nil)
      (eval-jest source-code))))
