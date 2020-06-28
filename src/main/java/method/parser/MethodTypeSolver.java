package method.parser;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MethodTypeSolver {
    public static CombinedTypeSolver cbSolver;
    public List<String> testDirsPaths = new ArrayList<String>();
    public MethodTypeSolver(File srcDir) {
        // the solver uses ReflectionTypeSolver and Java file parser solver
        this.cbSolver = new CombinedTypeSolver(new JavaParserTypeSolver(srcDir), new ReflectionTypeSolver());
    }

    // recursively add all the src dirs path to the solver src path
    public void addSolverSrc(ProjectRoot p) {

        for(SourceRoot s: p.getSourceRoots()) {
            if(s.toString().contains("src/") ) {
                this.cbSolver.add(new JavaParserTypeSolver(new File(s.getRoot().toString())));
            }

            if (s.toString().contains("src/test")) {
                    this.testDirsPaths.add(s.getRoot().toString());
            }
        }
    }

    public List<String> getTestDirsPaths() {
        return this.testDirsPaths;
    }

    public TypeSolver getSolver() {
        return this.cbSolver;
    }
}
