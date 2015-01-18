(ns jest.utils
  (:require [clojure.test :refer :all]
            [jest.jest :refer :all]))

;;(import 'jest.grammar.JestParser)
;;(import 'jest.grammar.JestCompiler)

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
    (is (= code-list clojure))))


(defn test-code-and-val
  "Test that the given jest code
  compiles into the given clojure code.
  In addition, test that, when evaluated,
  the code gives the supplied 'val'."
  [jest clojure val]
  (let [code-list (parse-source-file jest)]
    (println "\nJest: ")
    (println jest)
    (println "Clojure: ")
    (doall (map println code-list))
    (println "")
    (is (= code-list clojure))
    (let [code-val (eval-jest jest)]
      (is code-val val))))

