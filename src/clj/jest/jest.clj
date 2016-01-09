(ns jest.jest
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [jest.parser :refer :all]
            [cljfmt.core :as cljfmt])
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


(defn exit [status msg]
  (println msg)
  (System/exit status))


(defn- separator
  ([] (separator 50))
  ([n] (clojure.string/join (take n (repeat "-"))))
  ([title n] (let [text (str " " title " ")
                  text-len (count text)
                  left-n (Math/ceil (/ (- n text-len) 2))
                  right-n (- n left-n text-len)]
              (str (separator left-n) text (separator right-n)))))


(defn verbose-print-jest-source
  ""
  [jest-src-str]
  (println (separator "Jest Source" 80))
  (println jest-src-str)
  (println (separator "Compiled Clojure" 80))
  (doall (map println (jest->clojure jest-src-str)))
  (println (separator 80)))


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
        (:verbose options) (verbose-print-jest-source source-code)
        (:clojure options) (exit 0 (-> source-code jest->clojure println)))

      ;; Run all the things!
      (validate-and-execute-jest source-code))))
