grammar Jest;

options
{
  // antlr will generate java lexer and parser
  language = Java;
  // generated parser should create abstract syntax tree
  output = AST;
}

@lexer::header {
  package jest.grammar;
}

@parser::header {
package jest.grammar;
}

// or a python style hash comment
LineComment
    :   '[//,#]' ~('\n'|'\r')* //NEWLINE
        {System.out.println("lc > " + getText());
        skip();}
    ;

WS  :   (' '
        |   '\t'
        |   ('\n'|'\r'('\n'))
        )+
        {$channel=HIDDEN;}
    ;

/** Lexer Rules **/

PLUS:     '+' ;
MINUS:    '-' ;
MULT:     '*' ;
DIV:      '/' ;

/*ID: ('a'..'z' | 'A'..'Z')+;*/

STRING : '"' ~('\r' | '\n' | '"')* '"' ;

ID: ('a'..'z' | 'A'..'Z')+;

/*STRING: '"' (NAME)? '"';*/

/*
fragment
PATH: PATH_COMPONENT (PERIOD PATH_COMPONENT)*;
*/

fragment
PATH_COMPONENT: ('a'..'z' | 'A'..'Z')+;

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

/** Parser Rules **/

// A file is a list of statements
// followed by an EOF
exprlist returns [List<String> code_list]
@init{$code_list = new ArrayList<String>();}
    : ( statement_term {$code_list.add($statement_term.code);} )* (WS)? EOF!
    ;

// A statement_term is a statement followed
// by a semicolon
statement_term returns [String code]
    : statement SEMICOLON {$code = $statement.code;}
    ;

statement returns [String code]
    : val_assignment {$code = $val_assignment.code; }
/*    | function_call {$code = $function_call.code; }*/
    | function_def {$code = $function_def.code; }
    | import_statement {$code = $import_statement.code; }
    | expression {$code = $expression.code; }
    ;

import_statement returns [String code]
@init{$code = "(import '"; }
@after{$code += ")"; }
    : 'import ' a=ID {$code += $a.text;} (PERIOD b=ID {$code += "." + $b.text;} )*
    ;

expression returns [String code]
    : arithmetic_expression {$code = $arithmetic_expression.code; }
    ;

arithmetic_expression returns [String code]
@init{$code = ""; }
    : a=arithmetic_term {$code = $a.code;} ( ( PLUS {$code = "(+ " + $code + " ";} | MINUS {$code = "(- " + $code + " ";} ) b=arithmetic_term {$code += $b.code + ")";} )*
    ;

arithmetic_term returns [String code]
@init{$code = ""; }
    : a=expression_atom {$code = $a.code;} ( ( MULT {$code = "(* " + $code + " ";} | DIV {$code = "(/ " + $code + " ";} ) b=expression_atom {$code += $b.code + ")";} )*
    ;

fragment
expression_atom returns [String code]
    : NUMBER {$code = $NUMBER.text;}
    | ID {$code = $ID.text; }
    | STRING {$code = $STRING.text; }
    | clojure_list {$code = $clojure_list.code; }
    | clojure_map {$code = $clojure_map.code; }
    | function_call {$code = $function_call.code; }
    | method_call {$code = $method_call.code; }
    | clojure_get {$code = $clojure_get.code; }
    ;

expression_list returns [List<String> code_list]
@init{$code_list = new ArrayList<String>();}
    :  a=expression {$code_list.add($a.code);} (COMMA! b=expression { $code_list.add($b.code);})+
    ;

val_assignment returns [String code]
    : 'val' ID '=' expression { $code = "(def " + $ID.text + " " + $expression.code + ")"; }
    ;

function_def_params returns [List<String> code_list]
@init{$code_list = new ArrayList<String>();}
    :  (a=ID COMMA! {$code_list.add($a.text);})* b=ID { $code_list.add($b.text);}
    ;

function_def returns [String code]
    : 'defn' ID '(' function_def_params ')' '{' expression '}' {
            $code = "(defn " + $ID.text + "[";
            for(int i=0; i < $function_def_params.code_list.size(); ++i) { $code += " " + $function_def_params.code_list.get(i); }
            $code += "]";
            $code += " " + $expression.code;
            $code += ")";
        }
    ;

function_call returns [String code]
    : (ID '(' expression COMMA!) => ID '(' expression_list ')' {
            $code = "(" + $ID.text;
            for(int i=0; i < $expression_list.code_list.size(); ++i) {
                $code += " " + $expression_list.code_list.get(i);
            }
            $code += ")";
        }
    | ID '(' expression ')' { $code = "(" + $ID.text + " " + $expression.code + ")"; }
    /*| ID '(' ')' { $code = "(" + $ID.text + ")"; }*/
    ;

/**
Method calls are inverted functions:

obj.func(x, y, z) <--> func(obj, x, y, z)

*/
method_call returns [String code]
    : (ID PERIOD ID '(' expression COMMA ) => obj=ID PERIOD func=ID '(' expression_list ')' {
            $code = "(" + $func.text + " " + $obj.text;
            for(int i=0; i < $expression_list.code_list.size(); ++i) {
                $code += " " + $expression_list.code_list.get(i);
            }
            $code += ")";
        }
    | obj=ID PERIOD func=ID '(' expression ')' { $code = "(" + $func.text + " " + $obj.text + " " + $expression.code + ")"; }
    /*| obj=ID PERIOD func=ID  '(' ')' { $code = "(" + $func.text + " " + $obj.text + ")"; }*/
    ;

clojure_list returns [String code]
@init{$code = "["; }
@after{$code += "]"; }
    : '[' a=expression {$code += $a.code;} (COMMA WS? b=expression {$code += ", " + $b.code;})* ']'
    ;

clojure_map returns [String code]
@init{$code = "{"; }
@after{$code += "}"; }
    : '{' a=expression COLON b=expression {$code += $a.code + " " + $b.code;} (COMMA WS? c=expression COLON d=expression {$code += " " + $c.code + " " + $d.code;})* '}'
    ;

clojure_get returns [String code]
    : a=ID '[' b=expression ']' {$code = "(get " + $a.text + " " + $b.code + ")";}
    ;




