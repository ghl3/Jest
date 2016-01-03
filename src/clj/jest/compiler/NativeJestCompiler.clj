(ns jest.compiler.NativeJestCompiler
  (:import (jest.grammar JestParser$SourceCodeContext JestParser$StatementTermContext JestParser$StatementContext JestParser$DefAssignmentContext
                         JestParser$ExpressionContext JestParser$ComparisonExpressionContext JestParser$ArithmeticExpressionContext JestParser$ArithmeticTermContext JestParser$ExpressionComposedContext JestParser$MethodCallContext JestParser$MethodCallChainContext JestParser$ExpressionAtomContext JestParser$MemberGetChainContext JestParser$MemberGetContext JestParser$RecordConstructorContext JestParser$ExpressionListContext JestParser$TypeAnnotationContext JestParser$FuncTypeAnnotationContext JestParser$LambdaContext JestParser$FunctionDefContext JestParser$MethodDefContext JestParser$FunctionDefParamsContext JestParser$FunctionCallContext JestParser$RecordDefContext JestParser$ImplementationDefContext JestParser$MethodParamsContext JestParser$ForLoopContext JestParser$BlockContext JestParser$VarScopeContext JestParser$ConditionalContext JestParser$ClojureVectorContext JestParser$ClojureMapContext)
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



;; TODO
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


;; TODO
(defn -visitArithmeticTerm
  [this ^JestParser$ArithmeticTermContext ctx]

  (reduce (fn [accum next]
            (let [[left right] next
                  op (symbol (. left getText))
                  term (.. this (visitExpressionComposed right))]
              '(~op ~accum ~term)))
          (. this (visitExpressionComposed (. ctx a)))
          (zip (. ctx op) (. ctx b))))


(defn -visitExpressionComposed
  [this ^JestParser$ExpressionComposedContext ctx]

  (cond
    (. ctx methodCallChain) (self-visit this ctx methodCallChain) ;;(.. this (visitMethodCallChain (. ctx methodCallChain)))
    (. ctx expressionAtom) (self-visit this ctx expressionAtom) ;;(.. this (visitExpressionAtom (. ctx expressionAtom)))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitMethodCallChain
  [this ^JestParser$MethodCallChainContext ctx]

  (cond

    (. ctx methodCall) (self-visit this ctx methodCall) ;;(.. this (visitMethodCall (. ctx methodCall)))

    (. ctx PERIOD) (let [a (.. ctx a getText)
                         b (self-visit this ctx methodCallChain) ;;(.. this (visitMethodCallChain (. ctx methodCallChain)))
                         params (.. this (visitMethodParams (. ctx b)))]
                     `(~a ~b ~@params))

    (. ctx ARROW) (let [a (.. ctx a getText)
                        b (self-visit this ctx methodCallChain) ;;(.. this (visitMethodCallChain (. ctx methodCallChain)))
                        params (.. this (visitMethodParams (. ctx b)))]
                    `(~a ~@params ~b))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitMethodCall
  [this ^JestParser$MethodCallContext ctx]

  (cond
    (. ctx PERIOD) (let [a (.. ctx func getText)
                         b (.. this (visitExpressionAtom (. ctx obj)))
                         params (self-visit this ctx methodParmams)] ;;(.. this (visitMethodParams (. ctx methodParams)))]
                     (a b params))


    (. ctx ARROW) (let [a (.. ctx func getText)
                        b (.. this (visitExpressionAtom (. ctx obj)))
                        params (self-visit this ctx methodParams)] ;;(.. this (visitMethodParams (. ctx methodParams)))]
                    (b a params))

    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitExpressionAtom
  [this ^JestParser$ExpressionAtomContext ctx]

  (cond
    (. ctx NUMBER) (read-string (.. ctx NUMBER getText))

    (. ctx TRUE) true ;;(.. ctx TRUE getText)

    (. ctx FALSE) false ;;(.. ctx FALSE getText)

    (. ctx NIL) nil ;;(.. ctx NIL getText)

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
  (throw (new NotImplementedException)))


(defn -visitMemberGet
  [this ^JestParser$MemberGetContext ctx]
  (throw (new NotImplementedException)))


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
  (throw (new NotImplementedException)))


(defn -visitMethodDef
  [this ^JestParser$MethodDefContext ctx]
  (throw (new NotImplementedException)))


(defn -visitFunctionDefParams
  [this ^JestParser$FunctionDefParamsContext ctx]
  (throw (new NotImplementedException)))


(defn -visitFunctionCall
  [this ^JestParser$FunctionCallContext ctx]

  `(~(get-symbol ctx ID) ~@(.. this (visitMethodParams (. ctx methodParams)))))
  ;
  ;String code = String.format("(%s%s)",
  ;                             ctx.ID().getText(),
  ;                             this.visitMethodParams(ctx.methodParams()).getSingleLine());
  ;return Code.singleLine(code);
  ;
  ;
  ;(throw (new NotImplementedException)))


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
    :else (throw (new ClojureSourceGenerator$BadSource ctx))))


(defn -visitForLoop
  [this ^JestParser$ForLoopContext ctx]
  (throw (new NotImplementedException)))


(defn -visitBlock
  [this ^JestParser$BlockContext ctx]
  (throw (new NotImplementedException)))


(defn -visitVarScope
  [this ^JestParser$VarScopeContext ctx]
  (throw (new NotImplementedException)))


(defn -visitConditional
  [this ^JestParser$ConditionalContext ctx]
  (throw (new NotImplementedException)))


(defn -visitClojureVector
  [this ^JestParser$ClojureVectorContext ctx]
  (throw (new NotImplementedException)))


(defn -visitClojureMap
  [this ^JestParser$ClojureMapContext ctx]
  (throw (new NotImplementedException)))


(defn -visitClojureGet
  [this JestParser$ClojureGetContext ctx]
  (throw (new NotImplementedException)))