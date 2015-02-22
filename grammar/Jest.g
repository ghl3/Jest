grammar Jest;

options
{
  // antlr will generate java lexer and parser
  language = Java;
  // generated parser should create abstract syntax tree
  //output = AST;
}

@lexer::header {
  package jest.grammar;
}

@parser::header {
package jest.grammar;
}

// A file is a list of statements
// followed by an EOF
sourceCode returns [List<String> codeList]
@init{
    $codeList = new ArrayList<String>();
}
    : (importStatement SEMICOLON {$codeList.add($importStatement.code);} WS?)*
      (statementTerm {$codeList.add($statementTerm.code);} WS?)*
        EOF
    ;

importStatement returns [String code]
@init{$code = "(import '"; }
@after{$code += ")"; }
    : IMPORT a=ID {$code += $a.text;} (PERIOD b=ID {$code += "." + $b.text;} )*
    ;

// A statementTerm is a statement followed
// by a semicolon
statementTerm returns [String code]
    : statement SEMICOLON {$code = $statement.code;}
    | functionDef {$code = $functionDef.code; }
    | recordDef {$code = $recordDef.code; }
    | block {$code = $block.code; }
    | varScope {$code = $varScope.code; }
    ;

statement returns [String code]
    : expression {$code = $expression.code; }
    | defAssignment {$code = $defAssignment.code; }
    ;

defAssignment returns [String code]
@init{
    String annotation = "";
}
@after {
    $code = annotation + $code;
}
    : DEF name=ID
      (COLON type=typeAnnotation {annotation = "(t/ann " + $name.text + " " + $type.code +  ")\n";})?
      '=' expression { $code = "(def " + $name.text + " " + $expression.code + ")"; }
    ;


expression returns [String code]
    : comparisonExpression {$code = $comparisonExpression.code; }
    ;


comparisonExpression returns [String code]
    : a=arithmeticExpression {$code=$a.code;}
    (op=(GT | LT | GTEQ | LTEQ | CPEQ) b=arithmeticExpression {$code="("+$op.text+" "+$a.code+" "+$b.code+")";})?
/*
    | a=arithmeticExpression {$code=$a.code;} (LT b=arithmeticExpression {$code="(< "+$a.code+" "+$b.code+")";})?
    | a=arithmeticExpression {$code=$a.code;} (GTEQ b=arithmeticExpression {$code="(>= "+$a.code+" "+$b.code+")";})?
    | a=arithmeticExpression {$code=$a.code;} (LTEQ b=arithmeticExpression {$code="(<= "+$a.code+" "+$b.code+")";})?
    | a=arithmeticExpression {$code=$a.code;} (CPEQ b=arithmeticExpression {$code="(= "+$a.code+" "+$b.code+")";})?
    */
    | a=arithmeticExpression {$code=$a.code;}
    ;


arithmeticExpression returns [String code]
@init{$code = ""; }
    : a=arithmeticTerm {$code = $a.code;} ( PLUS {$code = "(+ " + $code + " ";} | MINUS {$code = "(- " + $code + " ";} ) b=arithmeticTerm {$code += $b.code + ")";}
    | a=arithmeticTerm {$code = $a.code;}
    ;

arithmeticTerm returns [String code]
@init{$code = ""; }
    : a=expressionComposed {$code = $a.code;} ( MULT {$code = "(* " + $code + " ";} | DIV {$code = "(/ " + $code + " ";} ) b=expressionComposed {$code += $b.code + ")";}
    | a=expressionComposed {$code = $a.code;}
    ;

expressionComposed returns [String code]
    : methodCallChain{$code=$methodCallChain.code;}
    | expressionAtom {$code=$expressionAtom.code;}
    ;


/*
Method calls are inverted functions
obj.func(x, y, z) <--> func(obj, x, y, z)
They can take any expression atom and transform it
into a function call on that atom
*/

methodCallChain returns [String code]
    : methodCall {$code=$methodCall.code;}
        (
            PERIOD a=ID b=methodParams {$code="("+$a.text+" "+$code+$b.code+")";} |
            ARROW c=ID d=methodParams {$code="("+$c.text+$d.code+" "+$code+")";}
        )+
    | methodCall {$code=$methodCall.code;}
    ;

methodCall returns [String code]
    : obj=expressionAtom PERIOD func=ID methodParams { $code = "(" + $func.text + " " + $obj.code + $methodParams.code + ")"; }
    | obj=expressionAtom ARROW func=ID methodParams { $code = "(" + $func.text + $methodParams.code + " " + $obj.code + ")"; }
    ;


/*
An expression atom is an undividable component
of an expression.  Each atom itself is an expression
but can be composed with each other (using other sytax)
to create more complicated expressions
(The obvious exception is the final line, but that
is present to make the code simpler)
*/

expressionAtom returns [String code]
    : NUMBER {$code = $NUMBER.text;}
    | TRUE {$code = $TRUE.text; }
    | FALSE {$code = $FALSE.text; }
    | NIL {$code = $NIL.text; }
    | ID {$code = $ID.text; }
    | STRING {$code = $STRING.text; }
    | SYMBOL {$code = $SYMBOL.text; }
    | clojureVector {$code = $clojureVector.code; }
    | clojureMap {$code = $clojureMap.code; }
    | functionCall {$code = $functionCall.code; }
    | clojureGet {$code = $clojureGet.code; }
    | forLoop {$code = $forLoop.code; }
    | conditional {$code = $conditional.code; }
    | lambda {$code=$lambda.code;}
    | memberGetChain {$code=$memberGetChain.code;}
    | recordConstructor {$code=$recordConstructor.code;}
    | block {$code=$block.code;}
    | '(' expression ')' {$code = $expression.code; }
    ;



memberGetChain returns [String code]
    : memberGet {$code=$memberGet.code;} (PERIOD a=ID {$code="(:"+$a.text+" "+$code+")";})+
    | memberGet {$code=$memberGet.code;}
    ;


memberGet returns [String code]
    : record=ID PERIOD member=ID { $code = "(:" + $member.text + " " + $record.text + ")";}
    ;


recordConstructor returns [String code]
    : NEW ID methodParams {$code="(->"+$ID.text+$methodParams.code+")";}
    | NEW name=ID {$code="(map->"+$name.text+" {";}
         '(' firstKey=ID COLON firstExp=expression {$code+=":"+$firstKey.text+" "+$firstExp.code;} (COMMA key=ID COLON exp=expression {$code+=" :"+$key.text+" "+$exp.code;})+ ')'
          {$code+="})";}
    ;


expressionList returns [List<String> codeList]
@init{$codeList = new ArrayList<String>();}
    :  a=expression {$codeList.add($a.code);} (COMMA b=expression { $codeList.add($b.code);})+
    ;

/* Consider adding '/t' as a prefix to all of these
   and remove the hash notation to prefix that by hand*/

typeAnnotation returns [String code]
    : type=ID  {$code=$type.text;}
    | '#' type=ID  {$code="t/" + $type.text;}
    | typeleft=ID num=NUMBER  {$code=$typeleft.text + " " + $num.text;}
    | '(' thing=typeAnnotation ')'  {$code = "(" + $thing.code + ")";}
    | container=ID {$code = "(t/" + $container.text;} '[' (inner=typeAnnotation {$code += " " + $inner.code;})+ ']' {$code += ")";}
        /* This is a bit of a hack to get nested containers to work */
        /* The issue is that in jest: HVec[[(?) (?) (?)]] coudl be parsed */
        /* by allowing a '[' typeAnnotation ']' branch, but this breaks */
        /* the jest container branch style below */
    | container=ID {$code = "(t/" + $container.text + " [";} '[[' (inner=typeAnnotation {$code += " " + $inner.code;})+ ']]' {$code += "])";}
    ;

funcTypeAnnotation returns [String code]
    : first=typeAnnotation {$code = $first.code;} (next=typeAnnotation {$code += " " + $next.code;})*
    ;


/*
Lambda must have a '%' somewhere in the expression
If the expression is a function call, we unwrap the
external paranthese so it is:
#(func % %)
and not
#((func % %))
*/
lambda returns [String code]
    : '#(' expression ')' {
            if ($expression.code.length() > 2 && $expression.code.matches("[(].*[)]")) { //get(0)=="(" && $expression.code.get(expressions.length()-1)==")") {
                $code="#"+$expression.code;
            } else {
                $code="#("+$expression.code+")";
            }
        }
    ;

/* NEW SCOPE */
functionDef returns [String code]
@init{
    String annotation = "";
}
@after{
    $code = annotation + $code;
}
    : DEFN name=ID {$code="(defn "+$name.text;}
            functionDefParams {$code += " ["+$functionDefParams.code+" ]";}
        (COLON {annotation = "(t/ann " + $name.text + " [";} a=funcTypeAnnotation { annotation += $a.code + " ";}
         ARROW c=typeAnnotation {annotation += "-> " + $c.code + "])\n";})?
         block {$code+=$block.code;} (SEMICOLON)? {$code+=")";}
    ;

/* NEW SCOPE */
methodDef returns [String code]
@init{
    String annotation = "";
}
@after{
    $code = annotation + $code;
}
    : DEFN name=ID {$code="("+$name.text;}
            functionDefParams {$code += " ["+$functionDefParams.code+" ]";}
        (COLON {annotation = "(t/ann " + $name.text + " [";} a=funcTypeAnnotation { annotation += $a.code + " ";}
         ARROW c=typeAnnotation {annotation += "-> " + $c.code + "])\n";})?
         block {$code+=$block.code;} (SEMICOLON)? {$code+=")";}
    ;


functionDefParams returns [String code]
    : '(' ')' { $code = "";}
    | '(' first=ID {$code = " "+$first.text;} (COMMA rest=ID {$code += " " + $rest.text;})* ')'
    ;


functionCall returns [String code]
    : ID methodParams { $code = "(" + $ID.text + $methodParams.code + ")"; }
    ;


recordDef returns [String code]
    : RECORD name=ID '{' {$code="(defrecord " + $name.text + " [";}
        first=ID SEMICOLON {$code += $first.text;} (field=ID SEMICOLON{$code += " "+$field.text;})* {$code += "]";}


        (IMPLEMENTS protocol=ID {$code += "\n"+$protocol.text;} '{'(method=methodDef {$code += "\n"+$method.code;})*'}')*

        '}' {$code += ")";}
    ;

/*
 Parameter for a function or method call
 is either an empty string or, if parameters are
 present, includes leading whitespace so it can be
 directly added to any previous text
*/

methodParams returns [String code]
    : '(' ')' { $code = "";}
    | '(' expressionList ')' {
            $code = "";
            for(int i=0; i < $expressionList.codeList.size(); ++i) {
                $code += " " +$expressionList.codeList.get(i);
            }
        }
    | '(' expression ')' { $code = " " + $expression.code; }
    ;

/* NEW SCOPE */
forLoop returns [String code]
@init{
    String func = "(fn ";
    String iterator = "";
    Boolean lazy = false;
}
@after{
    func += ") ";

    if (lazy) {
      $code = "";
    } else {
      $code = "(doall ";
    }

    $code += "(map " + func + " " + iterator + ")";

    if (!lazy) $code += ")";
}
    : FOR '(' a=ID {func += "[ " + $a.text;} (COMMA b=ID {func += " " + $b.text;})* {func += " ]";}
      COLON c=expression {iterator = "(seq " + $c.code + ")";} (COMMA d=expression {iterator += " (seq " + $d.code + ")";})* ')'
      (LAZY {lazy=true;})? block { func += $block.code;}
    ;

/* NEW SCOPE */
block returns [String code]
    : '{' expression {$code=$expression.code;} '}'
    | '{' {$code="";} (statementTerm {$code+=" "+$statementTerm.code;})+ '}'
    | '{' {$code="";} (varScope {$code+=" "+$varScope.code;})+ '}'
    ;

/* TODO: add typing using the core.typed let macro:
http://clojure.github.io/core.typed/#clojure.core.typed/let
*/
varScope returns [String code]
    :  {$code="(let [";} (LET name=ID '=' exp=expression SEMICOLON {$code+=" "+$name.text+" "+$exp.code;})+ {$code+=" ]";}
        (statementTerm {$code+=" "+$statementTerm.code;})* {$code+=")";}
    ;

/* NEW SCOPE */
conditional returns [String code]
@init{
    List<String> conditions = new ArrayList<String>();
    List<String> results = new ArrayList<String>();
}
@after{

    if (conditions.size()==results.size()) {
        if (conditions.size()==1) {
            $code = "(if " + conditions.get(0) + " " + results.get(0) + ")";
        } else {
            $code = "(cond ";
            for (int i=0; i < conditions.size(); ++i) {
                $code += "\n" + conditions.get(i) + " " + results.get(i);
            }
            $code += ")";
        }
    } else if (conditions.size()+1 == results.size()) {
        // If we have 1 more result than condition, the
        // final result is a guaranteed 'else'
        if (conditions.size()==1) {
            $code = "(if " + conditions.get(0) + " " + results.get(0) + " " + results.get(1) +")";
        } else {
            $code = "(cond ";
            for (int i=0; i < conditions.size(); ++i) {
                $code += "\n" + conditions.get(i) + " " + results.get(i);
            }
            $code += "\n :else " + results.get(results.size()-1) + ")";
        }
    } else {
        $code = "WTF!!!";
    }
}
    : IF '(' ifCondition=expression ')' iftrue=block {conditions.add($ifCondition.code);results.add($iftrue.code);}
      (ELIF '(' elifExpression=expression ')' elifBlock=block {conditions.add($elifExpression.code);results.add($elifBlock.code);})*
       (ELSE elseBlock=block {results.add($elseBlock.code);})?
    ;


clojureVector returns [String code]
@init{$code = "["; }
@after{$code += "]"; }
    : '[' ']'
    | '[' a=expression {$code += $a.code;} (COMMA WS? b=expression {$code += ", " + $b.code;})* ']'
    ;

clojureMap returns [String code]
@init{$code = "{"; }
@after{$code += "}"; }
    : '{' '}'
    | '{' a=expression COLON b=expression {$code += $a.code + " " + $b.code;} (COMMA WS? c=expression COLON d=expression {$code += " " + $c.code + " " + $d.code;})* '}'
    ;

clojureGet returns [String code]
    : a=ID '[' b=expression ']' {$code = "(get " + $a.text + " " + $b.code + ")";}
    ;


// LEXER RULES


// or a python style hash comment
Comment
    :   '//' ~('\n'|'\r')* ('\n'|'\r') {skip();}
    |  '/*' .*? '*/' {skip();}
    ;

WS  :   (' '
        |   '\t'
        |   ('\n'|'\r'('\n'))
        )+ -> channel(HIDDEN)
    ;

/** Lexer Rules **/

ARROW: '->' ;

PLUS:     '+' ;
MINUS:    '-' ;
MULT:     '*' ;
DIV:      '/' ;

STRING : '"' ~('\r' | '\n' | '"')* '"' ;

DEF: 'def';

LET: 'let';

NEW: 'new';

RECORD: 'record';

IMPLEMENTS: 'implements';

DEFN: 'defn';

FOR: 'for';

LAZY: 'lazy';

IF: 'if';

ELSE: 'else';

ELIF: 'elif';

IMPORT: 'import';

GT: '>' ;

GTEQ: '>=' ;

LT: '<' ;

LTEQ: '<=' ;

CPEQ: '==';

TRUE: 'true';

FALSE: 'false';

NIL: 'nil';

/* Names of variables and functions */
ID
    : ('a'..'z' | 'A'..'Z')+('?')?
    | '%'(INTEGER)?
    ;

SYMBOL: ':' ID;

fragment
DIGIT : '0'..'9';

fragment
INTEGER : (DIGIT)+;

fragment
DOUBLE : '0'..'9'+'.''0'..'9'+ ;

NUMBER
    : INTEGER
    | DOUBLE
    ;

SEMICOLON : ';';

COMMA : ',';

COLON : ':';

PERIOD : '.';

HASH : '#';

