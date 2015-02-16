
(ns jest.scope-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))

(import 'jest.compiler.JestCompiler)


(defn validate-code
  "Ensures that the code is valid"
  ([src] (validate-code src true))
  ([src should-be-valid?]
  (let [is-valid? (. JestCompiler validateSourceCode src)]
    (if should-be-valid?
      (is is-valid?)
      (is (not is-valid?))))))


(deftest scope-func-def-test-1
  (validate-code
   "val a = 10; defn foo() { val a = 20; a; };"))

(deftest scope-func-def-test-2
  (validate-code
   "val a = 10; defn foo() { val a = 20; a; } val a = 20;" false))

(deftest scope-func-def-test-3
  (validate-code
   "val a = 10; defn foo() { val a = 20; a; } b;" false))

(deftest scope-func-def-test-4
  (validate-code
   "val a = 10; defn foo(x, y, z) { x+y+z; } a;"))

(deftest scope-func-def-test-5
  (validate-code
   "val a = 10; defn foo(x, y, z) { x+y+z; } x;" false))


(deftest scope-loop-test-1
  (validate-code
   "val a = 10; for (b: a) {val z = 10; z;}; a;"))

(deftest scope-loop-test-2
  (validate-code
   "val a = 10; for (b: a) {val z = 10; z;}; z;" false))

(deftest scope-loop-test-3
  (validate-code
   "val a = 10; for (b: a) {b;}; a;"))


(deftest scope-conditional-test-1
  (validate-code
   "if (true) { val a = 10; a; } else {val b = 20; b;};"))

(deftest scope-conditional-test-2
  (validate-code
   "if (true) { val a = 10; a; } else {a;};" false))

(deftest scope-conditional-test-3
  (validate-code
   "if (true) { val a = 10; a; } else {val b = 20; b;}; a;" false))


(deftest scope-block-test-1
  (validate-code
   "val a = 10; {val a = 20;} a;"))

(deftest scope-block-test-2
  (validate-code
   "val a = 10; {val b = 20;} b;" false))


