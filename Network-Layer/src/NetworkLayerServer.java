import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//Work needed
public class NetworkLayerServer {

    static int clientCount = 0;
    static ArrayList<Router> routers = new ArrayList<>();
    static RouterStateChanger stateChanger = null;
    static Map<IPAddress,Integer> clientInterfaces = new HashMap<>(); //Each map entry represents number of client end devices connected to the interface
    static Map<IPAddress, EndDevice> endDeviceMap = new HashMap<>();
    static ArrayList<EndDevice> endDevices = new ArrayList<>();
    static Map<Integer, Integer> deviceIDtoRouterID = new HashMap<>();
    static Map<IPAddress, Integer> interfacetoRouterID = new HashMap<>();
    static Map<Integer, Router> routerMap = new HashMap<>();

    public static void main(String[] args) {

        //Task: Maintain an active client list

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(4444);
        } catch (IOException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Server Ready: " + serverSocket.getInetAddress().getHostAddress());
        System.out.println("Creating router topology");

        readTopology();
        //printRouters();

        initRoutingTables(); //Initialize routing tables for all routers

        DVR(1); //Update routing table using distance vector routing until convergence
        //simpleDVR(1);

        for (Router router: routers) {
            router.printRoutingTable();
        }

        //stateChanger = new RouterStateChanger();//Starts a new thread which turns on/off routers randomly depending on parameter Constants.LAMBDA

        while(true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Client" + clientCount + " attempted to connect");
                EndDevice endDevice = getClientDeviceSetup();
                //if (!routerMap.get(interfacetoRouterID.get(endDevice.getGateway())).getState()) continue;
                clientCount++;
                endDevices.add(endDevice);
                endDeviceMap.put(endDevice.getIpAddress(),endDevice);
                new ServerThread(new NetworkUtility(socket), endDevice);
            } catch (IOException ex) {
                Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void initRoutingTables() {
        for (Router router : routers) {
            router.initiateRoutingTable();
        }
    }

    public static void applyDVR(int startingRouterID) {
        //NetworkLayerServer.simpleDVR(startingRouterID);
        NetworkLayerServer.DVR(startingRouterID);
    }

    public static synchronized void DVR(int startingRouterId) {
        System.out.println("DVR STARTED");
        if (stateChanger!=null) stateChanger.isPause = true;
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.print("Down Routers ");
        for (Router router: NetworkLayerServer.routers) {
            if (!router.getState()) System.out.print("  "+router.getRouterId()+",");
        }
        System.out.println();


        boolean isConvergence = false;
        Router startingRouter = routerMap.get(startingRouterId);

        while (!isConvergence) {
            boolean isUpdate = sfUpdate(startingRouter);

            for (Router router: routers) {
                isUpdate = isUpdate | sfUpdate(router);
            }
            isConvergence = !isUpdate;
        }
        System.out.println("DVR ENDED");
        //stateChanger = new RouterStateChanger();
    }

    private static boolean sfUpdate(Router router) {
        boolean temp = false;
        for (int neighborId: router.getNeighborRouterIDs()) {
            Router neighborRouter = routerMap.get(neighborId);
            if (neighborRouter.getState()) {
                temp = temp | neighborRouter.sfupdateRoutingTable(router);
            }
        }
        return temp;
    }

    public static boolean update(Router router){
        boolean temp = false;
        for (int neighborId: router.getNeighborRouterIDs()) {
            Router neighborRouter = routerMap.get(neighborId);
            if (neighborRouter.getState()) {
                temp = temp | neighborRouter.updateRoutingTable(router);
            }
        }
        return temp;
    }

    public static synchronized void simpleDVR(int startingRouterId) {   ///TODO: Should we use routing tables of previous iteration?
        System.out.println("DVR STARTED");
        if (stateChanger!=null) stateChanger.isPause = true;
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.print("Down Routers ");
        for (Router router: NetworkLayerServer.routers) {
            if (!router.getState()) System.out.print("  "+router.getRouterId()+",");
        }
        System.out.println();


        boolean isConvergence = false;
        Router startingRouter = routerMap.get(startingRouterId);

        while (!isConvergence) {
            boolean isUpdate = update(startingRouter);

            for (Router router: routers) {
                isUpdate = isUpdate | update(router);
            }
            isConvergence = !isUpdate;
        }
        System.out.println("DVR ENDED");
        //stateChanger = new RouterStateChanger();
    }

    public static EndDevice getClientDeviceSetup() {
        Random random = new Random(System.currentTimeMillis());
        int r = Math.abs(random.nextInt(clientInterfaces.size()));

        //System.out.println("Size: " + clientInterfaces.size() + "\n" + r);

        Map.Entry<IPAddress, Integer> entry = (Map.Entry<IPAddress, Integer>) clientInterfaces.entrySet().toArray()[r];

        IPAddress gateway = entry.getKey();
        Integer value = entry.getValue();

        int deviceID = clientCount;
        IPAddress ip = new IPAddress(gateway.getBytes()[0] + "." + gateway.getBytes()[1] + "." + gateway.getBytes()[2] + "." + (value+2));
        value++;
        clientInterfaces.put(gateway, value);
        deviceIDtoRouterID.put(endDevices.size(), interfacetoRouterID.get(gateway));

        EndDevice device = new EndDevice(ip, gateway, clientCount);

        System.out.println("Device : " + ip + "::::" + gateway);
        return device;
    }

    public static void printRouters() {
        for(int i = 0; i < routers.size(); i++) {
            System.out.println("------------------\n" + routers.get(i));
        }
    }

    public static String strrouters() {
        String string = "";
        for (int i = 0; i < routers.size(); i++) {
            string += "\n------------------\n" + routers.get(i).strRoutingTable();
        }
        string += "\n\n";
        return string;
    }

    public static void readTopology() {
        Scanner inputFile = null;
        try {
            inputFile = new Scanner(new File("topology.txt"));
            //skip first 27 lines
            int skipLines = 27;
            for(int i = 0; i < skipLines; i++) {
                inputFile.nextLine();
            }

            //start reading contents
            while(inputFile.hasNext()) {
                inputFile.nextLine();
                int routerId;
                ArrayList<Integer> neighborRouters = new ArrayList<>();
                ArrayList<IPAddress> interfaceAddrs = new ArrayList<>();
                Map<Integer, IPAddress> interfaceIDtoIP = new HashMap<>();

                routerId = inputFile.nextInt();

                int count = inputFile.nextInt();
                for(int i = 0; i < count; i++) {
                    neighborRouters.add(inputFile.nextInt());
                }
                count = inputFile.nextInt();
                inputFile.nextLine();

                for(int i = 0; i < count; i++) {
                    String string = inputFile.nextLine();
                    IPAddress ipAddress = new IPAddress(string);
                    interfaceAddrs.add(ipAddress);
                    interfacetoRouterID.put(ipAddress, routerId);

                    /**
                     * First interface is always client interface
                     */
                    if(i == 0) {
                        //client interface is not connected to any end device yet
                        clientInterfaces.put(ipAddress, 0);
                    }
                    else {
                        interfaceIDtoIP.put(neighborRouters.get(i - 1), ipAddress);
                    }
                }
                Router router = new Router(routerId, neighborRouters, interfaceAddrs, interfaceIDtoIP);
                routers.add(router);
                routerMap.put(routerId, router);
            }


        } catch (FileNotFoundException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
