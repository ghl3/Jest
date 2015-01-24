(ns jest.comment-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest comment-test-1
  (test-code-eval
  "// COMMENT
  10;"
  ["10"]
  10))


(deftest comment-test-2
  (test-code-eval
  "/* COMMENT
  OTHER COMMENTS
  */
  10;"
  ["10"]
  10))


(deftest comment-test-3
  (test-code-eval
  "/*
  COMMENT
  OTHER COMMENTS
  STUFF */
  10;"
  ["10"]
  10))


(deftest comment-test-4
  (test-code-eval
  "10
  /*
  COMMENT
  OTHER COMMENTS
  STUFF */
  ;"
  ["10"]
  10))


(deftest comment-test-5
  (test-code-eval
  "10;
  /* /*
  // COMMENT
  OTHER COMMENTS
  STUFF */
  "
  ["10"]
  10))
