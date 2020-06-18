import demo.Calculator;
import demo.*;
import junit.framework.Assert;
import org.junit.Test;

public class CalculatorTest {

    @Test
    public void testSum() {
        // Given
        Calculator calculator = new Calculator();
        Calculator2 c = new Calculator2();
        ExtraCalculator ec = new ExtraCalculator();
        // When
        int result = calculator.add(2, 2);
        int result2 =  c.add(4,4);
        int result3 = ec.add(5,5);
        System.out.println(result3);
        // Then
        if (result != 4) {   // if 2 + 2 != 4
            Assert.fail();
        }
    }
}
