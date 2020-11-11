package method.parser;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;

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

    public static List<String> readJSON(String processedCommitsPath) throws FileNotFoundException {
        List<String> commits = new ArrayList<String>();
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(processedCommitsPath));
        Type type = new TypeToken<List<String>>() {}.getType();
        commits = gson.fromJson(reader, type);

        return commits;
    }
}
