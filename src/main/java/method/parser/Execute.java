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
import jdk.nashorn.internal.ir.debug.JSONWriter;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.*;
import util.git.GitHelper;
import util.readwrite.FileOperations;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.apache.commons.cli.*;

public class Execute {
    public static void main(String[] args) throws Exception {
        Execute.setArguments(args);
        if(Settings.filemode) {
           FileParser.readAllTestGroupByCommit(Settings.COMMITS_LIST_PATH);
        } else {
            AllTestMethodsGenerator.execute();
        }

    }

    private static void setArguments(String[] args) {
//        Settings.REPOS_PATH = "/home/ishtiaque/Desktop/projects/Research/";
//        Settings.REPO = "elasticsearch";
//        Settings.OUTPATH = "/home/ishtiaque/Desktop/projects/GitlabData/callGraphData/";
//        Settings.COMMITS_LIST_PATH = "/home/ishtiaque/Desktop/projects/ProcessCallGraph/data/EvolData/all-test-elasticsearch.json";
        Settings.filemode = true;
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

}
