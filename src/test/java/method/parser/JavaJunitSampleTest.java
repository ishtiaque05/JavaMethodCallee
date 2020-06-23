package method.parser;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JavaJunitSampleTest {
    List<TestMethodInfo> tmethods = new ArrayList<TestMethodInfo>();
    @Before
    public void setUp(){
        Gson gson = new Gson();
        try {
            JsonReader reader = new JsonReader(new FileReader("src/test/resources/java-junit-sample.json"));
            Type type = new TypeToken<List<TestMethodInfo>>() {}.getType();
            tmethods = gson.fromJson(reader, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void totalTestMethodsTest() {
        Assert.assertEquals(14 ,tmethods.toArray().length);
    }

    @Test
    public void ItemServiceTotalMethodsTest() {
        int count = 0;
        String methodPathToTest = "src/test/java/com/codecentric/sample/store/service/ItemServiceTest.java";
        int totalTestInItemServiceClass = 5;
        for (TestMethodInfo tmethod : tmethods) {
            if(tmethod.path.endsWith(methodPathToTest)) {
                count++;
            }
        }
        Assert.assertEquals(totalTestInItemServiceClass, count);
    }

    @Test
    public void readItemDescriptionWithIOExceptionTotalCalleeWithDuplicatesTest() {
        String methodPathToTest = "src/test/java/com/codecentric/sample/store/service/ItemServiceTest.java";
        for(TestMethodInfo tmethod : tmethods) {
            if(tmethod.path.endsWith(methodPathToTest) && tmethod.methodName.equals("readItemDescriptionWithIOException")){
                // Total called methods
                Assert.assertEquals(3, tmethod.calledMethods.toArray().length);
                break;
            }
        }
    }

    @Test
    public void readItemDescriptionWithIOExceptionDuplicateTest() {

        String methodPathToTest = "src/test/java/com/codecentric/sample/store/service/ItemServiceTest.java";
        String duplicateMethodSignature = "com.codecentric.sample.store.service.tools.StaticService.readFile(java.lang.String)";
        CalledMethodInfo duplicateMethodInfo = null;
        int duplicateCount = 0;
        for(TestMethodInfo tmethod : tmethods) {
            if(tmethod.path.endsWith(methodPathToTest) && tmethod.methodName.equals("readItemDescriptionWithIOException")){
                for(CalledMethodInfo c : tmethod.calledMethods) {
                    if(c.fullQualifiedSignature.equals(duplicateMethodSignature) && c.startline == 12 ) {
                        duplicateMethodInfo = c;
                        duplicateCount++;
                    }
                }
            }
        }

        // readFile called twice from readItemDescriptionWithIOExceptionTest method
        Assert.assertEquals("readFile", duplicateMethodInfo.name);
        Assert.assertEquals(2, duplicateCount);
    }

}
