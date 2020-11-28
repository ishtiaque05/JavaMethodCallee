package method.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;

public class JSONFormatterHelper {
    public static TestMethodInfo getInfoModel(MethodDeclaration aMethod) {
        String repo_path = Settings.REPOS_PATH + Settings.REPO + "/";
        TestMethodInfo tmethod = new TestMethodInfo();
        tmethod.name = aMethod.getNameAsString();
        tmethod.methodSignature = aMethod.getSignature().asString();
        tmethod.startline = aMethod.getName().getBegin().get().line;
        tmethod.endline = aMethod.getEnd().get().line;
        tmethod.path = aMethod.findCompilationUnit().get().getStorage().get().getPath().toString().replace(repo_path, "");
        tmethod.actualCodeAsString = aMethod.toString();
        tmethod.formattedMethodSignature = aMethod.getDeclarationAsString().trim().replace(" ","");
        tmethod.methodSignatureAsString = aMethod.getDeclarationAsString();
        return tmethod;
    }

    public static CalledMethodInfo getCalledMethodModel(MethodCallExpr callExpr, ResolvedMethodDeclaration resolvedMethod) {
        String fanoutMethodDeclaration = ((JavaParserMethodDeclaration) resolvedMethod).getWrappedNode().getDeclarationAsString();
        String repo_path = Settings.REPOS_PATH + Settings.REPO + "/";
        CalledMethodInfo cMethod = new CalledMethodInfo();
        cMethod.name = callExpr.getNameAsString();
        cMethod.className = resolvedMethod.getClassName();
        cMethod.fullQualifiedSignature = resolvedMethod.getQualifiedSignature();
        cMethod.actualCodeAsString = ((JavaParserMethodDeclaration) resolvedMethod).getWrappedNode().toString();
        cMethod.formattedMethodSignature = fanoutMethodDeclaration.trim().replace(" ", "");
        cMethod.methodSignatureAsString = fanoutMethodDeclaration;
        cMethod.packageName = resolvedMethod.getPackageName();
        cMethod.signature = resolvedMethod.getSignature();
        cMethod.startline = resolvedMethod.toAst().get().getName().getBegin().get().line;
        cMethod.endline = resolvedMethod.toAst().get().getName().getEnd().get().line;
        cMethod.path = resolvedMethod.toAst().get().findCompilationUnit().get().getStorage().get().getPath().toString().replace(repo_path, "");
        return cMethod;
    }
}
