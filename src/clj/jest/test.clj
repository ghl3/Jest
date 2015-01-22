(ns jest.test
  (:require [clojure.core.typed :as t]
            [clojure.string :as str]))


(t/ann x String)
(def x "Foobar")

(t/ann y Double)
(def y 6.0)


(t/ann func-1 [String -> String])
(defn func-1 [s]
  s)


(t/ann func-2 [String -> Double])
(defn func-2 [s]
  2.0)


(t/ann func-3 [Double -> Double])
(defn func-3 [d]
  d)


(func-1 x)

(func-3 y)
