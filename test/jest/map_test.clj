(ns jest.map-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest map-test-1
  (test-code
   "val map = {a:1};"
   ["(def map {a 1})"]))

(deftest map-test-2
  (test-code
   "val map = {a:1, b:2};"
   ["(def map {a 1 b 2})"]))

(deftest map-access-1
  (test-code-eval
   "val map = {\"a\":1, \"b\":2}; map[\"a\"];"
   ["(def map {\"a\" 1 \"b\" 2})" "(get map \"a\")"]
   1))

(deftest map-get-1
  (test-code-eval
   "val map = {\"a\":1, \"b\":2}; map.get(\"a\");"
   ["(def map {\"a\" 1 \"b\" 2})" "(get map \"a\")"]
   1))
