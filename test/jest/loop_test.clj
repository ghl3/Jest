(ns jest.loop-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest for-test-1
  (test-code
   "for (a: lst) { println(a); };"
   ["(doall (map (fn [ a ] (println a)) lst))"]))

(deftest for-test-2
  (test-code
   "for (a: lst) { println(a); a; };"
   ["(doall (map (fn [ a ] (println a) a) lst))"]))

(deftest for-test-3
  (test-code
   "for (x y z: lstX, lstY, lstZ) { println(x+y+z); z; };"
   ["(doall (map (fn [ x y z ] (println (+ (+ x y) z)) z) lstX lstY lstZ))"]))
