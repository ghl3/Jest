
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


(deftest scope-func-def-test-1
  (validate-code
   "val a = 10; defn foo() { val a = 20; a; };"))

(deftest scope-func-def-test-2
  (validate-code
   "val a = 10; defn foo() { val a = 20; a; } val a = 20;"
   false))

(deftest scope-func-def-test-3
  (validate-code
   "val a = 10; defn foo() { val a = 20; a; } b;"
   false))

(deftest scope-func-def-test-4
  (validate-code
   "val a = 10; defn foo(x, y, z) { x+y+z; } a;"))


(deftest scope-loop-test-1
  (validate-code
   "val a = 10; for (b: a) {val z = 10; z;}; a;"))

(deftest scope-loop-test-2
  (validate-code
   "val a = 10; for (b: a) {val z = 10; z;}; z;"
   false))

(deftest scope-loop-test-3
  (validate-code
   "val a = 10; for (b: a) {b;}; a;"))
