package method.parser;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.google.gson.internal.LinkedTreeMap;
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
import java.util.Map;
import java.util.TreeMap;

public class FileParser {
    public static int errors = 0, unsolved = 0, solved = 0, assertionUnsolved = 0, StackOverFlowCount =0;
    public static int testMethodsCount = 0, calledMethodsCount = 0;
    public static List<String> errsMsg = new ArrayList<String>();;
    public static List<TestMethodInfo> tmethods = new ArrayList<TestMethodInfo>();
    public static List<String> proccessedCommits = new ArrayList<String>();
    final static Logger logger = Logger.getLogger(FileParser.class);


    public static void readAllTestGroupByCommit(String jsonPath) throws FileNotFoundException {
        String repoPath = Settings.REPOS_PATH + Settings.REPO;
        LinkedTreeMap<String, Object> allTest = JsonHelper.readJSONObject(jsonPath);
        Path processedCommitFile = Paths.get(Settings.OUTPATH + "processed-" + Settings.REPO + ".json" );
        if(Files.exists(processedCommitFile)) {
            proccessedCommits = JsonHelper.readJSON(Settings.OUTPATH + "processed-" + Settings.REPO + ".json");
        }
        for(Map.Entry<String, Object> entry : allTest.entrySet()) {
            String commit = entry.getKey();
            ArrayList testMethods = (ArrayList) entry.getValue();
            GitHelper.checkoutCMD(commit, repoPath);
            FileParser.initJavaParser(Settings.REPO);
            List<String> proccessedFiles = new ArrayList<String>();
            if(proccessedCommits.contains(commit)) {
                System.out.println("######################### Skipping commit: " + commit);
                continue;
            } else {
                for(int i = 0; i < testMethods.size() ; i++) {
                    IdentificationModel im = new IdentificationModel();
                    im.name = (String)((LinkedTreeMap) testMethods.get(i)).get("name");
                    im.path = (String)((LinkedTreeMap) testMethods.get(i)).get("path");
                    im.startline = (Double) ((LinkedTreeMap) testMethods.get(i)).get("startline");
                    im.fileId = (String)((LinkedTreeMap) testMethods.get(i)).get("fileId");
                    if (proccessedFiles.contains(im.path)) {
                        continue;
                    } else {
                        FileParser.startProcessing(repoPath + "/" + im.path, commit, im);
                        proccessedFiles.add(im.path);
                    }
                }
            }
            proccessedFiles = null;
        }

    }
    public static void initJavaParser(String repoName) {
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
    }
    public static void startProcessing(String fullpath, String commit, IdentificationModel im) {
        FileParser.calculateFanOutForFile(fullpath);
        String outputDir = Settings.OUTPATH + Settings.REPO;

        try {
            Files.createDirectories(Paths.get(outputDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonHelper.writeToJSON(outputDir + "/" + commit +".json", tmethods);
        proccessedCommits.add(commit);
        JsonHelper.writeToFile(Settings.OUTPATH + "processed-" + Settings.REPO + ".json", proccessedCommits);

        logger.info(Settings.REPO + ": JavaSolverStats Solved: "
                +FileParser.solved+ " UnsolvedAssertions:"+
                FileParser.assertionUnsolved +
                " UnsolvedWithoutJunit:" + FileParser.unsolved +
                " Errors: "+ FileParser.errors + " StackOverFlowError: " + FileParser.StackOverFlowCount);

        // For GC to pick up
        tmethods = null;
        tmethods = new ArrayList<TestMethodInfo>();
    }

    public static void fetchFanout(MethodTypeSolver mts, String commit, IdentificationModel im) {

    }
    public static void calculateFanOutForFile(String file) {
        System.out.println("Building the Fan network now... ");
            try {
                CompilationUnit cu = StaticJavaParser.parse(new File(file));
                if (cu == null) {
                   throw new Exception("Compilation unit is null");
                }
                VoidVisitor<Object> methodVisitor = new MethodLister();
                methodVisitor.visit(cu, file);
            }catch(Exception e){
              System.out.println(e);
            }
    }
    public static class MethodLister extends VoidVisitorAdapter<Object> {
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
                    FileParser.solved++;
                } catch (UnsolvedSymbolException usym) {
                    if (usym.getName().contains("Assert") ) {
                        FileParser.assertionUnsolved++;
                    } else {
                        tmethod.notFoundMethods.add(usym.getMessage());
                        tmethod.shouldSkip = true;
                        FileParser.unsolved++;
                    }
//                                logger.error("Unsolved Exception" + usym);
                } catch(StackOverflowError e) {
                    FileParser.StackOverFlowCount++;
                    System.out.println("Caught stack overflow error!");
                }
                catch (Exception e) {
                    FileParser.errors++;
//                                logger.error(e);
                }
            }
            tmethods.add(tmethod);
            super.visit(aMethod, arg);
        }
    }
}

