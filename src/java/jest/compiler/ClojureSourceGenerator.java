package jest.compiler;

import com.google.common.base.Joiner;
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
        return this.visitComparisonExpression(ctx.comparisonExpression());
    }



    @Override
    public Code visitComparisonExpression(JestParser.ComparisonExpressionContext ctx) {
        if (ctx.op != null) {
            String code = String.format("(%s %s %s)",
                    ctx.op.getText(), ctx.a, ctx.b);
            return Code.singleLine(code);
        } else {
            return this.visitArithmeticExpression(ctx.a);
        }
    }


    @Override
    public Code visitArithmeticExpression(JestParser.ArithmeticExpressionContext ctx) {

        if (ctx.b != null) {
            String a = this.visitArithmeticTerm(ctx.a).getSingleLine();
            String b = this.visitArithmeticTerm(ctx.b).getSingleLine();
            String code = String.format("(%s %s %s)",
                    ctx.PLUS() != null ? ctx.PLUS() : ctx.MINUS(),
                   a, b);
            return Code.singleLine(code);
        } else {
            return this.visitArithmeticTerm(ctx.a);
        }
    }


    @Override
    public Code visitArithmeticTerm(JestParser.ArithmeticTermContext ctx) {
        if (ctx.b != null) {
            String a = this.visitExpressionComposed(ctx.a).getSingleLine();
            String b = this.visitExpressionComposed(ctx.b).getSingleLine();
            String code = String.format("(%s %s %s)",
                    ctx.MULT() != null ? ctx.MULT() : ctx.DIV(),
                    a, b);
            return Code.singleLine(code);
        } else {
            return this.visitExpressionComposed(ctx.a);
        }
    }


    @Override
    public Code visitExpressionComposed(JestParser.ExpressionComposedContext ctx) {

        if (ctx.methodCallChain() != null) {
            return this.visitMethodCallChain(ctx.methodCallChain());
        }

        else if (ctx.expressionAtom() != null) {
            return this.visitExpressionAtom(ctx.expressionAtom());
        }

        else {
            throw new BadSource(ctx);
        }
    }


    @Override
    public Code visitFunctionDef(JestParser.FunctionDefContext ctx) {

        String code = "";

        if (ctx.funcTypeAnnotation() != null) {
            //code += this.visitTypeAnnotation(ctx.typeAnnotation()).getSingleLine() + "\n";
            code += String.format("(t/ann %s [%s -> %s])\n",
                    ctx.name.getText(), ctx.funcTypeAnnotation().code, ctx.typeAnnotation().code);
        }

        code += String.format("(defn %s [%s ] %s)",
                ctx.name.getText(),
                ctx.functionDefParams.code,
                ctx.block().code);

        return Code.singleLine(code);
    }


    @Override
    public Code visitRecordDef(JestParser.RecordDefContext ctx) {

        List<String> ids = Lists.newArrayList();

        Integer numProtocols = ctx.IMPLEMENTS().size();
        Integer numMembers = ctx.ID().size() - numProtocols;

        // TODO: Does this loop over too many ids?
        for (TerminalNode id: ctx.ID().subList(0, numMembers)) {
            ids.add(id.getText());
        }

        String fields = Joiner.on(",").join(ids);

        String code = String.format("(defrecord %s [%s]",
                ctx.name.getText(), fields);

        if (ctx.protocol != null) {
           for (TerminalNode protocol: ctx.ID().subList(numMembers, numMembers + numProtocols)) {
               code += String.format("\n%s", protocol.getText());
           }
        }

        return Code.singleLine(code);

    }


}
