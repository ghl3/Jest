package jest.compiler;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.List;
import jest.grammar.JestParser;
import jest.grammar.JestBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
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
    public Code visitMethodCallChain(JestParser.MethodCallChainContext ctx) {

        if (ctx.methodCall() != null) {
            return this.visitMethodCall(ctx.methodCall);
        }

        String methodCallChain = this.visitMethodCallChain(ctx.methodCallChain())
                .getSingleLine();

        if (ctx.PERIOD() != null) {
            String code = String.format("(%s %s %s)",
                    ctx.a.getText(),
                    methodCallChain,
                    this.visitMethodParams(ctx.b).getSingleLine());
            return Code.singleLine(code);
        }

        else if (ctx.ARROW() != null) {
            String code = String.format("(%s %s %s)",
                    ctx.c.getText(),
                    this.visitMethodParams(ctx.d).getSingleLine(),
                    methodCallChain);
            return Code.singleLine(code);
        }

        else {
            throw new BadSource(ctx);
        }
    }


    @Override
    public Code visitMethodCall(JestParser.MethodCallContext ctx) {

        if (ctx.PERIOD() != null) {
            String code = String.format("(%s %s%s)",
                    ctx.func.getText(),
                    this.visitExpressionAtom(ctx.obj).getSingleLine(),
                    this.visitMethodParams(ctx.methodParams()).getSingleLine());
            return Code.singleLine(code);
        }

        else if (ctx.ARROW() != null) {
            String code = String.format("(%s%s %s)",
                    ctx.func.getText(),
                    this.visitMethodParams(ctx.methodParams()).getSingleLine(),
                    this.visitExpressionAtom(ctx.obj).getSingleLine());
            return Code.singleLine(code);
        }

        else {
            throw new BadSource(ctx);
        }
    }


    @Override
    public Code visitExpressionAtom(JestParser.ExpressionAtomContext ctx) {

        if (ctx.NUMBER() != null) {
            return Code.singleLine(ctx.NUMBER().getText());
        }
        else if (ctx.TRUE() != null) {
            return Code.singleLine(ctx.TRUE().getText());
        }
        else if (ctx.FALSE() != null) {
            return Code.singleLine(ctx.FALSE().getText());
        }
        else if (ctx.NIL() != null) {
            return Code.singleLine(ctx.NIL().getText());
        }
        else if (ctx.ID() != null) {
            return Code.singleLine(ctx.ID().getText());
        }
        else if (ctx.STRING() != null) {
            return Code.singleLine(ctx.STRING().getText());
        }
        else if (ctx.SYMBOL() != null) {
            return Code.singleLine(ctx.SYMBOL().getText());
        }
        else if (ctx.clojureVector() != null) {
            return this.visitClojureVector(ctx.clojureVector());
        }
        else if (ctx.clojureMap() != null) {
            return this.visitClojureMap(ctx.clojureMap());
        }
        else if (ctx.functionCall() != null) {
            return this.visitFunctionCall(ctx.functionCall());
        }
        else if (ctx.clojureGet() != null) {
            return this.visitClojureGet(ctx.clojureGet());
        }
        else if (ctx.forLoop() != null) {
            return this.visitForLoop(ctx.forLoop());
        }
        else if (ctx.conditional() != null) {
            return this.visitConditional(ctx.conditional());
        }
        else if (ctx.lambda() != null) {
            return this.visitLambda(ctx.lambda());
        }
        else if (ctx.memberGetChain() != null) {
            return this.visitMemberGetChain(ctx.memberGetChain());
        }
        else if (ctx.recordConstructor() != null) {
            return this.visitRecordConstructor(ctx.recordConstructor());
        }
        else if (ctx.block() != null) {
            return this.visitBlock(ctx.block());
        }
        else if (ctx.expression() != null) {
            return this.visitExpression(ctx.expression());
        }
        else {
            throw new BadSource(ctx);
        }

    }


    @Override
    public Code visitMemberGetChain(JestParser.MemberGetChainContext ctx) {
        if (ctx.PERIOD() != null) {
            String code = String.format("(:%s %s)",
                    ctx.a.getText(),
                    this.visitMemberGet(ctx.memberGet()).getSingleLine());
            return Code.singleLine(code);
        } else {
            return this.visitMemberGet(ctx.memberGet());
        }
    }

    @Override
    public Code visitMemberGet(JestParser.MemberGetContext ctx) {
        String code = String.format("(:%s %s)",
                ctx.member.getText(),
                ctx.record.getText());
        return Code.singleLine(code);
    }


    @Override
    public Code visitRecordConstructor(JestParser.RecordConstructorContext ctx) {
        if (ctx.name == null) {
            String code = String.format("(->%s %s)",
                    ctx.ID,
                    this.visitMethodParams(ctx.methodParams()).getSingleLine());
            return Code.singleLine(code);
        } else {
            String code = String.format("(map->%s {", ctx.name.getText());
            code += String.format(":%s %s",
                    ctx.firstKey.getText(),
                    this.visitExpression(ctx.firstExp).getSingleLine());

            for (int i=0; i < ctx.key.size(); ++i) {
                String key = ctx.key.get(i).getText();
                String expr = this.visitExpression(ctx.exp.get(i)).getSingleLine();
                code += String.format(" :%s %s", key, expr);
            }
            code += "})";

            return Code.singleLine(code);
        }
    }


    @Override
    public Code visitExpressionList(JestParser.ExpressionListContext ctx) {

        String code = this.visitExpression(ctx.a).getSingleLine();

        for (JestParser.ExpressionContext expression: ctx.b) {
            code += " " + this.visitExpression(expression).getSingleLine();
        }

        return Code.singleLine(code);
    }


    @Override
    public Code visitTypeAnnotation(JestParser.TypeAnnotationContext ctx) {

        String code;

        if (ctx.typeleft != null) {
            code = String.format("%s %s",
                    ctx.typeleft.getText(),
                    ctx.num.getText());
        }

        else if (ctx.thing != null) {
            code = String.format("(%s)",
                    ctx.thing.getText());
        }

        else if (ctx.container != null) {
            code = String.format("(t/%s [", ctx.container.getText());
            for (JestParser.TypeAnnotationContext annotation: ctx.inner) {
                code += " " + this.visitTypeAnnotation(annotation).getSingleLine();
            }
            code += "])";
        }

        // TODO - Double Block

        else {
            throw new BadSource(ctx);
        }

        return Code.singleLine(code);
    }


    @Override
    public Code visitFuncTypeAnnotation(JestParser.FuncTypeAnnotationContext ctx) {

        String code = this.visitTypeAnnotation(ctx.first).getSingleLine();

        for (JestParser.TypeAnnotationContext annotation: ctx.typeAnnotation()) {
            code += " " + this.visitTypeAnnotation(annotation).getSingleLine();
        }

        return Code.singleLine(code);
    }


    @Override
    public Code visitLambda(JestParser.LambdaContext ctx) {

        String expression = this.visitExpression(ctx.expression).getSingleLine();

        String code;

        if (expression.length() > 2 && expression.matches("[(].*[)]")) { //get(0)=="(" && $expression.code.get(expressions.length()-1)==")") {
            code="#"+expression;
        } else {
            code="#("+expression+")";
        }

        return Code.singleLine(code);
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
    public Code visitMethodDef(JestParser.MethodDefContext ctx) {

        String annotation = "";

        if (ctx.typeAnnotation() != null) {
            annotation = String.format("(t/ann %s [%s ->%s])\n",
                    ctx.name.getText(),
                    this.visitFuncTypeAnnotation(ctx.a).getSingleLine(),
                    this.visitTypeAnnotation(ctx.c).getSingleLine());
        }

        String code = String.format("(%s [%s ] %s)",
                ctx.name.getText(),
                this.visitFunctionDefParams(ctx.functionDefParams()),
                this.visitBlock(ctx.block).getSingleLine());

       return Code.singleLine(annotation+code);
    }


    @Override
    public Code visitFunctionDefParams(JestParser.FunctionDefParamsContext ctx) {

        if (ctx.first == null) {
            return Code.singleLine("");
        }

        else {
            String code = String.format(" %s", ctx.first.getText());
            for (Token rest: ctx.rest) {
                code += String.format(" %s", rest.getText());
            }
            return Code.singleLine(code);
        }
    }

    @Override
    public Code visitFunctionCall(JestParser.FunctionCallContext ctx) {

        String code = String.format("(%s %s)",
                ctx.ID().getText(),
                this.visitMethodParams(ctx.methodParams()).getSingleLine());
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


    @Override
    public Code visitMethodParams(JestParser.MethodParamsContext ctx) {

        if (ctx.expressionList != null) {
            String code = String.format(" %s",
                    this.visitExpressionList(ctx.expressionList()).getSingleLine());
            return Code.singleLine(code);
        }

        else if (ctx.expression() != null) {
            return this.visitExpression(ctx.expression());
        }

        else {
            return Code.singleLine("");
        }
    }


    @Override
    public Code visitForLoop(JestParser.ForLoopContext ctx) {


        String func = "(fn ";
        String iterator = "";
        Boolean lazy = false;

        func += String.format("[ %s", ctx.a.getText());

        for (Token b: ctx.b) {
            func += String.format(" %s", b.getText());
        }

        func += " ]";

        iterator = String.format("(seq %s)",
                this.visitExpression(ctx.c).getSingleLine());

        for (JestParser.ExpressionContext d: ctx.d) {
            iterator += String.format(" (seq %s)",
                    this.visitExpression(d).getSingleLine());
        }

        if (ctx.LAZY() != null) {
            lazy = true;
        }

        func += this.visitBlock(ctx.block).getSingleLine();

        // AFTER
        String code;
        func += ") ";

        if (lazy) {
            code = "";
        } else {
            code = "(doall ";
        }

        code += "(map " + func + " " + iterator + ")";

        if (!lazy) code += ")";

        return Code.singleLine(code);
    }


    @Override
    public Code visitBlock(JestParser.BlockContext ctx) {

        if (ctx.expression != null) {
            return this.visitExpression(ctx.expression);
        }

        else if (ctx.term != null) {
            String code = "";
            for (JestParser.StatementTermContext term: ctx.term) {
                code += String.format(" %s",
                        this.visitStatementTerm(term).getSingleLine());
            }
            return Code.singleLine(code);
        }

        else if (ctx.scope != null) {
            String code = "";
            for (JestParser.VarScopeContext scope: ctx.scope) {
                code += String.format(" %s",
                        this.visitVarScope(scope).getSingleLine());
            }
            return Code.singleLine(code);
        }

        else {
            throw new BadSource(ctx);
        }
    }


    @Override
    public Code visitVarScope(JestParser.VarScopeContext ctx) {

        String code = String.format("(let [");

        for (int i=0; i < ctx.name.size(); ++i) {
            String name = ctx.name.get(i).getText();
            String expr = this.visitExpression(ctx.exp.get(i)).getSingleLine();
            code += String.format(" %s %s", name, expr);
        }
        code += " ]";

        for (JestParser.StatementTermContext term: ctx.terms) {
            code += String.format(" %s",
                    this.visitStatementTerm(term).getSingleLine());
        }
        code += ")";

        return Code.singleLine(code);
    }


    @Override
    public Code visitClojureVector(JestParser.ClojureVectorContext ctx) {

        if (ctx.a != null) {
            String code = "[";
            code += this.visitExpression(ctx.a).getSingleLine();

            for (JestParser.ExpressionContext expression: ctx.b) {
                code += ", " + this.visitExpression(expression).getSingleLine();
            }

            code += "]";
            return Code.singleLine(code);
        } else {
            return Code.singleLine("[]");
        }
    }



    @Override
    public Code visitClojureMap(JestParser.ClojureMapContext ctx) {

        if (ctx.a != null) {
            String code = "{";
            code += this.visitExpression(ctx.a).getSingleLine();
            code += " " + this.visitExpression(ctx.b).getSingleLine();

            for (int i=0; i < ctx.c.size(); ++i) {
                code += " " + this.visitExpression(ctx.c.get(i)).getSingleLine();
                code += " " + this.visitExpression(ctx.d.get(i)).getSingleLine();
            }

            code += "}";
            return Code.singleLine(code);
        } else {
            return Code.singleLine("{}");
        }
    }


    @Override
    public Code visitClojureGet(JestParser.ClojureGetContext ctx) {
        String code = String.format("(get %s %s)",
                ctx.a.getText(),
                this.visitExpression(ctx.b));
        return Code.singleLine(code);
    }


}
