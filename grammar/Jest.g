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

// A comment is a c-style single-line comment

/*
tokens {
  FUNC;
  PARAMS;
        }
*/

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

VAL: 'val';

ID: ('a'..'z' | 'A'..'Z')+;

INTEGER_NUMBER
    :   (DIGIT)+;

fragment
DIGIT   :   '0'..'9';

SEMICOLON : ';';

COMMA : ',';

/** Parser Rules **/

// A file is a list of statements
// followed by an EOF
exprlist returns [List<String> code_list]
@init{$code_list = new ArrayList<String>();}
    : ( statement {$code_list.add($statement.code);} )* (WS)? EOF!
    ;

// A statement is an expression followed
// by a semicolon
statement returns [String code]
    : expression SEMICOLON {$code = $expression.code;}
    ;

expression returns [String code]
    : val_assignment {$code = $val_assignment.code; }
    | function_call {$code = $function_call.code; }
    | function_def {$code = $function_def.code; }
    ;

val_assignment returns [String code]
    : VAL ID '=' INTEGER_NUMBER { $code = "(def " + $ID.text + " " + $INTEGER_NUMBER.text + ")"; }
    ;

paramdefs returns [List<String> code_list]
@init{$code_list = new ArrayList<String>();}
    :  (a=ID COMMA! {$code_list.add($a.text);})* b=ID { $code_list.add($b.text);}
    ;

function_call returns [String code]
    : ID '(' paramdefs ')' {
                $code = "(" + $ID.text;
                for(int i=0; i < $paramdefs.code_list.size(); ++i) { $code += " " + $paramdefs.code_list.get(i); }
                $code += ")";
        }
    ;

function_def returns [String code]
    : 'defn' ID '(' paramdefs ')' '{' expression '}' {
            $code = "(defn " + $ID.text + "[";
            for(int i=0; i < $paramdefs.code_list.size(); ++i) { $code += " " + $paramdefs.code_list.get(i); }
            $code += "]";
            $code += $expression.code;
            $code += ")";
        }
    ;

