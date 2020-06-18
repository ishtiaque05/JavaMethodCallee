import demo.Calculator;
import demo.*;
import junit.framework.Assert;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class CalculatorTest {

    @Test
    public void testSum(int x) {
        // Given
        Calculator calculator = new Calculator();
        Calculator2 c = new Calculator2();
        ExtraCalculator ec = new ExtraCalculator();
        // When
        int result = calculator.add(2, 2);
        int result2 =  c.add(4,4);
        int result3 = ec.add(5,5);
        File srcDir = new File("/home/ishtiaque/Desktop/projects/JavaMethodCallee/testExamples/testCallGraph/src");
        if (srcDir.exists()) {
            assertTrue(srcDir.exists());
            srcDir.listFiles();
        }
        assertEquals(0,0);
        assertEquals(1,1);

        System.out.println(result3);
        // Then
        if (result != 4) {   // if 2 + 2 != 4
            Assert.fail();
        }
    }
}
