(ns jest.conditional-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest if-test-1
  (test-code-eval
   "if (true) {10};"
   ["(if true 10)"]
   10))

(deftest if-test-2
  (test-code-eval
   "if (nil) {10};"
   ["(if nil 10)"]
   nil))

(deftest if-test-3
  (test-code-eval
   "if (true) {10;} else {20;};"
   ["(if true 10 20)"]
   10))

(deftest if-test-4
  (test-code-eval
   "if (nil) {10} else {20};"
   ["(if nil 10 20)"]
   20))

(deftest if-test-4
  (test-code-eval
   "if (nil) {10} elif (true) {20};"
   ["(cond nil 10 true 20)"]
   20))

(deftest if-test-5
  (test-code-eval
   "if (nil) {10} elif (nil) {20} else {30};"
   ["(cond nil 10 nil 20 :else 30)"]
   30))

(deftest if-test-6
  (test-code-eval
   "if (nil) {10} elif (nil) {20} elif (true) {30} else {40};"
   ["(cond nil 10 nil 20 true 30 :else 40)"]
   30))
