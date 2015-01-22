(ns jest.type-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]
            [clojure.core.typed :as t]
            [jest.parser :refer [get-clojure]]))

(deftest type-val-test-1
  (test-code
   "val x : String = \"Foobar\";"
   ["(t/ann x String)
     (def x \"Foobar\")"]))

(deftest type-val-check-test-1
  (test-jest-type-correct
   "val x : String = \"Foobar\";"))


(deftest type-func-test-1
  (test-code
   "defn func(x): String -> String {
         x;
    };"
   ["(t/ann func [String -> String])
     (defn func [ x ] x)"]))

(deftest type-func-check-test-1
  (test-jest-type-correct
   "defn func(x): String -> String {
         x;
    };"))


(deftest type-func-test-2
  (test-code
   "val x: List[String] = [\"FooBar\"];"
   ["(t/ann x (List String)) (def x [\"FooBar\"])"]))

(deftest type-container-test-1
  (test-code
   "defn func(x, y): String String -> String {
         x + y;
    };"
   ["(t/ann func [String String -> String])
     (defn func [ x y ] (+ x y))"]))


(deftest type-clojure-pass-test-1
  (test-clojure-type-correct
   "(do (require '[clojure.core.typed :as t]) (t/ann fun [Integer -> Integer]) (defn fun [x] x) (fun 1))"))

(deftest type-clojure-fail-test-1
  (test-clojure-type-correct
   "(do (require '[clojure.core.typed :as t]) (t/ann fun [Integer -> Integer]) (defn fun [x] x) (fun 1.0))"
   false))

(deftest type-clojure-correct-test-1
  (test-clojure-type-equals
   "(do (require '[clojure.core.typed :as t]) (t/ann fun [Integer -> Integer]) (defn fun [x] x) (fun 1))"
   'java.lang.Integer))

(deftest type-clojure-incorrect-test-1
  (test-clojure-type-equals
   "(do (require '[clojure.core.typed :as t]) (t/ann fun [Integer -> Integer]) (defn fun [x] x) (fun 1))"
   'java.lang.String false))

(deftest type-clojure-pass-test-1
  (test-clojure-type-correct
   (format "(do (require '[clojure.core.typed :as t]) %s)"
           (get-clojure
            "val x : String = \"Foobar\";"))))



