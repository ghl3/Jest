
package jest.compiler;

import java.util.List;
import jest.compiler.Validator.ValidationError;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import jest.grammar.*;


public class JestCompiler {


    /**
     * Take a source code string,
     * lex it to convert it to tokens,
     * and return a parser for those tokens
     * @param source
     * @return
     */
    public static JestParser createParser(String source) {
        // create an instance of the lexer
        JestLexer lexer = new JestLexer(new ANTLRInputStream(source));

        // wrap a token-stream around the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create the parser
        return new JestParser(tokens);
    }


    /**
     * Take a source code string,
     * create a parser for it,
     * and return a SourceCodeContext object
     * representing the root of the parse tree
     * @param source
     * @return
     * @throws org.antlr.v4.runtime.RecognitionException
     */
    public static JestParser.SourceCodeContext compileSourceCodeToParseTree(String source)
        throws org.antlr.v4.runtime.RecognitionException {

        JestParser parser = createParser(source);

        // Generate the AST of the source code
        JestParser.SourceCodeContext sourceTree = parser.sourceCode();

        return sourceTree;
    }

    /**
     * Take a source code string,
     * create a parser for it,
     * and return a SourceCodeContext object
     * representing the root of the parse tree
     * @param source
     * @return
     * @throws org.antlr.v4.runtime.RecognitionException
     */
    public static JestParser.ExpressionContext compileExpressionToParseTree(String source)
            throws org.antlr.v4.runtime.RecognitionException {

        JestParser parser = createParser(source);

        // Generate the AST of the source code
        JestParser.ExpressionContext expression = parser.expression();

        return expression;
    }


    /**
     * Takes a string of Jest code and
     * validates it.  If it is invalid,
     * this method prints validation errors
     * and returns false.  Else, it returns true.
     * @param source
     * @return
     * @throws ValidationError
     */
    public static Boolean validateSourceCode(String source) throws ValidationError {
        ParseTree tree = compileSourceCodeToParseTree(source);
        return validateParseTree(tree);
    }


    /**
     * Takes a Jest ParseTree and validates it.
     * If a validation error is found, that error is
     * printed and the method returns false.
     * Else, it returns true.
     * Validation includes ensuring variable scope
     * is consistent, etc.
     * @param tree
     * @return
     * @throws ValidationError
     */
    public static Boolean validateParseTree(ParseTree tree) {
        try {
            ParseTreeWalker.DEFAULT.walk(new Validator(), tree);
            return true;
        } catch (ValidationError e) {
            System.out.println(e.getMessage());
            return false;
        }
    }


    public static List<String> parseSourceFile(String source) {
        ParseTree tree = compileSourceCodeToParseTree(source);
        return parseSourceFile(tree);
    }


    public static List<String> parseSourceFile(ParseTree tree) {
        ClojureSourceGenerator clojureSourceGenerator = new ClojureSourceGenerator();
        Code code = clojureSourceGenerator.visit(tree);
        return code.getLines();
    }


    public static String parseExpression(String expression) {
        ParseTree tree = compileExpressionToParseTree(expression);
        return parseExpression(tree);
    }


    public static String parseExpression(ParseTree tree) {
        ClojureSourceGenerator clojureSourceGenerator = new ClojureSourceGenerator();
        Code code = clojureSourceGenerator.visit(tree);
        return code.getSingleLine();
    }


/*
    public static List<String> parseSourceFileVisitor(String source, boolean validate) {
        ParseTree tree = compileSourceCodeToAst(source);
        //validateAst(tree);
        return parseSourceFileVisitor(tree);
    }
*/




    /*
    public static java.util.List<String> parseSourceFile(String source)
        throws org.antlr.v4.runtime.RecognitionException {
        return parseSourceFile(source, true);
    }
*/
/*
    public static java.util.List<String> parseSourceFile(String source, boolean validate)
        throws org.antlr.v4.runtime.RecognitionException {

        // Generate the AST of the source code
        JestParser.SourceCodeContext sourceTree = compileSourceCodeToAst(source);

        if (validate) {
            boolean valid = validateAst(sourceTree);
            if (!valid) {
                System.out.println("Source code is invalid");
                return new java.util.ArrayList<String>();
            }
        }

        return sourceTree.codeList;
    }
*/

    /**
       Parse a single jest expression into
       a Clojure expression and return the
       resulting Clojure code as a string.
     */
    /*
    public static String parseExpression(String source)
        throws org.antlr.v4.runtime.RecognitionException{

        JestParser parser = createParser(source);

        // Return the parsed result as an expression
        return parser.expression().code;
    }
*/




}
