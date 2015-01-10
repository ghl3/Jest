grammar S001HelloWord;

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

// ***************** lexer rules:
//the grammar must contain at least one lexer rule
SALUTATION:'Hello world';   
ENDSYMBOL:'!';

// ***************** parser rules:
//our grammar accepts only salutation followed by an end symbol
expression : SALUTATION ENDSYMBOL;
