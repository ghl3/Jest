(ns jest.parser-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]
            [jest.parser :refer :all]))


(deftest parse-expression-test-1
  (let [expression "10+5"]
    (is (= (parse-expression expression) "(+ 10 5)"))))

