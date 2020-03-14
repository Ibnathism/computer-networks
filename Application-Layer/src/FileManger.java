import java.io.*;
import java.nio.charset.StandardCharsets;

class FileManger {
    private boolean type;   // type = 0 when file, type = 1 when dir
    static StringBuilder openFile() {
        try {
            File file = new File("index.html");
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while(( line = br.readLine()) != null ) {
                sb.append( line );
                sb.append( '\n' );
            }
            return sb;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

    static void viewDirectory(StringBuilder stringBuilder, String path) {
        File[] files = new File(path).listFiles();
        if (path.equals("/")) {
            path = "";
        }
        else{
            String[] allPaths = path.split("/");
            int len = allPaths.length;
            path=allPaths[len-1];
        }

        assert files != null;
        for (File file : files) {
            if(file.isDirectory()) {
                stringBuilder.append("<b><a href=\"").append(path).append("/").append(file.getName()).append("\">").append(file.getName()).append("</a></b><br>");

            }else{
                stringBuilder.append("<a href=\"").append(path).append("/").append(file.getName()).append("\">").append(file.getName()).append("</a><br>");
            }
        }
    }

/*
    public static void main(String[] args) {
        String path = "E:\\Workspaces\\IntelliJ\\Computer-Networks\\Application-Layer\\root";
        File file = new File(path);
        listf(file.getAbsolutePath(),new StringBuilder());

    }*/
}

