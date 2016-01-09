(ns jest.compiler.JestToClojureTranslator
  (:import (jest.grammar JestParser$SourceCodeContext JestParser$StatementTermContext
                         JestParser$StatementContext JestParser$DefAssignmentContext
                         JestParser$ExpressionContext JestParser$ComparisonExpressionContext
                         JestParser$ArithmeticExpressionContext JestParser$ArithmeticTermContext
                         JestParser$ExpressionComposedContext JestParser$MethodCallContext
                         JestParser$MethodCallChainContext JestParser$ExpressionAtomContext
                         JestParser$MemberGetChainContext JestParser$MemberGetContext
                         JestParser$RecordConstructorContext JestParser$ExpressionListContext
                         JestParser$TypeAnnotationContext JestParser$FuncTypeAnnotationContext
                         JestParser$LambdaContext JestParser$FunctionDefContext
                         JestParser$MethodDefContext JestParser$ConditionalContext
                         JestParser$FunctionDefParamsContext JestParser$FunctionCallContext
                         JestParser$RecordDefContext JestParser$ImplementationDefContext
                         JestParser$MethodParamsContext JestParser$ForLoopContext
                         JestParser$BlockContext JestParser$VarScopeContext
                         JestParser$ClojureVectorContext JestParser$ClojureMapContext JestParser$ClojureGetContext)
           (jest.compiler LegacyClojureSourceGenerator$BadSource)
           (sun.reflect.generics.reflectiveObjects NotImplementedException)
           (java.util List))
  (:gen-class
    :extends jest.grammar.JestBaseVisitor
    :prefix "-"
    :main false))

(defn- make-seqable
  [x]
  `(seq ~x))

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


(defn- wrap-in-do
  [expressions]
  (cond
    (not (instance? List expressions)) expressions
    (= 0 (count expressions)) (throw (new NotImplementedException))
    (= 1 (count expressions)) (first expressions)
    :else `(do ~@expressions)))


;; HELPER
(defn merge-items
  "Takes two items and joins them as a single sequence."
  [left right]
  (concat (obj->seq left) (obj->seq right)))


;; HELPER
(defn zip
  [& args]
  (apply map vector args))


(defn alternate
  "Takes a list of sequences and returns a sequence
  of the first from each sequence, in order, followed
  by the 2nd, etc (as a single long sequence)"
  [& args]
  (apply concat (apply zip args)))


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
    (. ctx statement) (self-visit this ctx statement)
    (. ctx functionDef) (self-visit this ctx functionDef)
    (. ctx recordDef) (self-visit this ctx recordDef)

    ;; I believe we want to wrap this in a do to make it
    ;; a single statement, not a list, correct...?
    (. ctx block) (wrap-in-do (self-visit this ctx block))
    (. ctx varScope) (self-visit this ctx varScope)

    :else (throw (new LegacyClojureSourceGenerator$BadSource ctx))))


(defn -visitStatement
  [this ^JestParser$StatementContext ctx]

  (cond
    (. ctx expression)    (self-visit this ctx expression)
    (. ctx defAssignment) (self-visit this ctx defAssignment)

    :else (throw (new LegacyClojureSourceGenerator$BadSource ctx))))


(defn -visitDefAssignment
  [this ^JestParser$DefAssignmentContext ctx]

  (let [type (. ctx type)
        name (get-symbol ctx name)
        expr (self-visit this ctx expression)]

    (if type (println type))
    `(def ~name ~expr)))


(defn -visitExpression
  [this ^JestParser$ExpressionContext ctx]
  (self-visit this ctx comparisonExpression))


(def comparison-override-map {"==", "="})


(defn- create-comparison-operation
  [this ctx]
  (let [op-raw (.. ctx op getText)
        op (symbol (get comparison-override-map op-raw op-raw))
        left (.. this (visitArithmeticExpression (. ctx a)))
        right (.. this (visitArithmeticExpression (. ctx b)))]
    `(~op ~left ~right)))


(defn -visitComparisonExpression
  [this ^JestParser$ComparisonExpressionContext ctx]

  (if (. ctx op)
    (create-comparison-operation this ctx)
    (.. this (visitArithmeticExpression (. ctx a)))))


(defn -visitArithmeticExpression
  [this ^JestParser$ArithmeticExpressionContext ctx]
  (reduce (fn [accum next]
            (let [[left right] next
                  op (symbol (. left getText))
                  term (.. this (visitArithmeticTerm right))]
              `(~op ~accum ~term)
              ))
          (. this (visitArithmeticTerm (. ctx a)))
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

    :else (throw (new LegacyClojureSourceGenerator$BadSource ctx))))


(defn -visitMethodCallChain
  [this ^JestParser$MethodCallChainContext ctx]

  (cond

    (. ctx methodCall) (self-visit this ctx methodCall)

    (. ctx PERIOD) (let [left (get-symbol ctx a)
                         right (self-visit this ctx methodCallChain)
                         params (.. this (visitMethodParams (. ctx b)))]
                     `(~left ~right ~@params))

    (. ctx ARROW) (let [left (get-symbol ctx c)
                        right (self-visit this ctx methodCallChain)
                        params (.. this (visitMethodParams (. ctx d)))]
                    `(~left ~@params ~right))

    :else (throw (new LegacyClojureSourceGenerator$BadSource ctx))))


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
                    `(~a ~@params ~b))

    :else (throw (new LegacyClojureSourceGenerator$BadSource ctx))))


(defn -visitExpressionAtom
  [this ^JestParser$ExpressionAtomContext ctx]

  (cond
    (. ctx NUMBER) (read-string (.. ctx NUMBER getText))

    (. ctx TRUE) true

    (. ctx FALSE) false

    (. ctx NIL) nil

    (. ctx ID) (symbol (.. ctx ID getText))

    (. ctx STRING) (read-string (.. ctx STRING getText))

    ;; TODO: Rename 'symbol' to 'keyword'
    (. ctx SYMBOL) (keyword (subs (.. ctx SYMBOL getText) 1)) ;;(keyword (.. ctx SYMBOL getText))

    (. ctx clojureVector) (self-visit this ctx clojureVector)

    (. ctx clojureMap) (self-visit this ctx clojureMap)

    (. ctx functionCall) (self-visit this ctx functionCall)

    (. ctx clojureGet) (self-visit this ctx clojureGet)

    (. ctx forLoop) (self-visit this ctx forLoop)

    (. ctx conditional) (self-visit this ctx conditional)

    (. ctx lambda) (self-visit this ctx lambda)

    (. ctx memberGetChain) (self-visit this ctx memberGetChain)

    (. ctx recordConstructor) (self-visit this ctx recordConstructor)

    ;; A block, when used as an expression, must consist of a
    ;; single expression or otherwise must be wrapped
    ;; in a do statement
    (. ctx block) (wrap-in-do (self-visit this ctx block))

    (. ctx expression) (self-visit this ctx expression)

    :else (throw (new LegacyClojureSourceGenerator$BadSource ctx))))


(defn -visitMemberGetChain
  [this ^JestParser$MemberGetChainContext ctx]
  (if (. ctx PERIOD)
    (reduce (fn [accum next] `(~(keyword (. next getText)) ~accum))
            (self-visit this ctx memberGet)
            (. ctx a))
    (self-visit this ctx memberGet)))


(defn -visitMemberGet
  [this ^JestParser$MemberGetContext ctx]
  `(~(keyword (.. ctx member getText)) ~(symbol (.. ctx record getText))))


(defn -visitRecordConstructor
  [this ^JestParser$RecordConstructorContext ctx]

  (if (nil? (. ctx firstKey))

    `(~(symbol (str "->" (.. ctx name getText))) ~@(self-visit this ctx methodParams))

    `(~(symbol (str "map->" (.. ctx name getText)))
       ~(apply array-map (alternate
         (map #(keyword (. % getText)) (merge-items (. ctx firstKey) (. ctx key)))
         (map #(.. this (visitExpression %)) (merge-items (. ctx firstExp) (. ctx exp))))))))


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
  (let [params (self-visit this ctx functionDefParams)
        body  (self-visit this ctx block)]
  `(fn [~@params] ~@body)))


(defn -visitFunctionDef
  [this ^JestParser$FunctionDefContext ctx]

  ;if (ctx.funcTypeAnnotation() != null) {
  ;                                       code += String.format("(t/ann %s [%s -> %s])\n",
  ;                                                              ctx.name.getText(),
  ;                                                              this.visitFuncTypeAnnotation(ctx.funcTypeAnnotation()).getSingleLine(),
  ;                                                              this.visitTypeAnnotation(ctx.typeAnnotation()).getSingleLine());
  ;                                       }

  `(defn
     ~(get-symbol ctx name)
     [~@(self-visit this ctx functionDefParams)]
     ~@(self-visit this ctx block)))


(defn -visitMethodDef
  [this ^JestParser$MethodDefContext ctx]

  ;
  ;if (ctx.typeAnnotation() != null) {
  ;                                   annotation = String.format("(t/ann %s [%s ->%s])\n",
  ;                                                               ctx.name.getText(),
  ;                                                               this.visitFuncTypeAnnotation(ctx.a).getSingleLine(),
  ;                                                               this.visitTypeAnnotation(ctx.c).getSingleLine());
  ;                                   }


  `(~(symbol (.. ctx name getText))
     [~@(self-visit this ctx functionDefParams)]
     ~@(self-visit this ctx block)))


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

  `(defrecord ~(get-symbol ctx name)
     [~@(map #(symbol (.. % getText)) (merge-items (. ctx first) (. ctx field)))]
     ~@(apply concat (map #(.. this (visitImplementationDef %)) (. ctx implementationDef)))))


(defn -visitImplementationDef
  [this ^JestParser$ImplementationDefContext ctx]

  (into [(get-symbol ctx protocol)]
        (map #(.. this (visitMethodDef %)) (. ctx methodDef))))


(defn -visitMethodParams
  "Takes a parsed set of method parameters
  and return a list of those parameters
  as clojure objects"
  [this ^JestParser$MethodParamsContext ctx]

  (cond
    (. ctx expressionList) (self-visit this ctx expressionList)
    (. ctx expression)     [(self-visit this ctx expression)]
    :else '()))



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
  "Note that a block, unlike most other visit statements,
  returns a list of clojure forms, not just a single form.
  This can make working with blocks tricky.  One must take
  care to do one of the following:
  - Unwrap the list in a macro using the ~@ operator
  - Wrap the list in a single 'do' expression with the wrap-in-do function
  You would use the first version if it's okay to have a number of
  clojure forms in a row.  For example, a function body is allowed to
  consist of multiple clojure forms each representing an expression:

  (defn foobar [x]
      (A)
      (B)
      (C))

  On the other hand, the 'true' or 'false' part of an if statement
  must be a single expression, and therefore it must be wrapped in a
  do expression if one wants to use a block that consists of multiple
  expressions:

  (if true
      (do
          (A)
          (B)
          (C))
      false)

  We could have automatically wrapped all blocks in do statements in
  visitBlock (if necessary) but we like in the 'defn' example above
  having all the expressions inline an an unwrapped list, so we here
  return a list and leave the unwrapping of the list to the consumer."
  [this ^JestParser$BlockContext ctx]

  (cond
    (. ctx expression) [(self-visit this ctx expression)]

    (. ctx term) (into [] (map #(.. this (visitStatementTerm %)) (. ctx term)))

    (. ctx scope) (into [] (map #(.. this (visitVarScope %)) (. ctx scope)))

    :else (throw (new LegacyClojureSourceGenerator$BadSource ctx))))


(defn -visitVarScope
  [this ^JestParser$VarScopeContext ctx]
  (let [names (into [] (map #(symbol (. % getText)) (. ctx name)))
        vals (into [] (map #(.. this (visitExpression %)) (. ctx exp)))
        bindings (alternate names vals)
        expressions (map #(. this (visitStatementTerm %)) (. ctx terms))]
    `(let [~@bindings] ~@expressions)))


(defn -visitConditional
  [this ^JestParser$ConditionalContext ctx]

  (let [conditions (map #( .. this (visitExpression %)) (merge-items (. ctx ifCondition) (. ctx elifExpression)))
        results (map #(wrap-in-do (.. this (visitBlock %))) (merge-items (. ctx iftrue) (. ctx elifBlock)))
        else (if (. ctx elseBlock) (wrap-in-do (.. this (visitBlock (. ctx elseBlock)))) nil)
        single-if (= (. conditions size) 1)]

    (cond
      (and single-if (not (nil? else))) `(if ~(. conditions (get 0)) ~(. results (get 0)) ~else)
      single-if `(if ~(. conditions (get 0)) ~(. results (get 0)))
      (not else) `(cond ~@(alternate conditions results))
      :else `(cond ~@(alternate conditions results) :else ~else))))


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
  [this ^JestParser$ClojureGetContext ctx]
  `(get ~(symbol (.. ctx a getText)) ~(.. this (visitExpression (. ctx b)))))