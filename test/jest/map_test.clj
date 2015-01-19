(ns jest.map-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest map-test-1
  (test-code
   "val mp = {a:1};"
   ["(def mp {a 1})"]))

(deftest map-test-2
  (test-code
   "val mp = {a:1, b:2};"
   ["(def mp {a 1 b 2})"]))

(deftest map-access-1
  (test-code-eval
   "val mp = {\"a\":1, \"b\":2}; mp[\"a\"];"
   ["(def mp {\"a\" 1 \"b\" 2})" "(get mp \"a\")"]
   1))

(deftest map-get-1
  (test-code-eval
   "val mp = {\"a\":1, \"b\":2}; mp.get(\"a\");"
   ["(def mp {\"a\" 1 \"b\" 2})" "(get mp \"a\")"]
   1))


(deftest map-test-3
  (test-code-eval
   "val mp = {:a : 1, :b : 2}; mp[:a];"
   ["(def mp {:a 1 :b 2})" "(get mp :a)"]
   1))
