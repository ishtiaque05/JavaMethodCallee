package method.parser;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.junit.Assert;
import org.junit.Before;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.google.common.reflect.TypeToken;
import org.junit.Test;

public class CalculatorCallGraphJsonTest {
    List<TestMethodInfo> tmethods = new ArrayList<TestMethodInfo>();
    @Before
    public void setUp(){
        Gson gson = new Gson();
        try {
            JsonReader reader = new JsonReader(new FileReader("src/test/resources/calculator-unit-test-example-java.json"));
            Type type = new TypeToken<List<TestMethodInfo>>() {}.getType();
            tmethods = gson.fromJson(reader, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void SumCallTest(){
        for (TestMethodInfo tmethod : tmethods) {
            if(tmethod.methodSignature.equalsIgnoreCase("testSum()")) {
             Assert.assertEquals(1, tmethod.calledMethods.toArray().length);
             Assert.assertEquals("com.github.stokito.unitTestExample.calculator",tmethod.calledMethods.get(0).packageName);
             Assert.assertEquals("sum", tmethod.calledMethods.get(0).name);
             Assert.assertEquals(5 ,tmethod.calledMethods.get(0).startline);
             Assert.assertEquals("com.github.stokito.unitTestExample.calculator.Calculator.sum(int, int)" ,tmethod.calledMethods.get(0).fullQualifiedSignature);
             Assert.assertTrue(tmethod.calledMethods.get(0).path.endsWith("src/main/java/com/github/stokito/unitTestExample/calculator/Calculator.java"));
             break;
            }
        }
    }
    @Test
    public void totalTestMethodsTest() {
        Assert.assertEquals(4, tmethods.toArray().length);
    }
}
