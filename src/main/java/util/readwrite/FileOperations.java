package util.readwrite;

import java.io.*;

public class FileOperations {
    public static String loadAsString(File file) throws IOException {

        //source https://javarevisited.blogspot.com/2015/09/how-to-read-file-into-string-in-java-7.html

        InputStream is = new FileInputStream(file);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));

        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();

        while(line != null){
            sb.append(line).append("\n");
            line = buf.readLine();
        }

        String fileAsString = sb.toString();
        return fileAsString;
    }
}
