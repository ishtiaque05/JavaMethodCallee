package method.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
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
                        System.out.println(aMethod.getDeclarationAsString(true, false));
                        System.out.println(aMethod.getNameAsString());
                        List<MethodCallExpr> callExprList = aMethod.findAll(MethodCallExpr.class);
                        for (MethodCallExpr callExpr : callExprList) {
//                          Getting corresponding method declaration
//                          JavaParserFacade.get(new JavaParserTypeSolver("/home/ishtiaque/Desktop/projects/testCallGraph/src/main/java")).solve(callExpr);
                            System.out.println(callExpr.getName());
                            callExpr.resolve().toAst();
                            System.out.println(callExpr.resolve().getQualifiedName());
                            System.out.println(callExpr.resolve().toAst().get().getRange().get().begin.line);
                            System.out.println(callExpr.resolve().toAst().get().getParentNode().get().findCompilationUnit().get().getStorage().get().getPath());
                            Execute.solved++;
                        }
                        System.out.println("}\n");
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
        File testDir = new File("/home/ishtiaque/Desktop/projects/Research/commons-lang/src/test");
        File srcDir = new File("/home/ishtiaque/Desktop/projects/Research/commons-lang/src");

        // Intialize the solver by adding all the source path
        MethodTypeSolver mts = new MethodTypeSolver(srcDir);
        mts.addSolverSrc(srcDir.listFiles());
        TypeSolver myTypeSolver = mts.getSolver();

        // Configure the JavaParser to use the solver for parsing
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(myTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        listMethodCalls(testDir);
        System.out.println("Solved: "+Execute.solved+ " Unsolved Assertions:"+ Execute.jUnitUnsolved + " Unsolved without Junit:" +Execute.unsolved + " Errors: "+ Execute.errors);
        System.out.println(Execute.errsMsg);
    }

}
