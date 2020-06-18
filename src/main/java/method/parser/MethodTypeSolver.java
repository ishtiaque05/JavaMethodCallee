package method.parser;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;

public class MethodTypeSolver {
    public static CombinedTypeSolver cbSolver;
    public MethodTypeSolver(File srcDir) {
        // the solver uses ReflectionTypeSolver and Java file parser solver
        this.cbSolver = new CombinedTypeSolver(new JavaParserTypeSolver(srcDir), new ReflectionTypeSolver());
    }

    // recursively add all the src dirs path to the solver src path
    public void addSolverSrc(File[] files) {
        for(File f: files) {
            if(f.isDirectory() && !f.isFile() && !f.getAbsolutePath().contains("src/test")) {
                this.cbSolver.add(new JavaParserTypeSolver(new File(f.getAbsolutePath())));
                addSolverSrc(f.listFiles());
            }
        }
    }

    public TypeSolver getSolver() {
        return this.cbSolver;
    }
}
