package method.parser;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

public class JsonWriter {
    public static void writeToJSON(String outputPath, List<TestMethodInfo> methods) {
        String json = new Gson().toJson(methods);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));

            writer.write(json);

            writer.close();

        }catch(Exception e){
            //to-do

        }
    }
}
