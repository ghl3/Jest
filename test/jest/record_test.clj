(ns jest.record-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(deftest record-test-1
    (test-code
     "record Student{ name; class; }"
     ["(defrecord Student [name class])"]))


(deftest record-test-2
    (test-code-eval
     "record Student{ name; class; }
      val bob = new Student(\"Bob\", \"History\");
      bob.name;"
     ["(defrecord Student [name class])"
      "(def bob (->Student \"Bob\" \"History\"))"
      "(:name bob)"]
     "Bob"))
