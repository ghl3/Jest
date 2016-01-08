(ns jest.loop-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]))


(deftest for-test-1
  (test-code
   "for (a: lst) { println(a); };"
   ['(clojure.core/doall (clojure.core/map (clojure.core/fn [ a ] (println a)) (clojure.core/seq lst)))]))

(deftest for-test-2
  (test-code
   "for (a: lst) { println(a); a; };"
   ['(clojure.core/doall (clojure.core/map (clojure.core/fn [ a ] (println a) a) (clojure.core/seq lst)))]))

(deftest for-test-eval-2
  (test-code-eval
   "for (a: [1, 2, 3]) { a; };"
   ['(clojure.core/doall (clojure.core/map (clojure.core/fn [ a ] a) (clojure.core/seq [1, 2, 3])))]
   [1 2 3]))

(deftest for-test-3
  (test-code
   "for (x, y, z: lstX, lstY, lstZ) { println(x+y+z); z; };"
   ['(clojure.core/doall (clojure.core/map (clojure.core/fn [ x y z ] (println (+ (+ x y) z)) z) (clojure.core/seq lstX) (clojure.core/seq lstY) (clojure.core/seq lstZ)))]))

(deftest for-test-eval-3
  (test-code-eval
   "for (x, y, z: [1, 2, 3], [4, 5, 6], [7, 8, 9]) { x+y+z; };"
   ['(clojure.core/doall (clojure.core/map (clojure.core/fn [ x y z ] (+ (+ x y) z)) (clojure.core/seq [1, 2, 3]) (clojure.core/seq [4, 5, 6]) (clojure.core/seq [7, 8, 9])))]
   [12, 15, 18]))


(deftest for-lazy-test-1
  (test-code
   "for (a: lst) lazy { println(a); };"
   ['(clojure.core/map (clojure.core/fn [ a ] (println a)) (clojure.core/seq lst))]))


;; TODO: Uncomment and fix
;; (deftest for-loop-bug-1
;;   (test-code
;;    "def a = 10; for (b: a) {def z = 10; z;} a;"
;;    ["(def a 10)"
;;     "(doall (map (fn [b] (def z 10) z) (seq a)))"
;;     "a"]))
