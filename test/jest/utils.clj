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


(defn test-jest-vs-clojure
  [clojure code-list]

  ;; Compare each line individually
  (doall (map compare-code clojure code-list))

  ;; Conmpare all of the code
  (is (= (map remove-code-compare-whitespace clojure)
         (map remove-code-compare-whitespace code-list))))


(defn test-code
  "Test that the given jest code
  compiles into the given clojure code"
  ([jest clojure] (test-code jest clojure false))
  ([jest clojure validate?]
     (let [code-list (parse-source-code jest validate?)]
       (test-println "\nJest: ")
       (test-println jest)
       (test-println "Clojure: ")
       (doall (map test-println code-list))
       (test-println "")
       (test-jest-vs-clojure clojure code-list))))


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
  (test-code jest clojure true)
  (let [code-val (eval-jest jest)]
    (test-println "Code Val:")
    (test-println code-val)
    (is (= code-val val))))


(defn test-clojure-type-correct
  "Takes clojure code and either asserts that
  the code is correct in terms of type or that
  it IS NOT (based on correct?)"
  ([clojure-code] (test-clojure-type-correct clojure-code true))
  ([clojure-code correct?]
     (if correct?
       (is (type-check-clojure clojure-code))
       (is (thrown? clojure.lang.ExceptionInfo (type-check-clojure clojure-code))))))


(defn test-clojure-type-equals
  "Takes clojure code as a string and ensures
  that it is type correct.  It then checks the
  resulting type against the supplied type and
  either requires that they match or that they
  DO NOT match (based on match?)"

  ([clojure-code type] (test-clojure-type-equals clojure-code type true))
  ([clojure-code type match?]
     (test-clojure-type-correct clojure-code)
     (if match?
       (is (= (type-check-clojure clojure-code) type))
       (is (not= (type-check-clojure clojure-code) type)))))


(defn test-jest-type-correct
  "Takes jest code and either asserts that
  the code is correct in terms of type or that
  it IS NOT (based on correct?)"
  ([jest-code] (test-jest-type-correct jest-code true))
  ([jest-code correct?]
     (if correct?
       (is (type-check-jest jest-code))
       (is (thrown? clojure.lang.ExceptionInfo (type-check-jest jest-code))))))


(defn test-jest-type-equals
  "Takes jest code as a string and ensures
  that it is type correct.  It then checks the
  resulting type against the supplied type and
  either requires that they match or that they
  DO NOT match (based on match?)"

  ([jest-code type] (test-jest-type-equals jest-code type true))
  ([jest-code type match?]
     (test-jest-type-correct jest-code)
     (if match?
       (is (= (type-check-jest jest-code) type))
       (is (not= (type-check-jest jest-code) type)))))
