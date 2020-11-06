import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

//Work needed
public class Client {

    static EndDevice getDevice(String str){
        String[] endDeviceConfig = str.split("-");
        IPAddress endDeviceIP = new IPAddress(endDeviceConfig[1]);
        IPAddress endDeviceGateway = new IPAddress(endDeviceConfig[2]);
        //System.out.println("Device "+myDevice.getDeviceID()+"  IP: " +myDevice.getIpAddress()+"  Gateway: "+myDevice.getGateway());
        return new EndDevice(endDeviceIP, endDeviceGateway, Integer.parseInt(endDeviceConfig[0]));
    }
    public static void main(String[] args) throws InterruptedException {
        NetworkUtility networkUtility = new NetworkUtility("127.0.0.1", 4444);
        System.out.println("Connected to server");
        int totalPackets = 100;
        int successCount = 1;
        int failureCount = 0;

        EndDevice myDevice;
        ArrayList<EndDevice> activeClientList = new ArrayList<>();


            String s = (String) networkUtility.read();
            System.out.println("Received: " + s);
            String msg = s.split("::")[0];
            myDevice = getDevice(s.split("::")[1]);

            s = (String) networkUtility.read();
            //System.out.println("Number of active devices "+s);
            int active = Integer.parseInt(s);
            for (int i = 0; i < active; i++) {
                s = (String) networkUtility.read();
                EndDevice client = getDevice(s.split("::")[1]);
                if (!myDevice.getIpAddress().getString().equals(client.getIpAddress().getString())) {
                    //System.out.println("Client "+client.getIpAddress() +" added");
                    activeClientList.add(client);
                }
                //System.out.println("Client #"+i+ ":: "+s);
            }

            System.out.println("-------------------------");
            Random random = new Random(System.currentTimeMillis());
            int r;
            int totalHopCount = 0;
            boolean isShowRoute;
            for(int i=0;i<totalPackets;i++)
            {
                String message = "MESSAGE" + i;
                if (activeClientList.size()>2) {
                    r = random.nextInt(activeClientList.size());
                    EndDevice receiver = activeClientList.get(r);
                    System.out.println("Want to send " + message+" to "+receiver.getIpAddress().getString());
                    if (i==20) {
                        networkUtility.write(message+"-"+receiver.getIpAddress().getString()+"-"+Constants.SHOW_ROUTE);
                        isShowRoute = true;
                    }
                    else {
                        networkUtility.write(message+"-"+receiver.getIpAddress().getString()+"-"+Constants.NORMAL_MESSAGE);
                        isShowRoute = false;
                    }

                    s = (String) networkUtility.read();
                    if (s.equals(Constants.SUCCESS)) {
                        successCount++;
                        System.out.println(Constants.SUCCESS);
                        String str = (String) networkUtility.read();
                        String[] outputs = str.split(":");
                        System.out.println(outputs[0]);
                        totalHopCount = totalHopCount + Integer.parseInt(outputs[0].split("=")[1]);
                        if (isShowRoute) {
                            System.out.println(outputs[1]);
                            System.out.println(outputs[2]);
                        }
                    }
                    else if (s.equals(Constants.FAILURE)) {
                        failureCount++;
                        System.out.println(Constants.FAILURE);

                    }
                    else System.out.println("Not Valid");
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        showStats(totalPackets, successCount, failureCount, totalHopCount);
            while (true);





    }


    private static void showStats(int total, int success, int failure, int hopCount) {

        double dropRate = ((failure*1.0)/total)*100;
        double avgHops = ((hopCount*1.0)/success);
        System.out.println("Drop Rate : "+dropRate+"%");
        System.out.println("Average Hops : "+avgHops);

    }
}
