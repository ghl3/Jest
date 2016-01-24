(ns jest.types.inference-test
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all]
            [jest.parser :refer :all])
  (:import (jest  Utils$Pair)
           (jest.compiler Types$GenericParameter Core$PrimitiveType TypeInference)))


(deftest test-inference-1

  ;; [T]
  ;; [Number]

  (let [T (new Types$GenericParameter "Foobar" "T")
        constraints [(new Utils$Pair T Core$PrimitiveType/Number)]]
    (is (TypeInference/hasGenericsSolution constraints))))


(deftest test-inference-2

  ;; [T T]
  ;; [Number U]

  (let [T (new Types$GenericParameter "Foobar" "T")
        U (new Types$GenericParameter "Foobar" "U")
        constraints [
                     (new Utils$Pair T Core$PrimitiveType/Number)
                     (new Utils$Pair T U)
                     ]]
    (is (TypeInference/hasGenericsSolution constraints))))