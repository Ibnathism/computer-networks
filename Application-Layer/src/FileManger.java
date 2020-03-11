import java.io.*;

public class FileManger {
    public static String openFile() {
        try {
            File file = new File("index.html");
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while(( line = br.readLine()) != null ) {
                sb.append( line );
                sb.append( '\n' );
            }
            return sb.toString();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }
}
