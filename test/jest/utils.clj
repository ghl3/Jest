(ns jest.utils
  (:require [clojure.test :refer :all]
            [jest.parser :refer :all]
            [clojure.string :as string]))

(defn remove-code-compare-whitespace
  [s]
  (string/replace s #"[\n\t]" ""))

(defn compare-code
  [x y]
  (println x y)
  (is (= (remove-code-compare-whitespace x) (remove-code-compare-whitespace y))))


(defn test-code
  "Test that the given jest code
  compiles into the given clojure code"
  [jest clojure]
  (let [code-list (parse-source-file jest)]
    (println "\nJest: ")
    (println jest)
    (println "Clojure: ")
    (doall (map println code-list))
    (println "")

    ;; Compare each line individually
    (doall (map compare-code clojure code-list))

    ;; Conmpare all of the code
    (is (= (map remove-code-compare-whitespace clojure)
           (map remove-code-compare-whitespace code-list)))))


(defn test-eval
  "Evaluate the jest expression and
  assert that it equals the given value"
  [jest val]
  (let [expression (parse-expression jest)]
    (println "\nJest: ")
    (println jest)
    (println "Clojure: ")
    (println expression)
    (println "")

    (let [code-val (read-and-eval expression)]
      (println "Code Val:")
      (println code-val)
      (is (= code-val val)))))


(defn test-code-eval
  "Test that the given jest code
  compiles into the given clojure code.
  In addition, test that, when evaluated,
  the code gives the supplied 'val'."
  [jest clojure val]
  (let [expression (parse-expression jest)]
    (println "\nJest: ")
    (println jest)
    (println "Clojure: ")
    (println expression)
    (println "")

    (is (= expression clojure))
    (let [code-val (read-and-eval expression)]
      (println "Code Val:")
      (println code-val)
      (is (= code-val val)))))

