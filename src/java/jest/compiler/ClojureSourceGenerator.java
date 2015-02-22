package jest.compiler;

import com.google.common.collect.Lists;
import java.util.List;
import jest.grammar.JestParser;
import jest.grammar.JestBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;


public class ClojureSourceGenerator extends JestBaseVisitor<Code> {

    public static class ClojureSourceGeneratorException extends RuntimeException {
        public ClojureSourceGeneratorException(String message) {
            super(message);
        }
    }

    public class BadSource extends ClojureSourceGeneratorException {
        public BadSource(ParserRuleContext context) {
            super(String.format("Error - %s", getLineInfo(context)));
        }
    }

    public static String getLineInfo(ParserRuleContext context) {
        return String.format("%s (Line: %s Character: %s)",
                context.getText(), context.start.getLine(), context.start.getCharPositionInLine());
    }

    @Override
    public Code visitSourceCode(JestParser.SourceCodeContext ctx) {

        List<String> codeList = Lists.newArrayList();

        // Add import statements
        for (JestParser.ImportStatementContext importStatement : ctx.importStatement()) {
            codeList.addAll(this.visitImportStatement(importStatement).getLines());
        }

        // Add Statement Terms
        for (JestParser.StatementTermContext statementTerm : ctx.statementTerm()) {
            codeList.addAll(this.visitStatementTerm(statementTerm).getLines());
        }

        return Code.multiLine(codeList);
    }


    @Override
    public Code visitStatementTerm(JestParser.StatementTermContext ctx) {

        if (ctx.statement() != null) {
            return this.visitStatement(ctx.statement());
        } else if (ctx.functionDef() != null) {
            return this.visitFunctionDef(ctx.functionDef());
        } else if (ctx.recordDef != null) {
            return this.visitRecordDef(ctx.recordDef());
        } else if (ctx.block() != null) {
            return this.visitBlock(ctx.block());
        } else if (ctx.varScope() != null) {
            return this.visitVarScope(ctx.varScope());
        } else {
            throw new BadSource(ctx);
        }
    }

    @Override
    public Code visitImportStatement(JestParser.ImportStatementContext ctx) {

        String code = "(import '";
        for (TerminalNode node : ctx.ID()) {
            code += node.getText();
        }
        code += ")";

        return Code.singleLine(code);
    }

    @Override
    public Code visitStatement(JestParser.StatementContext ctx) {

        if (ctx.expression() != null) {
            return this.visitExpression(ctx.expression());
        } else if (ctx.defAssignment() != null) {
            return this.visitDefAssignment(ctx.defAssignment());
        } else {
            throw new BadSource(ctx);
        }
    }

    @Override
    public Code visitDefAssignment(JestParser.DefAssignmentContext ctx) {

        String code = "";

        if (ctx.typeAnnotation() != null) {
            code += this.visitTypeAnnotation(ctx.typeAnnotation()).getSingleLine() + "\n";
        }

        code += String.format("(def %s %s)",
                ctx.name.getText(), this.visitExpression(ctx.expression()).getSingleLine());

        return Code.singleLine(code);

    }

    @Override
    public Code visitExpression(JestParser.ExpressionContext ctx) {
        return Code.singleLine(ctx.code);
    }

}
