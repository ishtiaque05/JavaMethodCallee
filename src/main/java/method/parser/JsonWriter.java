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

    public static void writeToFile(String outputPath, List<String> processedCommits) {
        String json = new Gson().toJson(processedCommits);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
            writer.append(json);
            writer.close();
        }catch(Exception e){
            //to-do

        }
    }
}
