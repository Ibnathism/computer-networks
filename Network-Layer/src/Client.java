import java.util.ArrayList;

//Work needed
public class Client {
    static EndDevice getDevice(String str){
        String[] endDeviceConfig = str.split("-");
        IPAddress endDeviceIP = new IPAddress(endDeviceConfig[1]);
        IPAddress endDeviceGateway = new IPAddress(endDeviceConfig[2]);
        EndDevice myDevice = new EndDevice(endDeviceIP, endDeviceGateway, Integer.parseInt(endDeviceConfig[0]));
        System.out.println("Device "+myDevice.getDeviceID()+"  IP: " +myDevice.getIpAddress()+"  Gateway: "+myDevice.getGateway());
        return myDevice;
    }
    public static void main(String[] args) throws InterruptedException {
        NetworkUtility networkUtility = new NetworkUtility("127.0.0.1", 4444);
        System.out.println("Connected to server");
        EndDevice myDevice;
        ArrayList<EndDevice> activeClientList = new ArrayList<>();
        /**
         * Tasks
         */
        while (true) {
            String s = (String) networkUtility.read();
            System.out.println("Received: " + s);
            String message = s.split("::")[0];
            myDevice = getDevice(s.split("::")[1]);


            s = (String) networkUtility.read();
            System.out.println("Number of active devices "+s);
            int active = Integer.parseInt(s);
            for (int i = 0; i < active; i++) {
                s = (String) networkUtility.read();
                EndDevice client = getDevice(s.split("::")[1]);
                activeClientList.add(client);
                //System.out.println("Client #"+i+ ":: "+s);
            }


        }

        /*
        1. Receive EndDevice configuration from server
        2. Receive active client list from server
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the message and recipient IP address to server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
                            all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
                    Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
        */
    }
}
