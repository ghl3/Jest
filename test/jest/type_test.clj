(ns jest.type-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]
            [clojure.core.typed :as t]
            [jest.parser :refer [get-clojure]]))

(deftest type-val-test-1
  (test-code
   "def x : String = \"Foobar\";"
   ["(t/ann x String)
     (def x \"Foobar\")"]))

(deftest type-val-check-test-1
  (test-jest-type-correct
   "def x : String = \"Foobar\";"))


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
   "defn func(x, y): String String -> String {
         x + y;
    };"
   ["(t/ann func [String String -> String])
     (defn func [ x y ] (+ x y))"]))


(deftest type-func-test-3
  (test-code
   "defn addScores(scores): Vec[Integer] -> #AnyInteger {
     scores.get(0,0) + scores.get(1, 1) + scores.get(2, 2) + scores.get(3, 3);
};"
   ["(t/ann addScores [(t/Vec Integer) -> t/AnyInteger])
     (defn addScores [ scores ] (+ (+ (+ (get scores 0 0) (get scores 1 1)) (get scores 2 2)) (get scores 3 3)))"]))

(deftest type-container-test-1
  (test-code
   "def x: Vec[String] = [\"FooBar\"];"
   ["(t/ann x (t/Vec String)) (def x [\"FooBar\"])"]))

(deftest type-container-test-2
  (test-code
   "def x: Map[String String] = {\"Foo\" : \"Bar\"};"
   ["(t/ann x (t/Map String String)) (def x {\"Foo\" \"Bar\"})"]))

(deftest type-container-test-3
  (test-code
   "def scores: HVec[[(Integer 1) (Integer 2) (Integer 3) (Integer 4)]] = [90, 85, 95, 92];"
   ["(t/ann scores (t/HVec [ (Integer 1) (Integer 2) (Integer 3) (Integer 4)])) (def scores [90, 85, 95, 92])"]))





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
            "def x : String = \"Foobar\";"))))



