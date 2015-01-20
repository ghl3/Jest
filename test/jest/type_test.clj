(ns jest.type-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]
            [clojure.core.typed :as t]
            [jest.parser :refer [type-check-clojure]]))

(deftest type-val-test-1
  (test-code
   "val x : String = \"Foobar\";"
   ["(t/ann x String)
     (def x \"Foobar\")"]))


(deftest type-func-test-1
  (test-code
   "defn func(x): String -> String {
         x;
    };"
   ["(t/ann func [String -> String])
     (defn func [ x ] x)"]))

(deftest type-func-test-2
  (test-code
   "defn func(x, y): String String -> String {
         x + y;
    };"
   ["(t/ann func [String String -> String])
     (defn func [ x y ] (+ x y))"]))



;; (deftest type-clojure-pass-test-1
;;   (is (thrown? clojure.lang.ExceptionInfo
;;                (type-check-clojure
;;                 "(do (require '[clojure.core.typed :as t]) (t/ann fun [Integer -> Integer]) (defn fun [x] x) (fun 1))" (symbol 'java.lang.Integer)))))

;; (deftest type-clojure-test-1
;;   (is (thrown? clojure.lang.ExceptionInfo
;;                (type-check-clojure
;;                 "(do (require '[clojure.core.typed :as t]) (t/ann fun [Integer -> Integer]) (defn fun [x] x) (fun 1))" (symbol 'java.lang.String)))))


(deftest type-clojure-pass-test-1
  (test-type-correct
   "(do (require '[clojure.core.typed :as t]) (t/ann fun [Integer -> Integer]) (defn fun [x] x) (fun 1))"))

(deftest type-clojure-correct-test-1
  (test-type-equals
   "(do (require '[clojure.core.typed :as t]) (t/ann fun [Integer -> Integer]) (defn fun [x] x) (fun 1))"
   'java.lang.Iteger))

(deftest type-clojure-fail-test-1
  (test-type-correct
   "(do (require '[clojure.core.typed :as t]) (t/ann fun [Integer -> Integer]) (defn fun [x] x) (fun 1.0))"
   false))
