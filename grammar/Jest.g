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
exprlist
    : ( statement )* (WS)? EOF!
    ;

// A statement is an expression followed
// by a semicolon
statement
    : expression SEMICOLON
    ;

expression : (val_assignment | function_call);

val_assignment
    : VAL ID '=' INTEGER_NUMBER -> ^(ID INTEGER_NUMBER);

paramdefs
    :  (ID COMMA!)* ID
    ;

function_call
    : ID '(' paramdefs ')' -> ^(ID paramdefs);

