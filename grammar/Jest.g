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
// or a python style hash comment
LineComment
    :   '[//,#]' ~('\n'|'\r')* //NEWLINE
        {System.out.println("lc > " + getText());
        skip();}
    ;
*/
// A comment is a c-style single-line comment
// or a python style hash comment
/*
WS
    :   ('[ \n\t\r]')+ -> skip
    ;
*/
/*
WS  :   (' '
        |   '\t'
        |   ('\n'|'\r'('\n')) {newline();}
        )+
    ;
*/

/*
WS     :
        (' '
        | '\\t'
        | '\\r' '\\n' { newline(); }
        | '\\n'      { newline(); }
        )
        { $setType(Token.SKIP); } ;
*/
// WS : '[ \r\t\n]+' -> skip ;

//WS  :   ( ' ' | '\t' | '\r' | '\n')+ {$channel=HIDDEN;} -> skip ;

// A file is a list of statements
// followed by an EOF
exprlist
    : ( statement )* EOF!
    ;

// A statement is an expression followed
// by a semicolon
statement
    : expression SEMICOLON
    ;

expression : (val_assignment | foobar);

foobar
    : ('FOOBAR')+;

val_assignment
    : 'val' ID '=' integer_number -> ^(ID integer_number);

ID: ('a'..'z' | 'A'..'Z')+;

integer_number
    :   DIGIT+;

fragment
DIGIT   :   ('0'..'9');

fragment SEMICOLON : ';';
