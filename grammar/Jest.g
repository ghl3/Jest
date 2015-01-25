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
Comment
    :   '//' ~('\n'|'\r')* ('\n'|'\r') {skip();}
    |  '/*' .* '*/' {skip();}
    ;

WS  :   (' '
        |   '\t'
        |   ('\n'|'\r'('\n'))
        )+
        {$channel=HIDDEN;}
    ;

/** Lexer Rules **/

ARROW: '->' ;

PLUS:     '+' ;
MINUS:    '-' ;
MULT:     '*' ;
DIV:      '/' ;

STRING : '"' ~('\r' | '\n' | '"')* '"' ;

VAL: 'val';

LET: 'let';

DEFN: 'defn';

FOR: 'for';

LAZY: 'lazy';

IF: 'if';

ELSE: 'else';

ELIF: 'elif';

GT: '>' ;

GTEQ: '>=' ;

LT: '<' ;

LTEQ: '<=' ;

CPEQ: '==';

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

/** Parser Rules **/

// A file is a list of statements
// followed by an EOF
source_code returns [List<String> code_list]
@init{$code_list = new ArrayList<String>();}
    : statement_list {for (String str: $statement_list.code_list) { $code_list.add(str); }} EOF!
    ;

statement_list returns [List<String> code_list]
@init{$code_list = new ArrayList<String>();}
    : ( statement_term {$code_list.add($statement_term.code);} )* (WS)?
    ;

// A statement_term is a statement followed
// by a semicolon
statement_term returns [String code]
    : statement SEMICOLON {$code = $statement.code;}
    | function_def {$code = $function_def.code; }
    ;

statement returns [String code]
    : val_assignment {$code = $val_assignment.code; }
    | import_statement {$code = $import_statement.code; }
    | expression {$code = $expression.code; }
    ;

import_statement returns [String code]
@init{$code = "(import '"; }
@after{$code += ")"; }
    : 'import ' a=ID {$code += $a.text;} (PERIOD b=ID {$code += "." + $b.text;} )*
    ;

expression returns [String code]
    : comparison_expression {$code = $comparison_expression.code; }
    ;


comparison_expression returns [String code]
    : (arithmetic_expression GT) => a=arithmetic_expression {$code=$a.code;} (GT b=arithmetic_expression {$code="(> "+$a.code+" "+$b.code+")";})?
    | (arithmetic_expression LT) => a=arithmetic_expression {$code=$a.code;} (LT b=arithmetic_expression {$code="(< "+$a.code+" "+$b.code+")";})?
    | (arithmetic_expression GTEQ) => a=arithmetic_expression {$code=$a.code;} (GTEQ b=arithmetic_expression {$code="(>= "+$a.code+" "+$b.code+")";})?
    | (arithmetic_expression LTEQ) => a=arithmetic_expression {$code=$a.code;} (LTEQ b=arithmetic_expression {$code="(<= "+$a.code+" "+$b.code+")";})?
    | (arithmetic_expression CPEQ) => a=arithmetic_expression {$code=$a.code;} (CPEQ b=arithmetic_expression {$code="(= "+$a.code+" "+$b.code+")";})?
    | (arithmetic_expression)=> a=arithmetic_expression {$code=$a.code;}
    ;


arithmetic_expression returns [String code]
@init{$code = ""; }
    : a=arithmetic_term {$code = $a.code;} ( ( PLUS {$code = "(+ " + $code + " ";} | MINUS {$code = "(- " + $code + " ";} ) b=arithmetic_term {$code += $b.code + ")";} )*
    ;

arithmetic_term returns [String code]
@init{$code = ""; }
    : a=expression_composed {$code = $a.code;} ( ( MULT {$code = "(* " + $code + " ";} | DIV {$code = "(/ " + $code + " ";} ) b=expression_composed {$code += $b.code + ")";} )*
    ;


fragment
expression_composed returns [String code]
    : (expression_atom (PERIOD|ARROW)) => method_call_chain{$code=$method_call_chain.code;}
    | expression_atom {$code=$expression_atom.code;}
    ;

method_call_chain returns [String code]
    : (method_call (PERIOD|ARROW)) => method_call {$code=$method_call.code;}
        (
            PERIOD a=ID b=method_params {$code="("+$a.text+" "+$code+$b.code+")";} |
            ARROW c=ID d=method_params {$code="("+$c.text+$d.code+" "+$code+")";}
        )+
    | method_call {$code=$method_call.code;}
    ;


/*
Method calls are inverted functions
obj.func(x, y, z) <--> func(obj, x, y, z)
They can take any expression atom and transform it
into a function call on that atom
*/

method_call returns [String code]
    : ( expression_atom PERIOD ID method_params) => obj=expression_atom PERIOD func=ID method_params { $code = "(" + $func.text + " " + $obj.code + $method_params.code + ")"; }
    | ( expression_atom ARROW ID method_params) => obj=expression_atom ARROW func=ID method_params { $code = "(" + $func.text + $method_params.code + " " + $obj.code + ")"; }
    ;


/*
An expression atom is an undividable component
of an expression.  Each atom itself is an expression
but can be composed with each other (using other sytax)
to create more complicated expressions
(The obvious exception is the final line, but that
is present to make the code simpler)
*/

fragment
expression_atom returns [String code]
    : NUMBER {$code = $NUMBER.text;}
    | ID {$code = $ID.text; }
    | STRING {$code = $STRING.text; }
    | SYMBOL {$code = $SYMBOL.text; }
    | clojure_vector {$code = $clojure_vector.code; }
    | clojure_map {$code = $clojure_map.code; }
    | function_call {$code = $function_call.code; }
    | clojure_get {$code = $clojure_get.code; }
    | for_loop {$code = $for_loop.code; }
    | conditional {$code = $conditional.code; }
    | let_statement {$code = $let_statement.code; }
    | lambda {$code=$lambda.code;}
    | '(' expression ')' {$code = $expression.code; }
    ;

expression_list returns [List<String> code_list]
@init{$code_list = new ArrayList<String>();}
    :  a=expression {$code_list.add($a.code);} (COMMA! b=expression { $code_list.add($b.code);})+
    ;

/* Consider adding '/t' as a prefix to all of these
   and remove the hash notation to prefix that by hand*/

type_annotation returns [String code]
    : type=ID  {$code=$type.text;}
    | ('#' ID) => '#' type=ID  {$code="t/" + $type.text;}
    | typeleft=ID num=NUMBER  {$code=$typeleft.text + " " + $num.text;}
    | '(' thing=type_annotation ')'  {$code = "(" + $thing.code + ")";}
    | container=ID {$code = "(t/" + $container.text;} '[' (inner=type_annotation {$code += " " + $inner.code;})+ ']' {$code += ")";}
        /* This is a bit of a hack to get nested containers to work */
        /* The issue is that in jest: HVec[[(?) (?) (?)]] coudl be parsed */
        /* by allowing a '[' type_annotation ']' branch, but this breaks */
        /* the jest container branch style below */
    | container=ID {$code = "(t/" + $container.text + " [";} '[[' (inner=type_annotation {$code += " " + $inner.code;})+ ']]' {$code += "])";}
    ;

func_type_annotation returns [String code]
    : first=type_annotation {$code = $first.code;} (next=type_annotation {$code += " " + $next.code;})*
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
    : ('#(' .* '%' .* ')')=> '#(' expression ')' {
            if ($expression.code.length() > 2 && $expression.code.matches("[(].*[)]")) { //get(0)=="(" && $expression.code.get(expressions.length()-1)==")") {
                $code="#"+$expression.code;
            } else {
                $code="#("+$expression.code+")";
            }
        }
    ;

val_assignment returns [String code]
@init{
    String annotation = "";
}
@after {
    $code = annotation + $code;
}
    : VAL name=ID
      (COLON type=type_annotation {annotation = "(t/ann " + $name.text + " " + $type.code +  ")\n";})?
      '=' expression { $code = "(def " + $name.text + " " + $expression.code + ")"; }
    ;


let_statement returns [String code]
@init{
    String annotation = "";
}
@after {
    $code = annotation + $code;
}
    : LET '(' VAL name=ID '=' exp=expression {$code="(let [" + $name.text + " " + $exp.code;}
           (SEMICOLON VAL next_name=ID '=' next_exp=expression {$code += "\n" + $next_name.text + " " + $next_exp.code;})* (SEMICOLON)?
           ')' block {$code += "] \n" + $block.code + ")";}
    ;


function_def_params returns [List<String> code_list]
@init{$code_list = new ArrayList<String>();}
    :  (a=ID COMMA! {$code_list.add($a.text);})* b=ID { $code_list.add($b.text);}
    ;

function_def returns [String code]
@init{
    String annotation = "";
    $code = "(defn ";}
@after{
    $code += ")";
    $code = annotation + $code;
}
    : DEFN name=ID '(' function_def_params ')' {
            $code = "(defn " + $name.text;
            $code += " [";
            for(int i=0; i < $function_def_params.code_list.size(); ++i) {
                $code += " " + $function_def_params.code_list.get(i);
            }
            $code += " ]";
        }
        (COLON {annotation = "(t/ann " + $name.text + " [";} a=func_type_annotation { annotation += $a.code + " ";}
         ARROW c=type_annotation {annotation += "-> " + $c.code + "])\n";})?
         block {$code+=$block.code;} /*
        '{' (statement_term { $code += "\n\t" + $statement_term.code; } )+ '}'*/ (SEMICOLON)?
    ;


function_call returns [String code]
    : ID method_params { $code = "(" + $ID.text + $method_params.code + ")"; }
    ;


/*
 Parameter for a function or method call
 is either an empty string or, if parameters are
 present, includes leading whitespace so it can be
 directly added to any previous text
*/

method_params returns [String code]
    : ( '(' ')') => '(' ')' { $code = "";}
    | ( '(' expression COMMA ) => '(' expression_list ')' {
            $code = "";
            for(int i=0; i < $expression_list.code_list.size(); ++i) {
                $code += " " +$expression_list.code_list.get(i);
            }
        }
    | '(' expression ')' { $code = " " + $expression.code; }
    ;


for_loop returns [String code]
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
      (LAZY {lazy=true;})? block { func += $block.code;} /*'{' (statement_term {func += "\n\t" + $statement_term.code;})+ '}'*/
    ;

block returns [String code]
    : ('{' expression '}') => '{' expression {$code=$expression.code;} '}'
    | '{' {$code="";} (statement_term {$code += "\n\t" + $statement_term.code;})+ '}'
    ;


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
    : IF '(' if_condition=expression ')' iftrue=block {conditions.add($if_condition.code);results.add($iftrue.code);}
      (ELIF '(' elif_expression=expression ')' elif_block=block {conditions.add($elif_expression.code);results.add($elif_block.code);})*
       (ELSE else_block=block {results.add($else_block.code);})?
    ;


clojure_vector returns [String code]
@init{$code = "["; }
@after{$code += "]"; }
    : '[' ']'
    | '[' a=expression {$code += $a.code;} (COMMA WS? b=expression {$code += ", " + $b.code;})* ']'
    ;

clojure_map returns [String code]
@init{$code = "{"; }
@after{$code += "}"; }
    : '{' '}'
    | '{' a=expression COLON b=expression {$code += $a.code + " " + $b.code;} (COMMA WS? c=expression COLON d=expression {$code += " " + $c.code + " " + $d.code;})* '}'
    ;

clojure_get returns [String code]
    : a=ID '[' b=expression ']' {$code = "(get " + $a.text + " " + $b.code + ")";}
    ;
