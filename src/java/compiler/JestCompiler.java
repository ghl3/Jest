
package jest.compiler;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import jest.grammar.*;

import jest.compiler.Validator;


public class JestCompiler {


    public static ParseTree compile(String expression) {
        try {

            JestLexer lexer = new JestLexer(new ANTLRInputStream(expression));

            // Get a list of matched tokens
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            //parser generates abstract syntax tree
            JestParser parser = new JestParser(tokens);
            ParseTree tree = parser.source_code(); //compilationUnit();

            return tree;

        } catch (RecognitionException e) {
            throw new IllegalStateException("Recognition exception is never thrown, only declared.");
        }
    }

    public static java.util.List<String> parseSourceFile(String source)
        throws org.antlr.v4.runtime.RecognitionException {
        return parseSourceFile(source, false);
    }

    public static java.util.List<String> parseSourceFile(String source, boolean validate)
        throws org.antlr.v4.runtime.RecognitionException {

        // create an instance of the lexer
        //JestLexer lexer = new JestLexer(new ANTLRStringStream(source));
        JestLexer lexer = new JestLexer(new ANTLRInputStream(source));

        // wrap a token-stream around the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create the parser
        JestParser parser = new JestParser(tokens);

        // Generate the AST of the source code
        JestParser.Source_codeContext sourceTree = parser.source_code();

        //ParseTree tree = parser.source_code();

        if (validate) {
            boolean valid = validateAst(sourceTree);
            if (!valid) {
                return new java.util.ArrayList<String>();
            }
        }

        return sourceTree.code_list; //parser.source_code().code_list;
    }

    /**
       Parse a single jest expression into
       a Clojure expression and return the
       resulting Clojure code as a string.
     */
    public static String parseExpression(String source)
        throws org.antlr.v4.runtime.RecognitionException{

        // create an instance of the lexer
        //JestLexer lexer = new JestLexer(new ANTLRStringStream(source));
        JestLexer lexer = new JestLexer(new ANTLRInputStream(source));

        // wrap a token-stream around the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create the parser
        JestParser parser = new JestParser(tokens);

        // Return the parsed result as an expression
        return parser.expression().code;
    }


    public static Boolean validateAst(ParseTree tree) {
        try {
            ParseTreeWalker.DEFAULT.walk(new Validator(), tree);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }


    public static Boolean validateSourceCode(String source) {

        JestLexer lexer = new JestLexer(new ANTLRInputStream(source));
        JestParser parser = new JestParser(new CommonTokenStream(lexer));
        ParseTree tree = parser.source_code();

        return validateAst(tree);
    }

}
