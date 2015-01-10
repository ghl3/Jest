(ns jest.grammar-test
  (:require [clojure.test :refer :all]))

(import 'jest.grammar.S001HelloWordParser)
(import 'jest.grammar.S001HelloWordCompiler)

(deftest a-test
  (let [ast (. S001HelloWordCompiler (compile "Hello world!"))]
    (is (.. ast (getChild 0) getType) (. S001HelloWordParser SALUTATION))
    (is (.. ast (getChild 1) getType) (. S001HelloWordParser ENDSYMBOL))
    ))

;; CommonTree leftChild = ast.getChild(0);
;; 16
;;   CommonTree rightChild = ast.getChild(1);
;; 17
 
;; 18
;;   //check ast structure
;; 19
;;   assertEquals(S001HelloWordParser.SALUTATION, leftChild.getType());
;; 20
;;   assertEquals(S001HelloWordParser.ENDSYMBOL, rightChild.getType());



;;     (is (= ast
;;     ))
;; (testing "FIXME, I fail."
;;   (is (= 0 1))))
