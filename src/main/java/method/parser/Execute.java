package method.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Execute {
    public static int errors = 0;
    public static int solved = 0;
    public static int unsolved = 0;
    public static int jUnitUnsolved = 0;
    public static List<String> errsMsg = new ArrayList<String>();;
    public static List<TestMethodInfo> tmethods = new ArrayList<TestMethodInfo>();
    static List<String> testDirsPaths = new ArrayList<String>();
    final static Logger logger = Logger.getLogger(Execute.class);
    public static void listMethodCalls(File projectDir) {
        new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            System.out.println(path.toString());
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(MethodDeclaration aMethod, Object arg) {
                        TestMethodInfo tmethod = new TestMethodInfo();
                        tmethod.methodName = aMethod.getNameAsString();
                        tmethod.methodSignature = aMethod.getSignature().asString();
                        tmethod.path = aMethod.getParentNode().get().findCompilationUnit().get().getStorage().get().getPath().toString();
                        List<MethodCallExpr> callExprList = aMethod.findAll(MethodCallExpr.class);
                        for (MethodCallExpr callExpr : callExprList) {
                            ResolvedMethodDeclaration resolvedMethod = callExpr.resolve();
                            CalledMethodInfo cMethod = new CalledMethodInfo();
                            tmethod.calledMethods = new ArrayList<CalledMethodInfo>();
                            cMethod.name = callExpr.getNameAsString();
                            System.out.println(callExpr.resolve().getQualifiedSignature());
                            cMethod.className = resolvedMethod.getClassName();
                            cMethod.fullQualifiedSignature = resolvedMethod.getQualifiedSignature();
                            cMethod.packageName = resolvedMethod.getPackageName();
                            cMethod.signature = resolvedMethod.getSignature();
                            cMethod.startline = resolvedMethod.toAst().get().getRange().get().begin.line;
                            cMethod.path = resolvedMethod.toAst().get().getParentNode().get().findCompilationUnit().get().getStorage().get().getPath().toString();
                            tmethod.calledMethods.add(cMethod);
                            Execute.solved++;
                        }
                        System.out.println("I AM HERE");
                        tmethods.add(tmethod);
                        super.visit(aMethod, arg);
                    }
                }.visit(StaticJavaParser.parse(file), null);
            } catch (UnsolvedSymbolException usym) {
                if(usym.getName().contains("junit")) {
                    Execute.jUnitUnsolved++;
                } else {
                    Execute.unsolved++;
                }
                logger.error("Unsolved Exception" + usym);
                System.out.println("Unsolved Exception:"+ usym);
            }
            catch (Exception e) {
                Execute.errors++;
                System.out.println(e.getMessage());
                Execute.errsMsg.add(e.getMessage());
                logger.error(e);
            }
        }).explore(projectDir);
    }

    public static void main(String[] args) {

        // TODO: take this as parameter from args

        File srcDir = new File("/home/ishtiaque/Desktop/projects/JavaMethodCallee/testExamples/testCallGraph/");
        String repoName = "abc";

        // Intialize the solver by adding all the source path
        MethodTypeSolver mts = new MethodTypeSolver(srcDir);
        mts.addSolverSrc(srcDir.listFiles());
        TypeSolver myTypeSolver = mts.getSolver();

        // Configure the JavaParser to use the solver for parsing
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(myTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        File testDir = new File("/home/ishtiaque/Desktop/projects/JavaMethodCallee/testExamples/testCallGraph/src/test");
        File repo = new File("/home/ishtiaque/Desktop/projects/JavaMethodCallee/testExamples/testCallGraph");
        Execute.findTestDirs(repo);
//        Execute.listMethodCalls(testDir);
        Execute.startProcessing();

        JsonWriter.writeToJSON(Settings.OUTPATH+repoName+".json", tmethods);

        logger.info(repoName+ " JavaSolverStats Solved: "+Execute.solved+ " UnsolvedAssertions:"+ Execute.jUnitUnsolved + " UnsolvedWithoutJunit:" +Execute.unsolved + " Errors: "+ Execute.errors);
    }

    public static void startProcessing() {
        for (String file : testDirsPaths) {
            Execute.listMethodCalls(new File(file));
        }
    }

    public static void findTestDirs(final File folder) {
        for (final File f : folder.listFiles()) {
            if (f.getAbsolutePath().endsWith("src/test") && f.isDirectory()) {
                testDirsPaths.add(f.getAbsolutePath());
            } else if (f.isDirectory()) {
                findTestDirs(f);
            } else {
                continue;
            }
        }
    }
}
