(ns jest.utils
  (:require [clojure.test :refer :all]
            [jest.parser :refer :all]
            [clojure.string :as string]
            [environ.core :refer [env]]))


(defn test-println [& log]
  "Print the line if the :verbose
   keyword is set in the environment
   of the current profile"
  (if (env :verbose) (apply println log) nil))


(defn remove-code-compare-whitespace
  [s]
  (-> s
      (string/replace #"[\n\t]" " ")
      (string/replace #"\s+" " ")))

(defn compare-code
  [x y]
  (test-println x y)
  (is (= (remove-code-compare-whitespace x) (remove-code-compare-whitespace y))))


(defn test-code
  "Test that the given jest code
  compiles into the given clojure code"
  [jest clojure]
  (let [code-list (parse-source-file jest)]
    (test-println "\nJest: ")
    (test-println jest)
    (test-println "Clojure: ")
    (doall (map test-println code-list))
    (test-println "")

    ;; Compare each line individually
    (doall (map compare-code clojure code-list))

    ;; Conmpare all of the code
    (is (= (map remove-code-compare-whitespace clojure)
           (map remove-code-compare-whitespace code-list)))))


(defn test-eval
  "Evaluate the jest expression and
  assert that it equals the given value"
  [jest val]
  (let [clojure (get-clojure jest)]
    (test-println "\nJest: ")
    (test-println jest)
    (test-println "Clojure: ")
    (test-println clojure)
    (test-println "")

    (let [code-val (eval-jest jest)]
      (test-println "Code Val:")
      (test-println code-val)
      (is (= code-val val)))))


(defn test-code-eval
  "Test that the given jest code
  compiles into the given clojure code.
  In addition, test that, when evaluated,
  the code gives the supplied 'val'."
  [jest clojure val]
  (test-code jest clojure)
  (let [code-val (eval-jest jest)]
    (test-println "Code Val:")
    (test-println code-val)
    (is (= code-val val))))


(defn test-type-correct
  "Takes clojure code as a string and a
  type (as a symbol) and determines if
  the clojure code "
  ([clojure-code] (test-type-correct clojure-code true))
  ([clojure-code correct?]
     (if correct?
       (is (type-check-clojure clojure-code))
       (is (thrown? clojure.lang.ExceptionInfo (type-check-clojure clojure-code))))))


(defn test-type-equals
  "Takes clojure code as a string and a
  type (as a symbol) and determines if
  the clojure code "
  [clojure-code type]
  ((test-type-correct clojure-code)
   (is (= (type-check-clojure clojure-code) type))))
