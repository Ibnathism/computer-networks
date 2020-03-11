import java.io.*;
import java.net.Socket;
import java.util.Date;


public class ServerThread implements Runnable{
    private Socket socket;


    public ServerThread(Socket s) {
        this.socket = s;
    }

    @Override
    public void run() {

        try {

            String content = FileManger.openFile();
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
                        pr.write("HTTP/1.1 200 OK\r\n");
                        pr.write("Server: Java HTTP Server: 1.0\r\n");
                        pr.write("Date: " + new Date() + "\r\n");
                        pr.write("Content-Type: text/html\r\n");
                        pr.write("Content-Length: " + content.length() + "\r\n");
                        pr.write("\r\n");
                        pr.write(content);
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
