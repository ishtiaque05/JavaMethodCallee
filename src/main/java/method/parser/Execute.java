package method.parser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.google.common.base.Strings;
import method.parser.DirExplorer;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Execute {
    public static void listMethodCalls(File projectDir) {
        new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            System.out.println(file.getAbsolutePath());
            System.out.println(Strings.repeat("=", path.length()));
            try {
                new VoidVisitorAdapter<Object>() {
//                    @Override
//                    public void visit(MethodCallExpr n, Object arg) {
//                        super.visit(n, arg);
//                        System.out.println(" [L " + n.getBegin().get().line + "] " + n);
//                    }

                    @Override
                    public void visit(MethodDeclaration aMethod, Object arg) {
                        System.out.println(aMethod.getDeclarationAsString(true, false));
                        System.out.println();
                        System.out.println("{");
                        String declaredMethod = aMethod.getNameAsString();
                        List<MethodCallExpr> callExprList = aMethod.findAll(MethodCallExpr.class);
                        for (MethodCallExpr callExpr : callExprList) {
                            String calledMethodName = callExpr.getName().getIdentifier();
                            System.out.println(calledMethodName);
//                            visitMethodCall(callExpr, arg, declaredMethod);
//                            CombinedTypeSolver solver = typeSolver(srcFolders);
//                            JavaParserFacade j = JavaParserFacade.get(solver);
//                            SymbolReference<ResolvedMethodDeclaration> methodRef = j.solve(callExpr);
//                            String methodSignature = methodRef.getCorrespondingDeclaration().getQualifiedSignature();
//                            if(methodSignature.contains("javax.security.auth") || methodSignature.contains("javax") || methodSignature.contains("security") || methodSignature.contains("auth")) {
//                                System.out.println(methodSignature);
//                            }
                        }
                        System.out.println("}\n");
                        super.visit(aMethod, arg);

                    }

                    public void visitMethodCall(MethodCallExpr n, Object arg, String declaredMethod) {
                        super.visit(n, arg);
                    }

                    @Override
                    public void visit(MethodCallExpr n, Object arg) {
                        super.visit(n, arg);
                    }
                }.visit(StaticJavaParser.parse(file), null);
                System.out.println(); // empty line
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).explore(projectDir);
    }

    public static void main(String[] args) {
        File projectDir = new File("/home/ishtiaque/Desktop/projects/examples");
        listMethodCalls(projectDir);
    }
}
