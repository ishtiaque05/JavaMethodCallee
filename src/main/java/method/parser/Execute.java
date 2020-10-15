package method.parser;

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
import org.eclipse.jgit.api.errors.*;
import util.readwrite.FileOperations;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.apache.commons.cli.*;

public class Execute {
    public static int errors = 0, unsolved = 0, solved = 0, assertionUnsolved = 0, StackOverFlowCount =0;
    public static int testMethodsCount = 0, calledMethodsCount = 0;
    public static List<String> errsMsg = new ArrayList<String>();;
    public static List<TestMethodInfo> tmethods = new ArrayList<TestMethodInfo>();
    final static Logger logger = Logger.getLogger(Execute.class);
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
                        testMethodsCount++;
                        System.out.println("Current method: " + tmethod.name +"; Test Method Count " + testMethodsCount);
                        for (MethodCallExpr callExpr : callExprList) {
                            calledMethodsCount++;
                            System.out.println("Called method Count " + calledMethodsCount);
                            try {
                                if(callExpr.getNameAsString().contains("assert")) {
                                    throw  new UnsolvedSymbolException(callExpr.getNameAsString().toString());
                                }
                                ResolvedMethodDeclaration resolvedMethod = callExpr.resolve();
                                CalledMethodInfo cMethod = JSONFormatterHelper.getCalledMethodModel(callExpr, resolvedMethod);
                                tmethod.calledMethods.add(cMethod);
                                Execute.solved++;
                            } catch (UnsolvedSymbolException usym) {
                                tmethod.notFoundMethods.add(usym.getName());
                                if (usym.getName().contains("Assert")) {
                                    Execute.assertionUnsolved++;
                                } else {
                                    Execute.unsolved++;
                                }
                                logger.error("Unsolved Exception" + usym);
                            } catch(StackOverflowError e) {
                                Execute.StackOverFlowCount++;
                                System.out.println("Caught stack overflow error!");
                            }
                            catch (Exception e) {
                                Execute.errors++;
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

    public static void main(String[] args) throws IOException {
        Execute.setArguments(args);
        File f = new File(Settings.COMMITS_LIST_PATH);
        String lines = FileOperations.loadAsString(f);
        String[] commits = lines.split("\r\n|\r|\n");
        for(String commit: commits) {
            String repoPath = Settings.REPOS_PATH + Settings.REPO;
            Execute.checkoutCMD(commit, repoPath);
            Execute.init(Settings.REPO, repoPath, commit);
        }

    }

    private static void setArguments(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("repoDir",true, "Folder path that contains all the repos");
        options.addOption("repo",true, "Name of the repository");
        options.addOption("out",true, "Filepath to save the processed file");
        options.addOption("commitList",true, "Filepath that contains the commit list?");
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
            String repoDir = line.getOptionValue("repoDir");
            String repo = line.getOptionValue("repo");
            String out = line.getOptionValue("out");
            String commitList = line.getOptionValue("commitList");

            if(repoDir!=null){
                Settings.REPOS_PATH = repoDir;
            }
            if(repo != null){
                Settings.REPO = repo;
            }
            if(commitList != null){
                Settings.COMMITS_LIST_PATH = commitList;
            }

            if(out != null){
                Settings.OUTPATH = out;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void checkoutCMD(String commit, String repoPath) {
        try {
            Git.open(new File(repoPath + "/.git"))
                    .checkout().setName(commit).call();;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidRefNameException e) {
            e.printStackTrace();
        } catch (CheckoutConflictException e) {
            e.printStackTrace();
        } catch (RefAlreadyExistsException e) {
            e.printStackTrace();
        } catch (RefNotFoundException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
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
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(myTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        Execute.startProcessing(mts);

//        Problematic file /home/siahmad/projects/def-rtholmes-ab/siahmad/repos/pmd/pmd-cs/src/test/java/net/sourceforge/pmd/cpd/CsTokenizerTest.java
//        Uncomment below line to have stackoverflow error
//        Execute.listMethodCalls(new File(Settings.REPOS_PATH+"/pmd/pmd-cs/src/test/java/net/sourceforge/pmd/cpd/"));\
        String outputDir = Settings.OUTPATH + repoName;

        try {
            Files.createDirectories(Paths.get(outputDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonWriter.writeToJSON(outputDir + "/" + commit +".json", tmethods);

        logger.info(repoName+ ": JavaSolverStats Solved: "
                +Execute.solved+ " UnsolvedAssertions:"+
                Execute.assertionUnsolved +
                " UnsolvedWithoutJunit:" +Execute.unsolved +
                " Errors: "+ Execute.errors + " StackOverFlowError: " + Execute.StackOverFlowCount);

        // For GC to pick up
        tmethods = null;
        tmethods = new ArrayList<TestMethodInfo>();
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
