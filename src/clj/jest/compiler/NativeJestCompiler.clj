(ns jest.compiler.NativeJestCompiler
  (:import (jest.grammar JestParser$SourceCodeContext JestParser$StatementTermContext JestParser$StatementContext JestParser$DefAssignmentContext
                         JestParser$ExpressionContext JestParser$ComparisonExpressionContext JestParser$ArithmeticExpressionContext JestParser$ArithmeticTermContext JestParser$ExpressionComposedContext JestParser$MethodCallContext JestParser$MethodCallChainContext JestParser$ExpressionAtomContext)
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



(defn upper-first
  [string]
  (str (clojure.string/upper-case (get string 0)) (subs string 1)))

(defn lower-first
  [string]
  (str (clojure.string/lower-case (get string 0)) (subs string 1)))


(defmacro self-visit
  [name]
  `(.. ~'this (.. ~(symbol (str "visit" (upper-first name))) (. what ~(symbol (lower-first name))))))


(defn obj->seq
  "Convert an object into a sequence.
  If it is a sequence already, return the sequnce.
  If it's a java iterable, convert to sequence and return.
  Otherwise, create a list containing the single element
  and return that list"
  [obj]
  (cond
    (seq? obj) obj
    (instance? Iterable obj) (into [] obj)
    :else [obj]))


;; HELPER
(defn merge-items
  "Takes two items and joins them as a single sequence."
  [left right]
  (concat (obj->seq left) (obj->seq right)))


;; HELPER
(defn zip
  [& args]
  (apply map vector args))


(defn -visitImportStatement
  [this ^JestParser$StatementTermContext ctx]
  (let [to-import (clojure.string/join "."
                                       (map #(. % getText) (merge-items (.. ctx a) (.. ctx b))))]
    `(import ~(symbol to-import))))


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
        name (symbol (.. ctx name getText))
        expr (.. this (visitExpression (. ctx expression)))]

    (if type (println type))
    `(def ~name ~expr)))


(defn -visitExpression
  [this ^JestParser$ExpressionContext ctx]
  (.. this (visitComparisonExpression (. ctx comparisonExpression))))


(def comparison-override-map {"==", "="})


(defn- create-comparison-operation
  [ctx]
  (let [op-raw (. ctx op)
        op (get comparison-override-map op-raw op-raw)
        left (. ctx a)
        right (. ctx b)]
    '(op left right)))


(defn -visitComparisonExpression
  [this ^JestParser$ComparisonExpressionContext ctx]

  (if (. ctx op)
    (create-comparison-operation ctx)
    (.. this (visitArithmeticExpression (. ctx a)))))



;; TODO
(defn -visitArithmeticExpression
  [this ^JestParser$ArithmeticExpressionContext ctx]
  (reduce (fn [accum next]
            (let [[left right] next
                  term (.. this (visitArithmeticTerm (. right b)))]
              '(left accum term)))
          (. this (visitArithmeticTerm(. ctx a)))
          (zip (. ctx op) (. ctx b))))


;; TODO
(defn -visitArithmeticTerm
  [this ^JestParser$ArithmeticTermContext ctx]

  (reduce (fn [accum next]
            (let [[left right] next
                  term (.. this (visitExpressionComposed (. right b)))]
              '(left accum term)))
          (. this (visitExpressionComposed(. ctx a)))
          (zip (. ctx op) (. ctx b))))




(defn -visitExpressionComposed
  [this ^JestParser$ExpressionComposedContext ctx]

  (cond
    (. ctx methodCallChain) (.. this (visitMethodCallChain (. ctx methodCallChain)))
    (. ctx expressionAtom) (.. this (visitExpressionAtom (. ctx expressionAtom)))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitMethodCallChain
  [this ^JestParser$MethodCallChainContext ctx]

  (cond

    (. ctx methodCall) (.. this (visitMethodCall (. ctx methodCall)))

    (. ctx PERIOD) (let [a (.. ctx a getText)
                         b (.. this (visitMethodCallChain (. ctx methodCallChain)))
                         params (.. this (visitMethodParams (. ctx b)))]
                     (a b params))

    (. ctx ARROW) (let [a (.. ctx a getText)
                        b (.. this (visitMethodCallChain (. ctx methodCallChain)))
                        params (.. this (visitMethodParams (. ctx b)))]
                    (a params b))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitMethodCall
  [this ^JestParser$MethodCallContext ctx]

  (cond
    (. ctx PERIOD) (let [a (.. ctx func getText)
                         b (.. this (visitExpressionAtom (. ctx obj)))
                         params (.. this (visitMethodParams (. ctx methodParams)))]
                     (a b params))


    (. ctx ARROW) (let [a (.. ctx func getText)
                        b (.. this (visitExpressionAtom (. ctx obj)))
                        params (.. this (visitMethodParams (. ctx methodParams)))]
                    (b a params))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))



(defn -visitExpressionAtom
  [this ^JestParser$ExpressionAtomContext ctx]

  (cond
    (. ctx NUMBER) (symbol (.. ctx NUMBER getText))

    (. ctx TRUE) true ;;(.. ctx TRUE getText)

    (. ctx FALSE) false ;;(.. ctx FALSE getText)

    (. ctx NIL) nil ;;(.. ctx NIL getText)

    (. ctx ID) (symbol (.. ctx ID getText))

    (. ctx STRING) (.. ctx STRING getText)

    ;; TODO: Rename 'symbol' to 'keyword'
    (. ctx SYMBOL) (keyword (.. ctx SYMBOL getText))

    (. ctx clojureVector) (.. this (visitClojureVector (. ctx clojureVector)))

    ;
    ;                                }
    ;else if (ctx.clojureVector() != null) {
    ;                                       return this.visitClojureVector(ctx.clojureVector());
    ;                                       }
    ;else if (ctx.clojureMap() != null) {
    ;                                    return this.visitClojureMap(ctx.clojureMap());
    ;                                    }
    ;else if (ctx.functionCall() != null) {
    ;                                      return this.visitFunctionCall(ctx.functionCall());
    ;                                      }
    ;else if (ctx.clojureGet() != null) {
    ;                                    return this.visitClojureGet(ctx.clojureGet());
    ;                                    }
    ;else if (ctx.forLoop() != null) {
    ;                                 return this.visitForLoop(ctx.forLoop());
    ;                                 }
    ;else if (ctx.conditional() != null) {
    ;                                     return this.visitConditional(ctx.conditional());
    ;                                     }
    ;else if (ctx.lambda() != null) {
    ;                                return this.visitLambda(ctx.lambda());
    ;                                }
    ;else if (ctx.memberGetChain() != null) {
    ;                                        return this.visitMemberGetChain(ctx.memberGetChain());
    ;                                        }
    ;else if (ctx.recordConstructor() != null) {
    ;                                           return this.visitRecordConstructor(ctx.recordConstructor());
    ;                                           }
    ;else if (ctx.block() != null) {
    ;                               return this.visitBlock(ctx.block());
    ;                               }
    ;

    (. ctx expression) (.. this (visitExpression (. ctx expression)))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))
