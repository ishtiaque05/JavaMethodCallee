package method.parser;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.common.base.Strings;
import method.parser.DirExplorer;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Execute {
    public static void listMethodCalls(File projectDir) {
        new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            try {
                System.out.println(file.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
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
//                          Getting corresponding method declaration
//                          JavaParserFacade.get(new JavaParserTypeSolver("/home/ishtiaque/Desktop/projects/testCallGraph/src/main/java")).solve(callExpr);
                            callExpr.resolve().toAst();
                            System.out.println(callExpr.resolve().getQualifiedName());
                            System.out.println(callExpr.resolve().toAst().get().getRange().get().begin.line);
                            System.out.println(callExpr.resolve().toAst().get().getParentNode().get().findCompilationUnit().get().getStorage().get().getPath());
                        }
                        System.out.println("}\n");
                        super.visit(aMethod, arg);

                    }
                }.visit(StaticJavaParser.parse(file), null);
                System.out.println(); // empty line
            } catch (UnsolvedSymbolException usym) {
                System.out.println(usym);
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }).explore(projectDir);
    }

    public static void main(String[] args) {
        File projectDir = new File("/home/ishtiaque/Desktop/projects/JavaMethodCallee/testExamples/testCallGraph/src/test");
        File srcDir = new File("/home/ishtiaque/Desktop/projects/JavaMethodCallee/testExamples/testCallGraph/src");
        TypeSolver myTypeSolver;
        CombinedTypeSolver cbSolver = new CombinedTypeSolver(new JavaParserTypeSolver(srcDir), new ReflectionTypeSolver());
        if(srcDir.exists() && srcDir.isDirectory()){
            File fileArr[] = srcDir.listFiles();
            myTypeSolver = addAllSrcPath(fileArr, cbSolver);
//            System.out.println(myTypeSolver);
        } else {
            myTypeSolver = cbSolver;
        }
//            TypeSolver myTypeSolver = new CombinedTypeSolver(
//                new ReflectionTypeSolver(),
//                new JavaParserTypeSolver(new File("/home/ishtiaque/Desktop/projects/JavaMethodCallee/testExamples/testCallGraph/src/main/java/demo/nested")),
//                new JavaParserTypeSolver(new File("/home/ishtiaque/Desktop/projects/JavaMethodCallee/testExamples/testCallGraph/src/main/java/demo"))
//        );
//        TypeSolver typeSolver = new ReflectionTypeSolver();
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(myTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        listMethodCalls(projectDir);
    }

    public static TypeSolver addAllSrcPath(File[] files, CombinedTypeSolver myTypeSolver) {
        for(File f: files) {
            if(f.isDirectory() && !f.isFile() && !f.getAbsolutePath().contains("src/test")) {
//                System.out.println(f.getAbsolutePath());
                myTypeSolver.add(new JavaParserTypeSolver(new File(f.getAbsolutePath())));
                addAllSrcPath(f.listFiles(), myTypeSolver);
            }
        }
        return myTypeSolver;
    }
}
