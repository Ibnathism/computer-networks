import java.io.*;

public class FileManger {
    public static StringBuilder openFile() {
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
            //FileManger.listf("root", sb);


            /*for (File f: files){
                sb.append("<b><a href=\""+f.getName()+"\">"+f.getName()+"</a></b><br>");
            }*/
            return sb;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

    public static StringBuilder viewDirectory(StringBuilder stringBuilder, String path) {
        //path = "E:\\Workspaces\\IntelliJ\\Computer-Networks\\Application-Layer" + path;
        File[] files = new File(path).listFiles();
        if (path.equals("/")) {
            path = "";
            //stringBuilder.append("<b><a href=\"localhost:6789/root/\">" + "root" + "</a></b><br>");
            //return stringBuilder;
        }
        else{
            String[] allPaths = path.split("/");
            int len = allPaths.length;
            path=allPaths[len-1];
        }

        //String[] dirs = path.split("/");
        //String directoryName = dirs[dirs.length-1];
        for (File file : files) {
            if(file.isDirectory()) {
                //System.out.println(file.getName()+" path : "+path);
                stringBuilder.append("<b><a href=\"" + path + "/" + file.getName() + "\">" + file.getName() + "</a></b><br>");
            }else{
                //System.out.println(file.getName());
                stringBuilder.append("<a href=\"" + path + "/" + file.getName() + "\">" + file.getName() + "</a><br>");
            }
        }
        /*try {
            File directory = new File(directoryName);
            File[] files = directory.listFiles();

            if(files != null)
                for (File file : files) {
                    if (file.isFile()) {
                        //System.out.println(file.getName());
                        stringBuilder.append("<b><a href=\""+file.getName()+"\">"+file.getName()+"</a></b><br>");
                    } else if (file.isDirectory()) {
                        //System.out.println(file.getName() + " is a dir");
                        stringBuilder.append("<b><a href=\""+path+file.getName()+"\">"+file.getName()+"</a></b><br>");
                    }
                }

            *//*for (File f: files){
                sb.append("<b><a href=\""+f.getName()+"\">"+f.getName()+"</a></b><br>");
            }*//*
            return stringBuilder;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
*/
        return stringBuilder;
    }




    /*public static StringBuilder listf(String directoryName, StringBuilder stringBuilder) {
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        if(fList != null)
            for (File file : fList) {
                if (file.isFile()) {
                    //System.out.println(file.getName());
                    stringBuilder.append("<b><a href=\""+file.getName()+"\">"+file.getName()+"</a></b><br>");
                } else if (file.isDirectory()) {
                    //System.out.println(file.getName() + " is a dir");
                    stringBuilder.append("<b><a href=\""+file.getName()+"\">"+file.getName()+"</a></b><br>");
                }
            }
        return stringBuilder;
    }*/

/*
    public static void main(String[] args) {
        String path = "E:\\Workspaces\\IntelliJ\\Computer-Networks\\Application-Layer\\root";
        File file = new File(path);
        listf(file.getAbsolutePath(),new StringBuilder());

    }*/
}

