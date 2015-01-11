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
// or a python style hash comment
LineComment
    :   '[//,#]' ~('\n'|'\r')* //NEWLINE
        {System.out.println("lc > " + getText());
        skip();}
    ;

// Whitespace that we ignore
// Tabs and newlines
WS     :
        (' '
        | '\\t'
        | '\\r' '\\n' { newline(); }
        | '\\n'      { newline(); }
        )
        { $setType(Token.SKIP); } ;


// A file is a list of statements
// followed by an EOF
exprlist
    : ( statement )* EOF!
    ;

// A statement is an expression followed
// by a semicolon
statement
    : expression SEMICOLON!
    ;

ID: ('a'..'z' | 'A'..'Z')+;

fragment
DIGIT   :   ('0'..'9');

integer_number
    :   DIGIT+;

val_assignment
    : "val" ID '=' integer_number -> ^(ID integer_number);


/*    : "val" ID "=" integer_number;*/

/*
function_call
    : "func" LPAREN! func_params RPAREN!


func_params
        :  (ID COMMA)* ID -> ^(PARAMS ID+)
        ;
*/
/*
funcdef
        :  ID  '(' paramdefs ')' '=' expr   -> ^(FUNC ID paramdefs expr)
        ;
*/

/*expression : (val_assignment | function_call | funcdef)*/

expression : (val_assignment);
