import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Stack;


public class ServerThread implements Runnable{
    private Socket socket;


    public ServerThread(Socket s) {
        this.socket = s;
    }

    /*private String getPath(String input){
        int len = input.length();
        String path = input.substring(5, len-9);
        //System.out.println(path);
        String str = "E:\\Workspaces\\IntelliJ\\Computer-Networks\\Application-Layer";
        System.out.println("Path : "+path);
        if (path.equals("/")) path = path+str;
        else path = "/"+ str  + path;
        return path;
    }*/

    @Override
    public void run() {

        try {

            StringBuilder content = FileManger.openFile();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pr = new PrintWriter(socket.getOutputStream());
            String input = in.readLine();
            //String content = "<html>Hello</html>";
            //System.out.println(content);
            if(input != null) {
                System.out.println(input);
                if(input.length() > 0) {
                    if(input.startsWith("GET"))
                    {
                        /*String path = this.getPath(input);
                        System.out.println("PATH : "+path);
                        content = FileManger.viewDirectory(path, content, path);
                        */
                        /*//System.out.println("Commands "+commands[1]);
                        String directoryPath =  "E:\\Workspaces\\IntelliJ\\Computer-Networks\\Application-Layer"+commands[1];
                        //System.out.println(directoryPath);
                        String[] dir = directoryPath.split("/");
                        System.out.println(dir[dir.length-1]);*/


                        String[] commands = input.split(" ", 3);
                        FileManger.viewDirectory(content, commands[1]);





                        pr.write("HTTP/1.1 200 OK\r\n");
                        pr.write("Server: Java HTTP Server: 1.0\r\n");
                        pr.write("Date: " + new Date() + "\r\n");
                        pr.write("Content-Type: text/html\r\n");
                        pr.write("Content-Length: " + content.length() + "\r\n");
                        pr.write("\r\n");

                        //System.out.println(path);

                        pr.write(content.toString());
                        pr.flush();
                    }

                    else
                    {

                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
