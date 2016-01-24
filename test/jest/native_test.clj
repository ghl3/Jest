(ns jest.native-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]
            [jest.parser :refer [jest->clojure]]))



(deftest native-test-1

  (let [jest-src-string "import foo.Bar; import foo.Baz; def foo=12;"
        clj-form (jest->clojure jest-src-string)]

    (is (= (get clj-form 0) '(clojure.core/import foo.Bar)))
    (is (= (get clj-form 1) '(clojure.core/import foo.Baz)))
    (is (= (get clj-form 2) '(def foo 12)))

    (is (= clj-form
           ['(clojure.core/import foo.Bar) '(clojure.core/import foo.Baz) '(def foo 12)]))))


(deftest native-test-2

  (let [jest-src-string "1 + 1;"
        clj-form (jest->clojure jest-src-string)]

    (is (= clj-form ['(+ 1 1)]))))
