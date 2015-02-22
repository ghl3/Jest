package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import jest.grammar.JestParser;
import jest.grammar.JestBaseVisitor;
import jest.grammar.JestParser.ImportStatementContext;
import org.antlr.v4.runtime.tree.TerminalNode;


public class Visitor extends JestBaseVisitor<Code> {



    @Override
    public Code visitSourceCode(JestParser.SourceCodeContext ctx) {

        List<String> codeList = Lists.newArrayList();

        // Add import statements
        for (JestParser.ImportStatementContext importStatement: ctx.importStatement()) {
            codeList.addAll(this.visitImportStatement(importStatement).getLines());
        }

        // Add Statement Terms
        for (JestParser.StatementTermContext statementTerm: ctx.statementTerm()) {
            codeList.addAll(this.visitStatementTerm(statementTerm).getLines());
        }

        return Code.multiLine(codeList);
    }


    @Override
    public Code visitImportStatement(JestParser.ImportStatementContext ctx) {

        String code = "(import '";

        for (TerminalNode node: ctx.ID()) {
            code += node.getText();
        }

        code += ")";

        return Code.singleLine(code);
    }


    @Override
    public Code visitStatementTerm(JestParser.StatementTermContext ctx) {
        return Code.empty();
    }


}
