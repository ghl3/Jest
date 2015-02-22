package jest.compiler;

import jest.grammar.JestParser;
import jest.grammar.JestBaseVisitor;


public class Visitor extends JestBaseVisitor<Code> {


    @Override
    public Code visitSourceCode(JestParser.SourceCodeContext ctx) {
        return visitChildren(ctx);
    }

}
