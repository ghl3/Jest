package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
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

import jest.grammar.JestParser.TypeAnnotationContext;
import jest.grammar.JestParser.VarScopeContext;


/**
 * Takes an expression and returns the type of the expression
 */
public class ExpressionTypeVisitor extends JestBaseVisitor<Type> {


    public static <T> Iterable<T> combine(T left, Iterable<T> right) {
        List<T> lst = Lists.newArrayList();
        lst.add(left);
        for (T t: right) {
            lst.add(t);
        }
        return ImmutableList.copyOf(lst);
    }

    public static <T> Iterable<T> combine(Iterable<T> left, Iterable<T> right) {
        List<T> lst = Lists.newArrayList();
        for (T t: left) {
            lst.add(t);
        }
        for (T t: right) {
            lst.add(t);
        }
        return ImmutableList.copyOf(lst);
    }

    public static <T> Iterable<T> combine(Iterable<T> left, T right) {
        List<T> lst = Lists.newArrayList();
        for (T t: left) {
            lst.add(t);
        }
        lst.add(right);
        return ImmutableList.copyOf(lst);
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
        if (ctx.op == null) {
            return this.visitArithmeticTerm(ctx.a);
        } else {
            return resolveArithmeticType(combine(ctx.a, ctx.b));
        }
    }

    @Override
    public Type visitArithmeticTerm(ArithmeticTermContext ctx) {
        return super.visitArithmeticTerm(ctx);
    }

    @Override
    public Type visitExpressionComposed(ExpressionComposedContext ctx) {
        return super.visitExpressionComposed(ctx);
    }

    @Override
    public Type visitMethodCallChain(MethodCallChainContext ctx) {
        return super.visitMethodCallChain(ctx);
    }

    @Override
    public Type visitMethodCall(MethodCallContext ctx) {
        return super.visitMethodCall(ctx);
    }

    @Override
    public Type visitExpressionAtom(ExpressionAtomContext ctx) {
        return super.visitExpressionAtom(ctx);
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
        return super.visitBlock(ctx);
    }

    @Override
    public Type visitVarScope(VarScopeContext ctx) {
        return super.visitVarScope(ctx);
    }

    @Override
    public Type visitConditional(ConditionalContext ctx) {
        return super.visitConditional(ctx);
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
