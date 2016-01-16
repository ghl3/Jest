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
sourceCode
    : (importStatement SEMICOLON WS?)*
      (statementTerm WS?)*
        EOF
    ;

path
    :  a=ID (PERIOD b+=ID)*
    ;

importStatement
    : IMPORT path
    ;


// A statementTerm is a statement followed
// by a semicolon
statementTerm
    : statement SEMICOLON
    | functionDef
    | recordDef
    | block
    | varScope
    ;

statement
    : expression
    | defAssignment
    ;

defAssignment
    : DEF name=ID (COLON type=typeAnnotation)? '=' expression
    ;

expression
    : comparisonExpression
    ;

comparisonExpression
    : a=arithmeticExpression (op=(GT | LT | GTEQ | LTEQ | CPEQ) b=arithmeticExpression)?
    | a=arithmeticExpression
    ;

arithmeticExpression
    : a=arithmeticTerm ( ( op+=PLUS | op+=MINUS ) b+=arithmeticTerm)*
    ;

arithmeticTerm
    : a=expressionComposed ( (op+=MULT | op+=DIV) b+=expressionComposed)*
    ;

expressionComposed
    : methodCallChain
    | expressionAtom
    ;


/*
Method calls are inverted functions
obj.func(x, y, z) <--> func(obj, x, y, z)
They can take any expression atom and transform it
into a function call on that atom
*/
methodCallChain
    : methodCall
    | chain=methodCallChain (PERIOD a=ID b=methodParams | ARROW c=ID d=methodParams)
    ;

methodCall
    : obj=expressionAtom PERIOD func=ID methodParams
    | obj=expressionAtom ARROW func=ID methodParams
    ;

/*
An expression atom is an undividable component
of an expression.  Each atom itself is an expression
but can be composed with each other (using other sytax)
to create more complicated expressions
(The obvious exception is the final line, but that
is present to make the code simpler)
*/
expressionAtom
    : NUMBER
    | TRUE
    | FALSE
    | NIL
    | ID
    | STRING
    | SYMBOL
    | clojureVector
    | clojureMap
    | functionCall
    | clojureGet
    | forLoop
    | conditional
    | lambda
    | memberGetChain
    | recordConstructor
    | block
    | '(' expression ')'
    ;

memberGetChain
    : memberGet (PERIOD a+=ID)+
    | memberGet
    ;

memberGet
    : record=ID PERIOD member=ID
    ;

recordConstructor
    : NEW name=ID methodParams
    | NEW name=ID '(' firstKey=ID COLON firstExp=expression (COMMA key+=ID COLON exp+=expression)+ ')'
    ;

expressionList
    :  a=expression (COMMA b+=expression)+
    ;

/* Consider adding '/t' as a prefix to all of these
   and remove the hash notation to prefix that by hand*/
typeAnnotation
    : singleType=path
    | '#' hashType=ID
    | typeleft=ID num=NUMBER
    | '(' thing=typeAnnotation ')'
    | container=ID '[' (inner+=typeAnnotation)+ ']'
        /* This is a bit of a hack to get nested containers to work */
        /* The issue is that in jest: HVec[[(?) (?) (?)]] could be parsed */
        /* by allowing a '[' typeAnnotation ']' branch, but this breaks */
        /* the jest container branch style below */
    | nestedContainer=ID '[[' (nestedInner+=typeAnnotation)+ ']]'
    ;

funcTypeAnnotation
    : first=typeAnnotation (next+=typeAnnotation)*
    ;



functionDef
    : DEFN name=ID functionDefParams (ARROW returnType=typeAnnotation)? block (SEMICOLON)?
    ;


methodDef
    : DEFN name=ID functionDefParams (ARROW returnType=typeAnnotation)? block (SEMICOLON)?
    ;


functionDefParams
    : '(' ')'
    | '(' first=ID (COLON firstType=typeAnnotation)? (COMMA rest+=ID (COLON restTypes+=typeAnnotation)? )* ')'
    ;


functionCall
    : ID methodParams
    ;


lambda
    : functionDefParams ARROW body=block;


recordDef
    : RECORD name=ID '{'
        first=ID SEMICOLON (field+=ID SEMICOLON)*
        (imp+=implementationDef)*
        '}'
    ;

implementationDef
     : IMPLEMENTS protocol=ID '{' (method+=methodDef)* '}'
     ;

/*
 Parameter for a function or method call
 is either an empty string or, if parameters are
 present, includes leading whitespace so it can be
 directly added to any previous text
*/

methodParams
    : '(' ')'
    | '(' expressionList ')'
    | '(' expression ')'
    ;


forLoop
    : FOR '(' a=ID (COMMA b+=ID )* COLON c=expression (COMMA d+=expression)* ')' (LAZY)? block
    ;


block
    : '{' expression '}'
    | '{' (term+=statementTerm)+ '}'
    | '{' (scope+=varScope)+ '}'
    ;


varScope
    : (LET name+=ID (COLON a+=funcTypeAnnotation)? '=' exp+=expression SEMICOLON)+
      (terms+=statementTerm )*
    ;


conditional
    : IF '(' ifCondition=expression ')' iftrue=block
      (ELIF '(' elifExpression+=expression ')' elifBlock+=block)*
      (ELSE elseBlock=block)?
    ;


clojureVector
    : '[' ']'
    | '[' a=expression (COMMA WS? b+=expression)* ']'
    ;

clojureMap
    : '{' '}'
    | '{' a=expression COLON b=expression (COMMA WS? c+=expression COLON d+=expression)* '}'
    ;

clojureGet
    : a=ID '[' b=expression ']'
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

