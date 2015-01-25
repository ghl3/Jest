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

(deftest record-test-3
    (test-code-eval
     "record Student{ name; class; }
      record CollegeClass{ time; day; }
      val history = new CollegeClass(\"Noon\", \"Wednesday\");
      val bob = new Student(\"Bob\", history);
      bob.class.time;"
     ["(defrecord Student [name class])"
      "(defrecord CollegeClass [time day])"
      "(def history (->CollegeClass \"Noon\" \"Wednesday\"))"
      "(def bob (->Student \"Bob\" history))"
      "(:time (:class bob))"]
     "Noon"))