import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ServerThread implements Runnable{
    private static final int PACKET_SIZE = 1024;
    private Socket socket;


    ServerThread(Socket s) {
        this.socket = s;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            StringBuilder content = FileManger.openFile();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            String input = in.readLine();
            if(input != null) {
                System.out.println(input);
                if(input.length() > 0) {
                    if(input.startsWith("GET"))
                    {
                        String[] commands = input.split(" ", 3);
                        this.executeGetRequest(commands, dataOutputStream, content);
                    }
                    else if (input.startsWith("UPLOAD"))
                    {

                        String[] commands = input.split(" ");
                        this.executeUploadRequest(commands, in);
                    }
                }
            }
            socket.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeUploadRequest(String[] commands, BufferedReader bufferedReader) {

        try {
            FileOutputStream  fileOutputStream = new FileOutputStream("root/"+commands[1]);
            int n = 10;
            List<String> lines;
            do {
               lines = readNLines(bufferedReader, n, fileOutputStream);
            } while (!lines.isEmpty());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e){
            System.out.println("I/O Exception here in executeupload request " + e);
        }
    }

    private List<String> readNLines(BufferedReader br, int n, FileOutputStream fileOutputStream) {
        List<String> lines = new ArrayList<>(n);
        String line;
        while (true) {
            try {
                if (!(lines.size() < n && ((line = br.readLine()) != null))) break;
                lines.add(line);
                fileOutputStream.write(line.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return lines;
    }


    private void executeGetRequest(String[] commands, DataOutputStream dataOutputStream, StringBuilder content){
        File requestedFile = new File(commands[1]);
        try {
            if (requestedFile.exists()){
                if (!requestedFile.isDirectory()) this.downloadFile(dataOutputStream, requestedFile);
                else this.showFilesOfDirectory(content, commands, dataOutputStream);
            }
            else {
                this.generateErrorMsg(content, dataOutputStream);
            }
        } catch (Exception e){
            System.out.println("Exception " + e);
        }


    }

    private void generateErrorMsg(StringBuilder content, DataOutputStream dataOutputStream) {
        try {
            StringBuilder header = new StringBuilder();
            header.append("HTTP/1.1 404 NOT FOUND\r\n").append("Server: Java HTTP Server: 1.0\r\n").append("Date: ").append(new Date())
                    .append("\r\n").append("Content-Type: text/html\r\n").append("Content-Length: ").append(content.length()).append("\r\n").append("\r\n");
            dataOutputStream.writeBytes(header.toString());
            dataOutputStream.flush();
        } catch (IOException e){
            System.out.println("I/O Exception "+e);
        }
    }

    private void showFilesOfDirectory(StringBuilder content, String[] commands, DataOutputStream dataOutputStream) {

        try {
            //System.out.println("dir");
            FileManger.viewDirectory(content, commands[1]);
            StringBuilder header = new StringBuilder();
            header.append("HTTP/1.1 200 OK\r\n").append("Server: Java HTTP Server: 1.0\r\n").append("Date: " + new Date() + "\r\n")
                    .append("Content-Type: text/html\r\n").append("Content-Length: " + content.length() + "\r\n").append("\r\n");
            dataOutputStream.writeBytes(header.toString());
            dataOutputStream.writeBytes(content.toString());
            dataOutputStream.flush();
        } catch (IOException e)
        {
            System.out.println("I/O Exception "+ e);
        }
    }


    private void downloadFile(DataOutputStream dataOutputStream, File requestedFile){
        try {
            StringBuilder header = new StringBuilder();
            //System.out.println("filename : "+requestedFile.getName());
            header.append("HTTP/1.1 200 OK\r\n").append("Server: Java HTTP Server: 1.0\r\n").append("Date: ").append(new Date())
                    .append("\r\n").append("Content-Type: ").append(URLConnection.guessContentTypeFromName(requestedFile.getName())).append("\r\n")
                    .append("Content-Length: "+requestedFile.length()+"\r\n").append("Content-Disposition: attachment; filename=\""+requestedFile.getName()+"\"\r\n")
                    .append("Connection: close\r\n").append("\r\n");
            dataOutputStream.writeBytes(header.toString());
        } catch (IOException e){
            System.out.println("I/O exception found "+ e);
        }


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
                    break;
                }
            }
            dataOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
