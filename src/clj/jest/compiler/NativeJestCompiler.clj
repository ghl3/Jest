(ns jest.compiler.NativeJestCompiler
  (:import (jest.grammar JestParser$SourceCodeContext JestParser$StatementTermContext JestParser$StatementContext JestParser$DefAssignmentContext)
           (jest.compiler ClojureSourceGenerator$BadSource))
  (:gen-class
    :extends jest.grammar.JestBaseVisitor
    :prefix "-"
    :main false))


(defn -visitSourceCode
  [this ^JestParser$SourceCodeContext ctx]
  (into [] (concat
             (map #(. this visitImportStatement %) (. ctx importStatement))
             (map #(. this visitStatementTerm %) (. ctx statementTerm)))))


(defn -visitImportStatement
  [this ^JestParser$StatementTermContext ctx]

  (let [to-import (clojure.string/join "."
                                       (map #(. % getText)
                                            (concat [(.. ctx a)] (into [] (.. ctx b)))))]
    `(import ~to-import)))


(defn -visitStatementTerm
  [this ^JestParser$StatementTermContext ctx]

  (cond
    (. ctx statement)   (.. this (visitStatement (. ctx statement)))
    (. ctx functionDef) (.. this (visitFunctionDef (. ctx functionDef)))
    (. ctx recordDef)   (.. this (visitRecordDef (. ctx recordDef)))
    (. ctx block)       (.. this (visitBlock (. ctx block)))
    (. ctx varScope)    (.. this (visitVarScope (. ctx varScope)))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitStatement
  [this ^JestParser$StatementContext ctx]

  (cond
    (. ctx expression) (.. this (visitExpression (. ctx expression)))
    (. ctx defAssignment) (.. this (visitDefAssignment (. ctx defAssignment)))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitDefAssignment
  [this ^JestParser$DefAssignmentContext ctx]

  (let [type (. ctx type)
        name (.. ctx name getText)
        expr (.. this (visitExpression (. ctx expression)))]

    (if type (println type))
    `(def ~name ~expr)))
