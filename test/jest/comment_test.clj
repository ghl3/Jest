(ns jest.comment-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest comment-test-1
  "// COMMENT
  10"
  ["10"]
  10)


(deftest comment-test-2
  "/* COMMENT
  OTHER COMMENTS
  */
  10"
  ["10"]
  10)

(deftest comment-test-3
  "/*
  COMMENT
  OTHER COMMENTS
  STUFF */
  10"
  ["10"]
  10)

(deftest comment-test-4
  "10
  /*
  COMMENT
  OTHER COMMENTS
  STUFF */
  "
  ["10"]
  10)

(deftest comment-test-5
  "10
  /* /*
  // COMMENT
  OTHER COMMENTS
  STUFF */
  "
  ["10"]
  10)
