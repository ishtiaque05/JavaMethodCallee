package method.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.google.common.base.Strings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Execute {
    public static int errors = 0;
    public static int solved = 0;
    public static int unsolved = 0;
    public static int jUnitUnsolved = 0;
    public static List<String> errsMsg = new ArrayList<String>();;
    public static List<TestMethodInfo> tmethods = new ArrayList<TestMethodInfo>();
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
                    @Override
                    public void visit(MethodDeclaration aMethod, Object arg) {
                        TestMethodInfo tmethod = new TestMethodInfo();
                        tmethod.methodName = aMethod.getNameAsString();
                        tmethod.methodSignature = aMethod.getSignature().asString();

//                        System.out.println(aMethod.getDeclarationAsString(true, false));
//                        System.out.println(aMethod.getNameAsString());
//                        System.out.println(aMethod.getSignature());

                        List<MethodCallExpr> callExprList = aMethod.findAll(MethodCallExpr.class);
                        for (MethodCallExpr callExpr : callExprList) {
                            ResolvedMethodDeclaration resolvedMethod = callExpr.resolve();
//                          Getting corresponding method declaration
//                          JavaParserFacade.get(new JavaParserTypeSolver("/home/ishtiaque/Desktop/projects/testCallGraph/src/main/java")).solve(callExpr);
                            CalledMethodInfo cMethod = new CalledMethodInfo();
                            tmethod.calledMethods = new ArrayList<CalledMethodInfo>();
                            cMethod.name = callExpr.getNameAsString();
                            cMethod.className = resolvedMethod.getClassName();
                            cMethod.fullQualifiedSignature = resolvedMethod.getQualifiedSignature();
                            cMethod.packageName = resolvedMethod.getPackageName();
                            cMethod.signature = resolvedMethod.getSignature();
//                            System.out.println(callExpr.getName());
//                            System.out.println(resolvedMethod.getClassName());
//                            System.out.println(resolvedMethod.getPackageName());
//                            resolvedMethod.getQualifiedSignature();
//                            resolvedMethod.getSignature();
                            cMethod.startline = resolvedMethod.toAst().get().getRange().get().begin.line;
                            cMethod.path = resolvedMethod.toAst().get().getParentNode().get().findCompilationUnit().get().getStorage().get().getPath().toString();
//                            System.out.println(resolvedMethod.getQualifiedName());
//                            System.out.println(resolvedMethod.toAst().get().getRange().get().begin.line);
                            tmethod.calledMethods.add(cMethod);
                            tmethods.add(tmethod);
                            Execute.solved++;
                        }
//                        System.out.println("}\n");
                        super.visit(aMethod, arg);

                    }
                }.visit(StaticJavaParser.parse(file), null);
                System.out.println(); // empty line
            } catch (UnsolvedSymbolException usym) {
                if(usym.getName().contains("junit")) {
                    Execute.jUnitUnsolved++;
                } else {
                    Execute.unsolved++;
                }
                System.out.println("Unsolved Exception:"+ usym);
            }
            catch (Exception e) {
                Execute.errors++;
                System.out.println(e.getMessage());
                Execute.errsMsg.add(e.getMessage());
            }
        }).explore(projectDir);
    }

    public static void main(String[] args) {

        // TODO: take this as parameter from args
        File testDir = new File("/home/ishtiaque/Desktop/projects/JavaMethodCallee/testExamples/testCallGraph/src/test");
        File srcDir = new File("/home/ishtiaque/Desktop/projects/JavaMethodCallee/testExamples/testCallGraph/src");

        // Intialize the solver by adding all the source path
        MethodTypeSolver mts = new MethodTypeSolver(srcDir);
        mts.addSolverSrc(srcDir.listFiles());
        TypeSolver myTypeSolver = mts.getSolver();

        // Configure the JavaParser to use the solver for parsing
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(myTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        listMethodCalls(testDir);
        JsonWriter.writeToJSON("sample.json", tmethods);
        System.out.println("Solved: "+Execute.solved+ " Unsolved Assertions:"+ Execute.jUnitUnsolved + " Unsolved without Junit:" +Execute.unsolved + " Errors: "+ Execute.errors);
        System.out.println(Execute.errsMsg);
    }

}
