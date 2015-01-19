(ns jest.type-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

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

