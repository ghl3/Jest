(ns jest.record-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest record-test-1
    (test-code
     "record Student{ name; class; }"
     ["(defrecord Student [name class])"]))
