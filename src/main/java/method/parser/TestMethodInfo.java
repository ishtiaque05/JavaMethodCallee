package method.parser;

import java.util.List;

public class TestMethodInfo {
    String name;
    String methodSignature;
    String path;
    int startline;
    int endline;
    String formattedMethodSignature;
    String methodSignatureAsString;
    String actualCodeAsString;
    List<CalledMethodInfo> calledMethods;
    List<String> notFoundMethods;
    List<String> junitMethods;
    boolean shouldSkip;
}
