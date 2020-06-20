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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Execute {
    public static int errors = 0, unsolved = 0, solved = 0, assertionUnsolved = 0 ;
    public static int testMethodsCount = 0, calledMethodsCount = 0;
    public static List<String> errsMsg = new ArrayList<String>();;
    public static List<TestMethodInfo> tmethods = new ArrayList<TestMethodInfo>();
    final static Logger logger = Logger.getLogger(Execute.class);
    public static void listMethodCalls(File projectDir) {
        new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(MethodDeclaration aMethod, Object arg) {
                        TestMethodInfo tmethod = JSONFormatterHelper.getInfoModel(aMethod);
                        List<MethodCallExpr> callExprList = aMethod.findAll(MethodCallExpr.class);
                        tmethod.calledMethods = new ArrayList<CalledMethodInfo>();
                        testMethodsCount++;
                        System.out.println("Current method: " + tmethod.methodName +"; Test Method Count " + testMethodsCount);
                        for (MethodCallExpr callExpr : callExprList) {
                            calledMethodsCount++;
                            System.out.println("Called method Count " + calledMethodsCount);
                            try {
                                if(callExpr.getNameAsString().contains("assert")) {
                                    throw  new UnsolvedSymbolException("Assert statement: "+callExpr.getNameAsString().toString() + " cannot resolve");
                                }
                                ResolvedMethodDeclaration resolvedMethod = callExpr.resolve();
                                CalledMethodInfo cMethod = JSONFormatterHelper.getCalledMethodModel(callExpr, resolvedMethod);
                                tmethod.calledMethods.add(cMethod);
                                Execute.solved++;
                            } catch (UnsolvedSymbolException usym) {
                                if (usym.getName().contains("Assert")) {
                                    Execute.assertionUnsolved++;
                                } else {
                                    Execute.unsolved++;
                                }
                                logger.error("Unsolved Exception" + usym);
                            } catch (Exception e) {
                                logger.error(e);
                            }
                        }
                        tmethods.add(tmethod);
                        super.visit(aMethod, arg);
                    }
                }.visit(StaticJavaParser.parse(file), null);
            } catch (Exception e) {
                logger.error(e);
            }
        }).explore(projectDir);
    }

    public static void main(String[] args) {
        // TODO: take this as parameter from args
//        File srcDir = new File(Settings.REPOS_PATH+args[0]);
        File srcDir = new File("/home/ishtiaque/Desktop/projects/JavaMethodCallee/testExamples/java-junit-sample");
        String repoName = "sample-junit";

        // Intialize the solver by adding all the source path
        MethodTypeSolver mts = new MethodTypeSolver(srcDir);
        mts.addSolverSrc(srcDir.listFiles());
        TypeSolver myTypeSolver = mts.getSolver();

        // Configure the JavaParser to use the solver for parsing
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(myTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        Execute.startProcessing(mts);

        JsonWriter.writeToJSON(Settings.OUTPATH+repoName+".json", tmethods);

        logger.info(repoName+ " JavaSolverStats Solved: "+Execute.solved+ " UnsolvedAssertions:"+ Execute.assertionUnsolved + " UnsolvedWithoutJunit:" +Execute.unsolved + " Errors: "+ Execute.errors);
    }

    public static void startProcessing(MethodTypeSolver mts) {
        // Getting all the test directories path
        List<String> testPathsDirs = mts.getTestDirsPaths();
        for (String file : testPathsDirs) {
            System.out.println("Start Processing test folder " + file);
            Execute.listMethodCalls(new File(file));
            System.out.println("Done Processing Test folder " + file);
        }
    }
}
