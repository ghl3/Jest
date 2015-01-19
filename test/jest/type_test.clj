(ns jest.type-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))



(deftest type-val-test-0
  (test-code
   "val x : String = \"Foobar\";"
   ["(t/ann x String)
     (def x \"Foobar\")"]))


(deftest type-func-test-0
  (test-code
   "defn func(x): String -> String {
         x;
    };"
   ["(t/ann func [String -> String])
     (defn func [ x ] x)"]))

