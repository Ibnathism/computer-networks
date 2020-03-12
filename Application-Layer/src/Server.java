import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    static final int PORT = 6789;

    public Server() {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
            //String fileData = readFileData(file, Math.toIntExact(file.length()));
            //System.out.println(fileData);
            while(true)
            {
                Socket s = serverConnect.accept();
                ServerThread serverThread = new ServerThread(s);
                Thread thread = new Thread(serverThread);
                thread.start();
                thread.join();
                s.close();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }



    public static String readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return String.valueOf(fileData);
    }

    public static void main(String[] args) {

        new Server();

    }

}
