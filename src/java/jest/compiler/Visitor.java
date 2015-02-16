
import jest.grammar.JestParser;
import jest.grammar.JestBaseVisitor;

import java.lang.String;

public class Visitor extends JestBaseVisitor<String> {


    @Override
    public String visitSourceCode(JestParser.SourceCodeContext ctx) {

        return visitChildren(ctx);
    }

}
