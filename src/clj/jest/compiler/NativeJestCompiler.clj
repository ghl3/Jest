(ns jest.compiler.NativeJestCompiler
  (:import (jest.grammar JestParser$SourceCodeContext JestParser$StatementTermContext
                         JestParser$StatementContext JestParser$DefAssignmentContext
                         JestParser$ExpressionContext JestParser$ComparisonExpressionContext
                         JestParser$ArithmeticExpressionContext JestParser$ArithmeticTermContext
                         JestParser$ExpressionComposedContext JestParser$MethodCallContext
                         JestParser$MethodCallChainContext JestParser$ExpressionAtomContext
                         JestParser$MemberGetChainContext JestParser$MemberGetContext
                         JestParser$RecordConstructorContext JestParser$ExpressionListContext
                         JestParser$TypeAnnotationContext JestParser$FuncTypeAnnotationContext
                         JestParser$LambdaContext JestParser$FunctionDefContext JestParser$MethodDefContext
                         JestParser$FunctionDefParamsContext JestParser$FunctionCallContext
                         JestParser$RecordDefContext JestParser$ImplementationDefContext
                         JestParser$MethodParamsContext JestParser$ForLoopContext
                         JestParser$BlockContext JestParser$VarScopeContext JestParser$ConditionalContext
                         JestParser$ClojureVectorContext JestParser$ClojureMapContext)
           (jest.compiler ClojureSourceGenerator$BadSource)
           (sun.reflect.generics.reflectiveObjects NotImplementedException))
  (:gen-class
    :extends jest.grammar.JestBaseVisitor
    :prefix "-"
    :main false))


(defn upper-first
  [string]
  (str (clojure.string/upper-case (get string 0)) (subs string 1)))


(defn lower-first
  [string]
  (str (clojure.string/lower-case (get string 0)) (subs string 1)))


(defmacro self-visit
  "Compiles this:
  (self-visit this ctx name)
  to the following:
  (. this (visitName (. ctx name)))
  "
  [this ctx name]
  `(. ~this
      (~(symbol (str "visit" (upper-first (str name))))
       (. ~ctx ~(symbol (lower-first (str name)))))))


(defmacro get-symbol
  [ctx variable]
  `(symbol (.. ~ctx ~variable getText)))


(defn obj->seq
  "Convert an object into a sequence.
  If it is a sequence already, return the sequence.
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


(defn -visitSourceCode
  [this ^JestParser$SourceCodeContext ctx]
  (into [] (concat
             (map #(. this visitImportStatement %) (. ctx importStatement))
             (map #(. this visitStatementTerm %) (. ctx statementTerm)))))


(defn -visitImportStatement
  [this ^JestParser$StatementTermContext ctx]
  (let [to-import (clojure.string/join "."
                                       (map #(. % getText) (merge-items (.. ctx a) (.. ctx b))))]
    `(import ~(symbol to-import))))


(defn -visitStatementTerm
  [this ^JestParser$StatementTermContext ctx]

  (cond
    (. ctx statement)   (self-visit this ctx statement) ;;(.. this (visitStatement (. ctx statement)))
    (. ctx functionDef) (self-visit this ctx functionDef) ;;(.. this (visitFunctionDef (. ctx functionDef)))
    (. ctx recordDef)   (self-visit this ctx recordDef)  ;;(.. this (visitRecordDef (. ctx recordDef)))
    (. ctx block)       (self-visit this ctx block) ;;(.. this (visitBlock (. ctx block)))
    (. ctx varScope)    (self-visit this ctx varScope) ;;(.. this (visitVarScope (. ctx varScope)))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitStatement
  [this ^JestParser$StatementContext ctx]

  (cond
    (. ctx expression)    (self-visit this ctx expression) ;;(.. this (visitExpression (. ctx expression)))
    (. ctx defAssignment) (self-visit this ctx defAssignment) ;;(.. this (visitDefAssignment (. ctx defAssignment)))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitDefAssignment
  [this ^JestParser$DefAssignmentContext ctx]

  (let [type (. ctx type)
        name (get-symbol ctx name) ;;(symbol (.. ctx name getText))
        expr (self-visit this ctx expression)] ;;(.. this (visitExpression (. ctx expression)))]

    (if type (println type))
    `(def ~name ~expr)))


(defn -visitExpression
  [this ^JestParser$ExpressionContext ctx]
  (self-visit this ctx comparisonExpression))
  ;;(.. this (visitComparisonExpression (. ctx comparisonExpression))))


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


(defn -visitArithmeticExpression
  [this ^JestParser$ArithmeticExpressionContext ctx]
  (reduce (fn [accum next]
            (let [[left right] next
                  op (symbol (. left getText))
                  term (.. this (visitArithmeticTerm right))]
              `(~op ~accum ~term)
              ))
          (. this (visitArithmeticTerm(. ctx a)))
          (zip (. ctx op) (. ctx b))))


(defn -visitArithmeticTerm
  [this ^JestParser$ArithmeticTermContext ctx]

  (reduce (fn [accum next]
            (let [[left right] next
                  op (symbol (. left getText))
                  term (.. this (visitExpressionComposed right))]
              `(~op ~accum ~term)))
          (. this (visitExpressionComposed (. ctx a)))
          (zip (. ctx op) (. ctx b))))


(defn -visitExpressionComposed
  [this ^JestParser$ExpressionComposedContext ctx]

  (cond
    (. ctx methodCallChain) (self-visit this ctx methodCallChain)
    (. ctx expressionAtom) (self-visit this ctx expressionAtom)

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitMethodCallChain
  [this ^JestParser$MethodCallChainContext ctx]

  (cond

    (. ctx methodCall) (self-visit this ctx methodCall)

    (. ctx PERIOD) (let [a (.. ctx a getText)
                         b (self-visit this ctx methodCallChain)
                         params (.. this (visitMethodParams (. ctx b)))]
                     `(~a ~b ~@params))

    (. ctx ARROW) (let [a (.. ctx a getText)
                        b (self-visit this ctx methodCallChain)
                        params (.. this (visitMethodParams (. ctx b)))]
                    `(~a ~@params ~b))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitMethodCall
  [this ^JestParser$MethodCallContext ctx]

  (cond
    (. ctx PERIOD) (let [a (get-symbol ctx func)
                         b (.. this (visitExpressionAtom (. ctx obj)))
                         params (self-visit this ctx methodParams)]
                     `(~a ~b ~@params))


    (. ctx ARROW) (let [a (get-symbol ctx func)
                        b (.. this (visitExpressionAtom (. ctx obj)))
                        params (self-visit this ctx methodParams)]
                    `(~b ~a ~@params))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitExpressionAtom
  [this ^JestParser$ExpressionAtomContext ctx]

  (cond
    (. ctx NUMBER) (read-string (.. ctx NUMBER getText))

    (. ctx TRUE) true

    (. ctx FALSE) false

    (. ctx NIL) nil

    (. ctx ID) (symbol (.. ctx ID getText))

    (. ctx STRING) (.. ctx STRING getText)

    ;; TODO: Rename 'symbol' to 'keyword'
    (. ctx SYMBOL) (keyword (.. ctx SYMBOL getText))

    (. ctx clojureVector) (self-visit this ctx clojureVector)

    (. ctx clojureMap) (self-visit this ctx clojureMap)

    (. ctx functionCall) (self-visit this ctx functionCall)

    (. ctx clojureGet) (self-visit this ctx clojureGet)

    (. ctx forLoop) (self-visit this ctx forLoop)

    (. ctx conditional) (self-visit this ctx conditional)

    (. ctx lambda) (self-visit this ctx lambda)

    (. ctx memberGetChain) (self-visit this ctx memberGetChain)

    (. ctx recordConstructor) (self-visit this ctx recordConstructor)

    (. ctx block) (self-visit this ctx block)

    (. ctx expression) (self-visit this ctx expression)

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitMemberGetChain
  [this ^JestParser$MemberGetChainContext ctx]
  (if (. ctx PERIOD)
    (reduce (fn [accum next] `(~(keyword (. next getText)) ~accum))
            (self-visit this ctx memberGet)
            (. ctx a))
    (self-visit this ctx memberGet)))


(defn -visitMemberGet
  [this ^JestParser$MemberGetContext ctx]
  ;; TODO: Are these really "getText" calls?
  `(~(keyword (.. ctx member getText)) ~(symbol (.. ctx record getText))))


(defn -visitRecordConstructor
  [this ^JestParser$RecordConstructorContext ctx]
  (throw (new NotImplementedException)))


(defn -visitExpressionList
  "Take a parsed list of expressions and return
  a list of the expressions"
  [this ^JestParser$ExpressionListContext ctx]
  (into [] (map #(. this (visitExpression %)) (merge-items (. ctx a) (. ctx b)))))


(defn -visitTypeAnnotation
  [this ^JestParser$TypeAnnotationContext ctx]
  (throw (new NotImplementedException)))


(defn -visitFuncTypeAnnotation
  [this ^JestParser$FuncTypeAnnotationContext ctx]
  (throw (new NotImplementedException)))


(defn -visitLambda
  [this ^JestParser$LambdaContext ctx]
  (throw (new NotImplementedException)))


(defn -visitFunctionDef
  [this ^JestParser$FunctionDefContext ctx]

  ;if (ctx.funcTypeAnnotation() != null) {
  ;                                       code += String.format("(t/ann %s [%s -> %s])\n",
  ;                                                              ctx.name.getText(),
  ;                                                              this.visitFuncTypeAnnotation(ctx.funcTypeAnnotation()).getSingleLine(),
  ;                                                              this.visitTypeAnnotation(ctx.typeAnnotation()).getSingleLine());
  ;                                       }

  `(defn
     ~(get-symbol ctx name) ;;.. ctx name getText)
     [~@(self-visit this ctx functionDefParams)]
     ~@(self-visit this ctx block)))


(defn -visitMethodDef
  [this ^JestParser$MethodDefContext ctx]
  (throw (new NotImplementedException)))


(defn -visitFunctionDefParams
  [this ^JestParser$FunctionDefParamsContext ctx]
  (if (. ctx first)
    (into [] (map #(symbol (. % getText)) (merge-items (. ctx first) (. ctx rest))))
    '()))


(defn -visitFunctionCall
  [this ^JestParser$FunctionCallContext ctx]
  `(~(get-symbol ctx ID) ~@(.. this (visitMethodParams (. ctx methodParams)))))


(defn -visitRecordDef
  [this ^JestParser$RecordDefContext ctx]
  (throw (new NotImplementedException)))


(defn -visitImplementationDef
  [this ^JestParser$ImplementationDefContext ctx]
  (throw (new NotImplementedException)))


(defn -visitMethodParams
  "Takes a parsed set of method parameters
  and return a list of those parameters
  as clojure objects"
  [this ^JestParser$MethodParamsContext ctx]

  (cond
    (. ctx expressionList) (self-visit this ctx expressionList)
    (. ctx expression)     [(self-visit this ctx expression)]
    :else '()))


(defn- make-seqable
  [x]
  `(seq ~x))

(defn -visitForLoop
  [this ^JestParser$ForLoopContext ctx]

  (let [func-args (into [] (map #(symbol (. % getText)) (merge-items (. ctx a) (. ctx b))))
        func `(fn [~@func-args] ~@(self-visit this ctx block))
        seq-items (map #(.. this (visitExpression %)) (merge-items (. ctx c) (. ctx d)))
        iterator (into [] (map make-seqable seq-items))]

    (if (. ctx LAZY)
      `(map ~func ~@iterator)
      `(doall (map ~func ~@iterator)))))


(defn -visitBlock
  [this ^JestParser$BlockContext ctx]

  (cond
    (. ctx expression) (self-visit this ctx expression)

    (. ctx term) (into [] (map #(.. this (visitStatementTerm %)) (. ctx term)))

    (. ctx scope)  (map #(.. this (visitVarScope %)) (. ctx scope))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitVarScope
  [this ^JestParser$VarScopeContext ctx]
  (let [names (into [] (map #(symbol (. % getText)) (. ctx name)))
        vals (into [] (map #(.. this (visitExpression %)) (. ctx exp)))
        bindings (flatten (zip names vals))
        expressions (into [] (map #(. this (visitStatementTerm %)) (. ctx terms)))]
    `(let [~@bindings] ~@expressions)))


(defn -visitConditional
  [this ^JestParser$ConditionalContext ctx]
  (throw (new NotImplementedException)))


(defn -visitClojureVector
  [this ^JestParser$ClojureVectorContext ctx]
  (if (. ctx a)
    (into [] (map #(.. this (visitExpression %)) (merge-items (. ctx a) (. ctx b))))
    []))


(defn- extract-expression-pairs
  [this pair]
  (let [[left right] pair]
    [(.. this (visitExpression left))
     (.. this (visitExpression right))]))


(defn -visitClojureMap
  [this ^JestParser$ClojureMapContext ctx]
  (if (. ctx a)
    (let [kv-pairs (zip (merge-items (. ctx a) (. ctx c)) (merge-items (. ctx b) (. ctx d)))]
      (into (array-map) (map #(extract-expression-pairs this %) kv-pairs)))
    {}))


(defn -visitClojureGet
  [this JestParser$ClojureGetContext ctx]
  `(get ~(.. ctx a getText) ~(.. this (visitExpression (. ctx b)))))