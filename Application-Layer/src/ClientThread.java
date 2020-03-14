import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientThread implements Runnable {
    private Socket socket;
    private static final int PACKET_SIZE = 1024;
    public ClientThread(Socket socket) {
        this.socket = socket;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        System.out.println("Enter Full Path of the File you want to upload: ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        File file = new File(input);
        String request = "UPLOAD "+input+"\n";
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            if (file.exists()){
                //System.out.println("File found "+ file.getName());
                dataOutputStream.writeBytes(request);
                dataOutputStream.flush();
                uploadFile(dataOutputStream, file);
                socket.close();
            }
            else {
                System.out.println("File not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void uploadFile(DataOutputStream dataOutputStream, File file){

        int bytesRead = 0;
        try (BufferedInputStream bufferedInputStreamForFile = new BufferedInputStream(new FileInputStream(file)))
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
