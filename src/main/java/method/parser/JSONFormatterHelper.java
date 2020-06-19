package method.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

public class JSONFormatterHelper {
    public static TestMethodInfo getInfoModel(MethodDeclaration aMethod) {
        TestMethodInfo tmethod = new TestMethodInfo();
        tmethod.methodName = aMethod.getNameAsString();
        tmethod.methodSignature = aMethod.getSignature().asString();
        tmethod.path = aMethod.getParentNode().get().findCompilationUnit().get().getStorage().get().getPath().toString();
        return tmethod;
    }

    public static CalledMethodInfo getCalledMethodModel(MethodCallExpr callExpr, ResolvedMethodDeclaration resolvedMethod) {
        CalledMethodInfo cMethod = new CalledMethodInfo();
        cMethod.name = callExpr.getNameAsString();
        cMethod.className = resolvedMethod.getClassName();
        cMethod.fullQualifiedSignature = resolvedMethod.getQualifiedSignature();
        cMethod.packageName = resolvedMethod.getPackageName();
        cMethod.signature = resolvedMethod.getSignature();
        cMethod.startline = resolvedMethod.toAst().get().getRange().get().begin.line;
        cMethod.path = resolvedMethod.toAst().get().getParentNode().get().findCompilationUnit().get().getStorage().get().getPath().toString();
        return cMethod;
    }
}
