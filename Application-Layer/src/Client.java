import java.io.IOException;
import java.net.Socket;

public class Client {
    private static final int PORT = 6789;
    private Client(){
        try {
            Socket socket = new Socket("localhost", PORT);
            System.out.println("Connection Established");
            new ClientThread(socket);
        } catch (IOException e){
            System.out.println("Socket not found " +e);
        }

    }

    public static void main(String[] args) {
        new Client();
    }

}
