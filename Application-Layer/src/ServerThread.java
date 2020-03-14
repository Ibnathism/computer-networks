import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.util.Date;


public class ServerThread implements Runnable{
    private static final int PACKET_SIZE = 100;
    private Socket socket;
    private Thread thread;


    public ServerThread(Socket s) {
        this.socket = s;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        try {


            StringBuilder content = FileManger.openFile();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //PrintWriter pr = new PrintWriter(socket.getOutputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            String input = in.readLine();
            //String content = "<html>Hello</html>";
            //System.out.println(content);
            if(input != null) {
                System.out.println(input);
                if(input.length() > 0) {
                    if(input.startsWith("GET"))
                    {


                        String[] commands = input.split(" ", 3);
                        File requestedFile = new File(commands[1]);
                        if (requestedFile.exists()){
                            if (!requestedFile.isDirectory()) {
                                //System.out.println("file");
                                System.out.println("filename : "+requestedFile.getName());
                                dataOutputStream.writeBytes("HTTP/1.1 200 OK\r\n");
                                //System.out.println("HTTP/1.1 200 OK\r\n");
                                dataOutputStream.writeBytes("Server: Java HTTP Server: 1.0\r\n");
                                //System.out.println("Server: Java HTTP Server: 1.0\r\n");
                                dataOutputStream.writeBytes("Date: "+new Date()+"\r\n");
                                //System.out.println("Date: "+new Date()+"\r\n");
                                dataOutputStream.writeBytes("Content-Type: "+ URLConnection.guessContentTypeFromName(requestedFile.getName())+"\r\n");
                                //System.out.println("Content-Type: "+ URLConnection.guessContentTypeFromName(requestedFile.getName())+"\r\n");
                                dataOutputStream.writeBytes("Content-Length: "+requestedFile.length()+"\r\n");
                                //System.out.println("Content-Length: "+requestedFile.length()+"\r\n");
                                dataOutputStream.writeBytes("Content-Disposition: attachment; filename=\""+requestedFile.getName()+"\"\r\n");
                                dataOutputStream.writeBytes("Connection: close\r\n");
                                dataOutputStream.writeBytes("\r\n");
                                //System.out.println("Content-Disposition: attachment; filename=\""+requestedFile.getName()+"\"\r\n");
                                //dataOutputStream.writeBytes(content.toString());
                                //System.out.println(content.toString());

                                int bytesRead = 0;
                                try (BufferedInputStream bufferedInputStreamForFile = new BufferedInputStream(new FileInputStream(requestedFile)))
                                {
                                    byte[] byteDataArray = new byte[PACKET_SIZE];
                                    while (true){
                                        bytesRead = bufferedInputStreamForFile.read(byteDataArray);
                                        if(bytesRead > 0){
                                            dataOutputStream.write(byteDataArray);
                                        } else {
                                            dataOutputStream.flush();
                                            dataOutputStream.close();
                                            //in.close();
                                            break;
                                        }
                                    }

                                    dataOutputStream.close();
                                    in.close();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //dataOutputStream.flush();


                            }
                            else {
                                //System.out.println("dir");
                                FileManger.viewDirectory(content, commands[1]);
                                dataOutputStream.writeBytes("HTTP/1.1 200 OK\r\n");
                                dataOutputStream.writeBytes("Server: Java HTTP Server: 1.0\r\n");
                                dataOutputStream.writeBytes("Date: " + new Date() + "\r\n");
                                dataOutputStream.writeBytes("Content-Type: text/html\r\n");
                                dataOutputStream.writeBytes("Content-Length: " + content.length() + "\r\n");
                                dataOutputStream.writeBytes("\r\n");
                                //System.out.println(path);
                                dataOutputStream.writeBytes(content.toString());
                                dataOutputStream.flush();
                            }
                        }
                        else {
                            dataOutputStream.writeBytes("HTTP/1.1 404 NOT FOUND\r\n");
                            dataOutputStream.writeBytes("Server: Java HTTP Server: 1.0\r\n");
                            dataOutputStream.writeBytes("Date: " + new Date() + "\r\n");
                            dataOutputStream.writeBytes("Content-Type: text/html\r\n");
                            dataOutputStream.writeBytes("Content-Length: " + content.length() + "\r\n");
                            dataOutputStream.writeBytes("\r\n");
                            //System.out.println(path);
                            dataOutputStream.flush();
                        }

                    }

                    else
                    {

                    }
                }
            }
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }



}
