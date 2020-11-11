package method.parser;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AssertionHelper {

    // List obtained from: https://junit.org/junit5/docs/5.3.0/api/org/junit/jupiter/api/Assertions.html,
    // https://junit.org/junit4/javadoc/latest/org/junit/Assert.html
    // https://junit.org/junit5/docs/5.0.1/api/org/junit/jupiter/api/Assertions.html

    public static final List JUNIT_ASSERTION_API = Collections.unmodifiableList(
            Arrays.asList("assertArrayEquals",
                    "assertEquals",
                    "assertFalse",
                    "assertNotEquals",
                    "assertNotNull",
                    "assertNotSame",
                    "assertNull",
                    "assertSame",
                    "assertThat",
                    "assertThrows",
                    "assertTrue",
                    "fail",
                    "assertAll",
                    "assertIterableEquals",
                    "assertLinesMatch",
                    "assertTimeout",
                    "assertTimeoutPreemptively",
                    "assertDoesNotThrow")
    );
}
