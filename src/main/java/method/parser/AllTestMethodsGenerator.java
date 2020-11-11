package method.parser;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import org.apache.log4j.Logger;
import util.git.GitHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AllTestMethodsGenerator {
    public static int errors = 0, unsolved = 0, solved = 0, assertionUnsolved = 0, StackOverFlowCount =0;
    public static int testMethodsCount = 0, calledMethodsCount = 0;
    public static List<String> errsMsg = new ArrayList<String>();;
    public static List<TestMethodInfo> tmethods = new ArrayList<TestMethodInfo>();
    public static List<String> proccessedCommits = new ArrayList<String>();
    final static Logger logger = Logger.getLogger(AllTestMethodsGenerator.class);
    public static void listMethodCalls(File projectDir) {
        new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            System.out.println("Processing file:" + file.getAbsolutePath());
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(MethodDeclaration aMethod, Object arg) {
                        TestMethodInfo tmethod = JSONFormatterHelper.getInfoModel(aMethod);
                        List<MethodCallExpr> callExprList = aMethod.findAll(MethodCallExpr.class);
                        tmethod.calledMethods = new ArrayList<CalledMethodInfo>();
                        tmethod.notFoundMethods = new ArrayList<String>();
                        tmethod.junitMethods = new ArrayList<String>();
                        testMethodsCount++;
                        System.out.println("############## Current method: " + tmethod.name);
                        for (MethodCallExpr callExpr : callExprList) {
                            calledMethodsCount++;
//                            System.out.println("Called method Count " + calledMethodsCount);
                            try {
                                if(AssertionHelper.JUNIT_ASSERTION_API.contains(callExpr.getNameAsString())){
                                    tmethod.junitMethods.add(callExpr.getNameAsString());
                                }
                                ResolvedMethodDeclaration resolvedMethod = callExpr.resolve();
                                CalledMethodInfo cMethod = JSONFormatterHelper.getCalledMethodModel(callExpr, resolvedMethod);
                                tmethod.calledMethods.add(cMethod);
                                AllTestMethodsGenerator.solved++;
                            } catch (UnsolvedSymbolException usym) {
                                tmethod.notFoundMethods.add(usym.getName());
                                tmethod.shouldSkip = true;
                                if (usym.getName().contains("Assert")) {
                                    AllTestMethodsGenerator.assertionUnsolved++;
                                } else {
                                    tmethod.shouldSkip = true;
                                    AllTestMethodsGenerator.unsolved++;
                                }
//                                logger.error("Unsolved Exception" + usym);
                            } catch(StackOverflowError e) {
                                AllTestMethodsGenerator.StackOverFlowCount++;
                                System.out.println("Caught stack overflow error!");
                            }
                            catch (Exception e) {
                                AllTestMethodsGenerator.errors++;
//                                logger.error(e);
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
    public static void init(String repoName, String repoPath, String commit) {
        File srcDir = new File(Settings.REPOS_PATH + repoName);

        // Get all project root
        ProjectRoot p = new ParserCollectionStrategy().collect(srcDir.toPath());

        // Intialize the solver by adding all the source path
        MethodTypeSolver mts = new MethodTypeSolver(srcDir);
        mts.addSolverSrc(p);
        TypeSolver myTypeSolver = mts.getSolver();

        // Configure the JavaParser to use the solver for parsing
        ParserConfiguration parserConfiguration = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.RAW);
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(myTypeSolver);
        StaticJavaParser.setConfiguration(parserConfiguration);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        AllTestMethodsGenerator.startProcessing(mts);
        String outputDir = Settings.OUTPATH + repoName;

        try {
            Files.createDirectories(Paths.get(outputDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonHelper.writeToJSON(outputDir + "/" + commit +".json", tmethods);
        proccessedCommits.add(commit);
        JsonHelper.writeToFile(Settings.OUTPATH + "processed-" + Settings.REPO + ".json", proccessedCommits);

        logger.info(repoName+ ": JavaSolverStats Solved: "
                +AllTestMethodsGenerator.solved+ " UnsolvedAssertions:"+
                AllTestMethodsGenerator.assertionUnsolved +
                " UnsolvedWithoutJunit:" + AllTestMethodsGenerator.unsolved +
                " Errors: "+ AllTestMethodsGenerator.errors + " StackOverFlowError: " + AllTestMethodsGenerator.StackOverFlowCount);

        // For GC to pick up
        tmethods = null;
        tmethods = new ArrayList<TestMethodInfo>();
    }

    public static void startProcessing(MethodTypeSolver mts) {
        // Getting all the test directories path
        List<String> testPathsDirs = mts.getTestDirsPaths();
        for (String file : testPathsDirs) {
            System.out.println("Start Processing test folder " + file);
            AllTestMethodsGenerator.listMethodCalls(new File(file));
            System.out.println("Done Processing Test folder " + file);
        }
    }

    public static void execute() throws FileNotFoundException {
        List<String> commits = JsonHelper.readJSON(Settings.COMMITS_LIST_PATH);
        boolean shouldSkip = false;
        Path processedCommitFile = Paths.get(Settings.OUTPATH + "processed-" + Settings.REPO + ".json" );
        if(Files.exists(processedCommitFile)) {
            shouldSkip = true;
            proccessedCommits = JsonHelper.readJSON(Settings.OUTPATH + "processed-" + Settings.REPO + ".json");
        }
        for(String commit: commits) {
            if(shouldSkip && proccessedCommits.contains(commit)) {
                System.out.println("####################### SKIPPING----"+ commit);
                continue;
            } else {
                System.out.println("\n \n  **************** Running commit ******************* :" + commit);
                String repoPath = Settings.REPOS_PATH + Settings.REPO;
                GitHelper.checkoutCMD(commit, repoPath);

                AllTestMethodsGenerator.init(Settings.REPO, repoPath, commit);
                System.out.println("################# ERROR STATS #######################");
                System.out.println("JavaSolverStats Solved: " +AllTestMethodsGenerator.solved+
                        " UnsolvedAssertions: "+ AllTestMethodsGenerator.assertionUnsolved +
                        " UnsolvedWithoutJunit: " +AllTestMethodsGenerator.unsolved +
                        " Errors: "+ AllTestMethodsGenerator.errors +
                        " StackOverFlowError: " + AllTestMethodsGenerator.StackOverFlowCount);
                AllTestMethodsGenerator.solved = 0;
                AllTestMethodsGenerator.assertionUnsolved = 0;
                AllTestMethodsGenerator.unsolved = 0;
                AllTestMethodsGenerator.errors = 0;
                AllTestMethodsGenerator.StackOverFlowCount = 0;
            }

        }
        System.out.println("Complete processing------------" + Settings.REPO);
    }
}
