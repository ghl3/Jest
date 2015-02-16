(ns jest.loop-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest for-test-1
  (test-code
   "for (a: lst) { println(a); };"
   ["(doall (map (fn [ a ] (println a)) (seq lst)))"]))

(deftest for-test-2
  (test-code
   "for (a: lst) { println(a); a; };"
   ["(doall (map (fn [ a ] (println a) a) (seq lst)))"]))

(deftest for-test-eval-2
  (test-code-eval
   "for (a: [1, 2, 3]) { a; };"
   ["(doall (map (fn [ a ] a) (seq [1, 2, 3])))"]
   [1 2 3]))

(deftest for-test-3
  (test-code
   "for (x, y, z: lstX, lstY, lstZ) { println(x+y+z); z; };"
   ["(doall (map (fn [ x y z ] (println (+ (+ x y) z)) z) (seq lstX) (seq lstY) (seq lstZ)))"]))

(deftest for-test-eval-3
  (test-code-eval
   "for (x, y, z: [1, 2, 3], [4, 5, 6], [7, 8, 9]) { x+y+z; };"
   ["(doall (map (fn [ x y z ] (+ (+ x y) z)) (seq [1, 2, 3]) (seq [4, 5, 6]) (seq [7, 8, 9])))"]
   [12, 15, 18]))


(deftest for-lazy-test-1
  (test-code
   "for (a: lst) lazy { println(a); };"
   ["(map (fn [ a ] (println a)) (seq lst))"]))


;; TODO: Uncomment and fix
;; (deftest for-loop-bug-1
;;   (test-code
;;    "val a = 10; for (b: a) {val z = 10; z;} a;"
;;    ["(def a 10)"
;;     "(doall (map (fn [b] (def z 10) z) (seq a)))"
;;     "a"]))
