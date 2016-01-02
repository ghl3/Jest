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

  (let [jest-src-string "import Foo;\n import Bar;  let foo=12;"
        clj-form (jest->clojure jest-src-string)]
    (println clj-form)))