(ns jest.native.native-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all])
  (:import (jest.compiler NativeJestCompiler JestCompiler)))


(defn jest->clojure
  "Parse jest source code (as a string)
  into a clojure form"
  [jest-src-str]

  (let [tree (. JestCompiler (compileSourceCodeToParseTree jest-src-str))
        parser (new NativeJestCompiler)]
    (.. parser (visit tree))))


(deftest native-test-1

  (let [jest-src-string "import foo.Bar; import foo.Baz; def foo=12;"
        clj-form (jest->clojure jest-src-string)]

    (is (= (get clj-form 0) '(clojure.core/import foo.Bar)))
    (is (= (get clj-form 1) '(clojure.core/import foo.Baz)))
    (is (= (get clj-form 2) '(def foo 12)))

    (is (= clj-form
           ['(clojure.core/import foo.Bar) '(clojure.core/import foo.Baz) '(def foo 12)]))))