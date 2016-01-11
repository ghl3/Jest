package jest.compiler;

import jest.Exception.BadSource;
import jest.Exception.NotExpressionError;
import jest.compiler.DeclaredTypes.BuiltInTypes;
import jest.compiler.DeclaredTypes.Type;
import jest.grammar.JestBaseVisitor;
import jest.grammar.JestParser.ArithmeticExpressionContext;
import jest.grammar.JestParser.ArithmeticTermContext;
import jest.grammar.JestParser.BlockContext;
import jest.grammar.JestParser.ClojureGetContext;
import jest.grammar.JestParser.ClojureMapContext;
import jest.grammar.JestParser.ClojureVectorContext;
import jest.grammar.JestParser.ComparisonExpressionContext;
import jest.grammar.JestParser.ConditionalContext;
import jest.grammar.JestParser.ExpressionAtomContext;
import jest.grammar.JestParser.ExpressionComposedContext;
import jest.grammar.JestParser.ExpressionContext;
import jest.grammar.JestParser.ExpressionListContext;
import jest.grammar.JestParser.ForLoopContext;
import jest.grammar.JestParser.FuncTypeAnnotationContext;
import jest.grammar.JestParser.FunctionCallContext;
import jest.grammar.JestParser.FunctionDefContext;
import jest.grammar.JestParser.FunctionDefParamsContext;
import jest.grammar.JestParser.ImplementationDefContext;
import jest.grammar.JestParser.LambdaContext;
import jest.grammar.JestParser.MemberGetChainContext;
import jest.grammar.JestParser.MemberGetContext;
import jest.grammar.JestParser.MethodCallChainContext;
import jest.grammar.JestParser.MethodCallContext;
import jest.grammar.JestParser.MethodDefContext;
import jest.grammar.JestParser.MethodParamsContext;
import jest.grammar.JestParser.RecordConstructorContext;
import jest.grammar.JestParser.RecordDefContext;

import jest.grammar.JestParser.StatementContext;
import jest.grammar.JestParser.StatementTermContext;
import jest.grammar.JestParser.TypeAnnotationContext;
import jest.grammar.JestParser.VarScopeContext;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static jest.util.combine;
import static jest.util.last;


/**
 * Takes an expression and returns the type of the expression
 */
public class ExpressionEvaluator extends JestBaseVisitor<Type> {

    final Scope scope;

    public ExpressionEvaluator(Scope scope) {
        this.scope = scope;
    }


    @Override
    public Type visitStatement(StatementContext ctx) {

        if (ctx.expression() != null) {
            return this.visitExpression(ctx.expression());
        } else if (ctx.defAssignment() != null) {
            throw new NotExpressionError(ctx);
        } else {
            throw new BadSource(ctx);
        }
    }

    @Override
    public Type visitExpression(ExpressionContext ctx) {
        return this.visitComparisonExpression(ctx.comparisonExpression());
    }

    @Override
    public Type visitComparisonExpression(ComparisonExpressionContext ctx) {
        if (ctx.op == null) {
            return this.visitArithmeticExpression(ctx.a);
        } else {
            return BuiltInTypes.Boolean;
        }
    }


    public static Type resolveArithmeticType(Iterable<ArithmeticTermContext> arithmeticTerms) {
        return BuiltInTypes.Number;
    }

    @Override
    public Type visitArithmeticExpression(ArithmeticExpressionContext ctx) {
        if (ctx.op == null || ctx.op.size()==0) {
            return this.visitArithmeticTerm(ctx.a);
        } else {
            return resolveArithmeticType(combine(ctx.a, ctx.b));
        }
    }

    public static Type resolveArithmeticTerm(Iterable<ExpressionComposedContext> expressionComposed) {
        return BuiltInTypes.Number;
    }

    @Override
    public Type visitArithmeticTerm(ArithmeticTermContext ctx) {
        if (ctx.op == null || ctx.op.size() == 0) {
            return this.visitExpressionComposed(ctx.a);
        } else {
            return resolveArithmeticTerm(combine(ctx.a, ctx.b));
        }
    }

    @Override
    public Type visitExpressionComposed(ExpressionComposedContext ctx) {
        if (ctx.methodCallChain() != null) {
            return this.visitMethodCallChain(ctx.methodCallChain());
        } else if (ctx.expressionAtom() != null) {
            return this.visitExpressionAtom(ctx.expressionAtom());
        } else {
            throw new BadSource(ctx);
        }
    }

    @Override
    public Type visitMethodCallChain(MethodCallChainContext ctx) {

        // The 'typ' of a series of method calls it the type of the last one
        // TODO: Do we really need to have a "chain" as an grammar structure?
        // Feels like that should fall naturally out of expressions and method calls.

        if (ctx.a != null) {
            return this.scope.getFunctionSignature(ctx.a.getText()).get().getReturnType();
        } else if (ctx.c != null) {
            return this.scope.getFunctionSignature(ctx.c.getText()).get().getReturnType();
        } else {
            throw new BadSource(ctx);
        }
    }

    @Override
    public Type visitMethodCall(MethodCallContext ctx) {
        return this.scope.getFunctionSignature(ctx.func.getText()).get().getReturnType();
    }

    @Override
    public Type visitExpressionAtom(ExpressionAtomContext ctx) {

        if (ctx.NUMBER() != null) {
            return BuiltInTypes.Number;
        }
        else if (ctx.TRUE() != null) {
            return BuiltInTypes.Boolean;
        }
        else if (ctx.FALSE() != null) {
            return BuiltInTypes.Boolean;
        }
        else if (ctx.NIL() != null) {
            return BuiltInTypes.Nil;
        }
        else if (ctx.ID() != null) {
            // TODO: Do we assume this is always a variable?
            return this.scope.getVariableType(ctx.ID().getText()).get();
        }
        else if (ctx.STRING() != null) {
            return BuiltInTypes.String;
        }
        else if (ctx.SYMBOL() != null) {
            return BuiltInTypes.Symbol;
        }
        else if (ctx.clojureVector() != null) {
            return BuiltInTypes.Vector;
        }
        else if (ctx.clojureMap() != null) {
            return BuiltInTypes.Map;
        }
        else if (ctx.functionCall() != null) {
            return this.scope.getFunctionSignature(ctx.functionCall().ID().getText()).get().getReturnType();
        }
        else if (ctx.clojureGet() != null) {
            throw new NotImplementedException();
            //return this.visitClojureGet(ctx.clojureGet());
        }
        else if (ctx.forLoop() != null) {
            throw new NotImplementedException();
        }
        else if (ctx.conditional() != null) {
            return this.visitConditional(ctx.conditional());
        }
        else if (ctx.lambda() != null) {
            throw new NotImplementedException();

            //return this.visitLambda(ctx.lambda());
        }
        else if (ctx.memberGetChain() != null) {
            throw new NotImplementedException();

            //return this.visitMemberGetChain(ctx.memberGetChain());
        }
        else if (ctx.recordConstructor() != null) {
            return new Type() {
                @Override
                public String getName() {
                    return "RECORD";
                }
            };

//            return this.visitRecordConstructor(ctx.recordConstructor());
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
    public Type visitMemberGetChain(MemberGetChainContext ctx) {
        return super.visitMemberGetChain(ctx);
    }

    @Override
    public Type visitMemberGet(MemberGetContext ctx) {
        return super.visitMemberGet(ctx);
    }

    @Override
    public Type visitRecordConstructor(RecordConstructorContext ctx) {
        return super.visitRecordConstructor(ctx);
    }

    @Override
    public Type visitExpressionList(ExpressionListContext ctx) {
        return super.visitExpressionList(ctx);
    }

    @Override
    public Type visitTypeAnnotation(TypeAnnotationContext ctx) {
        return super.visitTypeAnnotation(ctx);
    }

    @Override
    public Type visitFuncTypeAnnotation(FuncTypeAnnotationContext ctx) {
        return super.visitFuncTypeAnnotation(ctx);
    }

    @Override
    public Type visitLambda(LambdaContext ctx) {
        return super.visitLambda(ctx);
    }

    @Override
    public Type visitFunctionDef(FunctionDefContext ctx) {
        return super.visitFunctionDef(ctx);
    }

    @Override
    public Type visitMethodDef(MethodDefContext ctx) {
        return super.visitMethodDef(ctx);
    }

    @Override
    public Type visitFunctionDefParams(FunctionDefParamsContext ctx) {
        return super.visitFunctionDefParams(ctx);
    }

    @Override
    public Type visitFunctionCall(FunctionCallContext ctx) {
        return super.visitFunctionCall(ctx);
    }

    @Override
    public Type visitRecordDef(RecordDefContext ctx) {
        return super.visitRecordDef(ctx);
    }

    @Override
    public Type visitImplementationDef(ImplementationDefContext ctx) {
        return super.visitImplementationDef(ctx);
    }

    @Override
    public Type visitMethodParams(MethodParamsContext ctx) {
        return super.visitMethodParams(ctx);
    }

    @Override
    public Type visitForLoop(ForLoopContext ctx) {
        return super.visitForLoop(ctx);
    }


    @Override
    public Type visitBlock(BlockContext ctx) {
        if (ctx.exception != null) {
            return this.visitExpression(ctx.expression());
        } else if (ctx.statementTerm != null) {
            return this.visitStatementTerm(last(ctx.statementTerm()).get());
        } else if (ctx.varScope() != null) {
            return this.visitVarScope(last(ctx.varScope()).get());
        } else {
            throw new BadSource(ctx);
        }
    }


    @Override
    public Type visitStatementTerm(StatementTermContext ctx) {
        if (ctx.statement() != null) {
            return this.visitStatement(ctx.statement());
            //throw new NotExpressionError(ctx);
        } else if (ctx.functionDef() != null) {
            throw new NotExpressionError(ctx);
        } else if (ctx.recordDef() != null) {
            throw new NotExpressionError(ctx);
        } else if (ctx.block() != null) {
            return this.visitBlock(ctx.block());
        } else if (ctx.varScope() != null) {
            return this.visitVarScope(ctx.varScope());
        } else {
            throw new BadSource(ctx);
        }
    }


    @Override
    public Type visitVarScope(VarScopeContext ctx) {
        return this.visitStatementTerm(last(ctx.statementTerm()).get());
    }

    @Override
    public Type visitConditional(ConditionalContext ctx) {

        Type trueType = this.visitBlock(ctx.iftrue);

        for (BlockContext block: combine(ctx.elifBlock, ctx.elseBlock)) {
            Type blockType = this.visitBlock(block);

            if (blockType != trueType) {
                throw new BadSource(ctx);
            }
        }

        return trueType;
    }

    @Override
    public Type visitClojureVector(ClojureVectorContext ctx) {
        return super.visitClojureVector(ctx);
    }

    @Override
    public Type visitClojureMap(ClojureMapContext ctx) {
        return super.visitClojureMap(ctx);
    }

    @Override
    public Type visitClojureGet(ClojureGetContext ctx) {
        return super.visitClojureGet(ctx);
    }
}
