(ns jest.symbol-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest map-test-1
  (test-code
   "val sym = :mySymbol;"
   ["(def sym :mySymbol)"]))

