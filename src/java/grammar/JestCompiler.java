
package jest.grammar;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
//import org.antlr.CommonAST;

import jest.grammar.*;

public class JestCompiler {

    public static CommonTree compile(String expression) {
        try {
            //lexer splits input into tokens
            ANTLRStringStream input = new ANTLRStringStream(expression);
            TokenStream tokens = new CommonTokenStream(new JestLexer(input));

            //parser generates abstract syntax tree
            JestParser parser = new JestParser(tokens);
            JestParser.source_code_return ret = parser.source_code();

            //acquire parse result
            CommonTree ast = (CommonTree) ret.tree;
            return ast;

        } catch (RecognitionException e) {
            throw new IllegalStateException("Recognition exception is never thrown, only declared.");
        }
    }


    public static java.util.List<String> parseSourceFile(String source)
        throws org.antlr.runtime.RecognitionException{

        // create an instance of the lexer
        JestLexer lexer = new JestLexer(new ANTLRStringStream(source));

        // wrap a token-stream around the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create the parser
        JestParser parser = new JestParser(tokens);

        // invoke the entry point of our grammar
        JestParser.source_code_return code = parser.source_code();
        java.util.List<String> data = new java.util.ArrayList<String>();

        for (int i=0; i < code.code_list.size(); ++i) {
            data.add(code.code_list.get(i));
        }

        return data;
    }

    public static String parseExpression(String source)
        throws org.antlr.runtime.RecognitionException{

        // create an instance of the lexer
        JestLexer lexer = new JestLexer(new ANTLRStringStream(source));

        // wrap a token-stream around the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create the parser
        JestParser parser = new JestParser(tokens);

        // Return the parsed result as an expression
        return parser.expression().code;
    }


    private static void printTree(CommonTree ast) {
        print(ast, 0);
    }


    private static void print(CommonTree tree, int level) {
        //indent level
        for (int i = 0; i < level; i++)
            System.out.print("--");

        //print node description: type code followed by token text
        System.out.println(" " + tree.getType() + " " + tree.getClass().getSimpleName() + " " + tree.getText());

        //print all children
        if (tree.getChildren() != null)
            for (Object ie : tree.getChildren()) {
                print((CommonTree) ie, level + 1);
            }
    }


    private static void printLisp(CommonTree tree, int level) {
        //indent level
        for (int i = 0; i < level; i++) {
            System.out.print(" ");
        }

        //print node description: type code followed by token text
        System.out.println("( " + tree.getText());

        //print all children
        if (tree.getChildren() != null) {
            for (Object ie : tree.getChildren()) {
                printChild((CommonTree) ie, level + 1);
            }
        }

        System.out.println(") ");
    }


    private static void printChild(CommonTree tree, int level) {
        //indent level
        for (int i = 0; i < level; i++) {
            System.out.print(" ");
        }

        //print node description: type code followed by token text
        System.out.println(tree.getText());

        //print all children
        if (tree.getChildren() != null) {
            for (Object ie : tree.getChildren()) {
                print((CommonTree) ie, level + 1);
            }
        }
    }
}
