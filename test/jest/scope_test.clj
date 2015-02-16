
(ns jest.scope-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(import 'jest.compiler.JestCompiler)


(defn validate-code
  "Ensures that the code is valid"
  ([src] (validate-code src true))
  ([src should-be-valid?]
  (let [is-valid? (. JestCompiler validateAst src)]
    (if should-be-valid?
      (is is-valid?)
      (is (not is-valid?))))))

(deftest scope-test-1
  (validate-code
   "val a = 10; defn foo() { val a = 20; a; };"))

(deftest scope-test-2
  (validate-code
   "val a = 10; defn foo() { val a = 20; a; } val a = 20;"
   false))

(deftest scope-test-3
  (validate-code
   "val a = 10; defn foo() { val a = 20; a; } b;"
   false))


(deftest scope-loop-test-1
  (validate-code
   "val a = 10; for (b: a) {val z = 10; z;}; a;"))

(deftest scope-loop-test-2
  (validate-code
   "val a = 10; for (b: a) {val z = 10; z;}; z;"
   false))
