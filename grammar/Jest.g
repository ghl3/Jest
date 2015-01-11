grammar Jest;

options
{
  // antlr will generate java lexer and parser
  language = Java;
  // generated parser should create abstract syntax tree
  output = AST;
}

//as the generated lexer will reside in org.meri.antlr_step_by_step.parsers 
//package, we have to add package declaration on top of it
@lexer::header {
  package jest.grammar;
}

//as the generated parser will reside in org.meri.antlr_step_by_step.parsers 
//package, we have to add package declaration on top of it
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


val_assignment
    : val ID "=" expr


function_call
    : func(params)


funcdef
        :  ID  '(' paramdefs ')' '=' expr   -> ^(FUNC ID paramdefs expr)
        ;

params
        :  (ID COMMA)* ID                   -> ^(PARAMS ID+)
        ;


expression : (val_assignment | function_call | funcdef)
