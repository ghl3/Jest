
package jest.compiler;

import jest.Exception.ValidationException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import org.apache.commons.lang3.exception.ExceptionUtils;

import jest.grammar.JestLexer;
import jest.grammar.JestParser;


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
        return parser.sourceCode();
    }


    public static void isValidOrThrow(String source) throws ValidationException {
        ParseTree tree = compileSourceCodeToParseTree(source);
        isValidOrThrow(tree);
    }


    public static void isValidOrThrow(ParseTree tree) throws ValidationException {
        ParseTreeWalker.DEFAULT.walk(new Validator(), tree);
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
     * @throws ValidationException
     */
    public static Boolean isParseTreeValid(ParseTree tree) {
        try {
            isValidOrThrow(tree);
            return true;
        } catch (ValidationException e) {
            System.out.println(String.format("Encountered Error during Validation: %s", e.getClass()));
            System.out.println(e.getMessage());

            String space = "-------------------------";
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(String.format("\n%s\n%s%s\n", space, stackTrace, space));
            //e.printStackTrace()
            return false;
        }
    }


    /**
     * Takes a string of Jest code and
     * validates it.  If it is invalid,
     * this method prints validation errors
     * and returns false.  Else, it returns true.
     * @param source
     * @return
     * @throws ValidationException
     */
    public static Boolean isSourceCodeValid(String source) {
        ParseTree tree = compileSourceCodeToParseTree(source);
        return isParseTreeValid(tree);
    }

}
