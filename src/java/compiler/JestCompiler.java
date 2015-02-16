
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


    public static JestParser createParser(String source) {
        // create an instance of the lexer
        JestLexer lexer = new JestLexer(new ANTLRInputStream(source));

        // wrap a token-stream around the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create the parser
        return new JestParser(tokens);

    }


    public static JestParser.Source_codeContext compileSourceCodeToAst(String source)
        throws org.antlr.v4.runtime.RecognitionException {

        JestParser parser = createParser(source);

        // Generate the AST of the source code
        JestParser.Source_codeContext sourceTree = parser.source_code();

        return sourceTree;
    }


    public static java.util.List<String> parseSourceFile(String source)
        throws org.antlr.v4.runtime.RecognitionException {
        return parseSourceFile(source, true);
    }

    public static java.util.List<String> parseSourceFile(String source, boolean validate)
        throws org.antlr.v4.runtime.RecognitionException {

        // Generate the AST of the source code
        JestParser.Source_codeContext sourceTree = compileSourceCodeToAst(source);

        if (validate) {
            boolean valid = validateAst(sourceTree);
            if (!valid) {
                return new java.util.ArrayList<String>();
            }
        }

        return sourceTree.code_list;
    }

    /**
       Parse a single jest expression into
       a Clojure expression and return the
       resulting Clojure code as a string.
     */
    public static String parseExpression(String source)
        throws org.antlr.v4.runtime.RecognitionException{

        JestParser parser = createParser(source);

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
        ParseTree tree = compileSourceCodeToAst(source);
        return validateAst(tree);
    }

}
