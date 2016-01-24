(ns jest.types.generic-type-checking
  (:require [clojure.test :refer :all]
            [jest.utils :refer :all])
  (:import (jest.compiler Types$GenericParameter Core$PrimitiveType Generics Types$GenericFunctionDeclaration Types$GenericType)))


(deftest generic-valid-test-1

  (let [T (new Types$GenericParameter "T")
        sig (new Types$GenericFunctionDeclaration "Foobar"
                 [T]
                 ["x" "y"]
                 [T T]
                 (new Types$GenericParameter "T"))
        usageTypes [Core$PrimitiveType/String Core$PrimitiveType/String]]

    (is (not (.. (Generics/checkGenericFunctionCall sig usageTypes) isPresent)))))


(deftest generic-invalid-test-1

  (let [T (new Types$GenericParameter "T")
        sig (new Types$GenericFunctionDeclaration "Foobar"
                 [T]
                 ["x" "y"]
                 [T T]
                 (new Types$GenericParameter "T"))
        usageTypes [Core$PrimitiveType/String Core$PrimitiveType/Number]]

    (is (.. (Generics/checkGenericFunctionCall sig usageTypes) isPresent))))


(deftest generic-valid-test-2

  (let [T (new Types$GenericParameter "T")
        U (new Types$GenericParameter "U")
        map->T->U (new Types$GenericType "Map" [(new Types$GenericParameter "T") (new Types$GenericParameter "U")])
        sig (new Types$GenericFunctionDeclaration "Foobar"
                 [T U] ["mapper" "val"] [map->T->U T] U)

        map->Number->String (new Types$GenericType "Map" [Core$PrimitiveType/Number Core$PrimitiveType/String])
        usageTypes [map->Number->String Core$PrimitiveType/Number]]

    (is (not (.. (Generics/checkGenericFunctionCall sig usageTypes) isPresent)))))


(deftest generic-invalid-test-2

  (let [T (new Types$GenericParameter "T")
        U (new Types$GenericParameter "U")
        map->T->U (new Types$GenericType "Map" [(new Types$GenericParameter "T") (new Types$GenericParameter "U")])
        sig (new Types$GenericFunctionDeclaration "Foobar"
                 [T U] ["mapper" "val"] [map->T->U T] U)

        map->Number->String (new Types$GenericType "Map" [Core$PrimitiveType/Number Core$PrimitiveType/String])
        usageTypes [map->Number->String Core$PrimitiveType/String]]

    (is (.. (Generics/checkGenericFunctionCall sig usageTypes) isPresent))))