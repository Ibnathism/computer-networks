import java.util.ArrayList;

public class ServerThread implements Runnable {

    NetworkUtility networkUtility;
    EndDevice endDevice;

    ServerThread(NetworkUtility networkUtility, EndDevice endDevice) {
        this.networkUtility = networkUtility;
        this.endDevice = endDevice;
        //System.out.println("Server Ready for client " + endDevice.getIpAddress());
        //NetworkLayerServer.clientCount++;
        new Thread(this).start();
    }

    @Override
    public void run() {
        /**
         * Synchronize actions with client.
         */
        networkUtility.write("End-Device-Config::" + endDevice.getDeviceID().toString() + '-' + endDevice.getIpAddress() + '-' + endDevice.getGateway());
        int active = NetworkLayerServer.endDevices.size();
        networkUtility.write(""+ active);
        for (EndDevice endDevice: NetworkLayerServer.endDevices) {
            networkUtility.write("End-Device-Config::" + endDevice.getDeviceID().toString() + '-' + endDevice.getIpAddress() + '-' + endDevice.getGateway());
        }
        while(true) {
            //Tasks:
            //1. Upon receiving a packet and recipient, call deliverPacket(packet)
            String s = (String) networkUtility.read();
            System.out.println("Request From -----" + endDevice.getIpAddress().getString()+" :::: "+s);
            String message = s.split("-")[0];
            IPAddress destIp = new IPAddress(s.split("-")[1]);
            String specialMessage = s.split("-")[2];

            Packet packet = new Packet(message, specialMessage, endDevice.getIpAddress(), destIp);

            //2. If the packet contains "SHOW_ROUTE" request, then fetch the required information
            //and send back to client
            if (specialMessage.equals(Constants.SHOW_ROUTE)) showRouteAndSendBack();

            //3. Either send acknowledgement with number of hops or send failure message back to client
            boolean isPacketDelivered = deliverPacket(packet);
            if (isPacketDelivered) sendAcknowledgement();
            else sendFailure();
        }


    }

    private void sendFailure() {

    }

    private void sendAcknowledgement() {

    }

    void showRouteAndSendBack() {

    }


    private EndDevice getDevice(IPAddress ipAddress) {
        EndDevice device = null;
        for (EndDevice endDevice: NetworkLayerServer.endDevices) {
            if (ipAddress.getString().equals(endDevice.getIpAddress().getString())) {
                device = endDevice;
                break;
            }
        }
        return device;
    }
    void dropPacket(Packet packet) {
        //System.out.println("Packet Dropped "+packet.getMessage());
        networkUtility.write(Constants.FAILURE);
    }

    public void applyDVR(int startingRouterID) {
        NetworkLayerServer.simpleDVR(startingRouterID);
        //NetworkLayerServer.DVR(startingRouterID);
    }


    public Boolean deliverPacket(Packet p) {
        ArrayList<Router> hops = new ArrayList<>();
        StringBuilder hopString = new StringBuilder();

        EndDevice source = getDevice(p.getSourceIP());
        Integer routerIdOfSource = -1;
        assert source!=null;
        routerIdOfSource = NetworkLayerServer.deviceIDtoRouterID.get(source.getDeviceID());
        Router s = NetworkLayerServer.routerMap.get(routerIdOfSource);;
        assert s != null;
        //System.out.println("Source Router : " + s.getRouterId());

        EndDevice dest = getDevice(p.getDestinationIP());
        Integer routerIdOfDest = -1;
        assert dest!=null;
        routerIdOfDest = NetworkLayerServer.deviceIDtoRouterID.get(dest.getDeviceID());
        Router d = NetworkLayerServer.routerMap.get(routerIdOfDest);;
        assert d != null;
        //System.out.println("Destination Router : " + d.getRouterId());
        int count = 0;

        Router prev = s;
        Router next = null;
        if (!prev.getState()){
            System.out.println("Gateway Router is OFF");
            dropPacket(p);
        }
        else {
            do {

                count++;
                if (count>Constants.INFINITY) {
                    dropPacket(p);
                    break;
                }
                p.hopcount++;
                next = NetworkLayerServer.routerMap.get(prev.getRTEntry(d.getRouterId()).getGatewayRouterId());

                if (next==null || !next.getState()) {
                    dropPacket(p);
                    if (prev.getRTEntry(d.getRouterId())!=null) prev.getRTEntry(d.getRouterId()).setDistance(Constants.INFINITY);
                    applyDVR(prev.getRouterId());
                    break;
                }
                System.out.println(prev.getRouterId() + " -> " + next.getRouterId());
                if (next.getRTEntry(prev.getRouterId())!=null && next.getRTEntry(prev.getRouterId()).getDistance()==Constants.INFINITY) {
                    next.getRTEntry(prev.getRouterId()).setDistance(1);
                    applyDVR(next.getRouterId());
                }
                hops.add(prev);
                prev = next;

            } while (next.getRouterId()!=d.getRouterId());


            if (next!=null && next.getRouterId()==d.getRouterId()) {
                hops.add(next);
                //System.out.println("Packet Sending Successful");
                networkUtility.write(Constants.SUCCESS);
                System.out.println("HOPS "+ p.hopcount);
                hopString.append("Hop Count ").append(p.hopcount).append(":").append("PATH ");
                for (Router router: hops) {
                    System.out.print("-> " + router.getRouterId());
                    hopString.append(router.getRouterId()).append("-");
                }
                hopString.append(":Routing Tables\n");
                for (Router router: hops){
                    hopString.append("Table Of Router ").append(router.getRouterId()).append("\n");
                    for (RoutingTableEntry entry: router.getRoutingTable()) {
                        hopString.append(entry.getRouterId()).append("-").append(entry.getDistance()).append("-").append(entry.getGatewayRouterId()).append("\n");
                    }

                }
                networkUtility.write(hopString.toString());
                System.out.println();
                return true;
            }

        }

        /*int tempRouterId = -1;
        Router tempRouter = s;
        Router hopRouter = s;
        while (tempRouterId!=d.getRouterId()) {
            count++;
            if (count>Constants.INFINITY) {
                dropPacket(p);
                break;
            }
            p.hopcount++;
            if (!tempRouter.getState()) {
                dropPacket(p);
                if (tempRouter.getRTEntry(d.getRouterId())!=null) tempRouter.getRTEntry(d.getRouterId()).setDistance(Constants.INFINITY);
                applyDVR(tempRouter.getRouterId());
                break;
            }
            else {
                tempRouterId = tempRouter.getRTEntry(d.getRouterId()).getGatewayRouterId();

                //System.out.println("TEMP ROUTER ID "+tempRouterId);
                if (tempRouterId==-1) {
                    dropPacket(p);
                    tempRouter.getRTEntry(d.getRouterId()).setDistance(Constants.INFINITY);
                    applyDVR(tempRouter.getRouterId());
                    break;
                }

                hopRouter = NetworkLayerServer.routerMap.get(tempRouterId);
                if (hopRouter.getRTEntry(tempRouter.getRouterId())!=null && hopRouter.getRTEntry(tempRouter.getRouterId()).getDistance()==Constants.INFINITY) {
                    hopRouter.getRTEntry(tempRouter.getRouterId()).setDistance(1);
                    applyDVR(tempRouter.getRouterId());
                }
                //System.out.println("Forwarding "+ tempRouter.getRouterId() +" to " + hopRouter.getRouterId());
                hops.add(tempRouter);
                tempRouter =  NetworkLayerServer.routerMap.get(tempRouterId);
            }
        }

        if (tempRouterId==d.getRouterId()) {
            hops.add(tempRouter);
            //System.out.println("Packet Sending Successful");
            networkUtility.write(Constants.SUCCESS);
            System.out.println("HOPS "+ p.hopcount);
            hopString.append("Hop Count ").append(p.hopcount).append(":").append("PATH ");
            for (Router router: hops) {
                System.out.print("-> " + router.getRouterId());
                hopString.append(router.getRouterId()).append("-");
            }
            hopString.append(":Routing Tables\n");
            for (Router router: hops){
                hopString.append("Table Of Router ").append(router.getRouterId()).append("\n");
                for (RoutingTableEntry entry: router.getRoutingTable()) {
                    hopString.append(entry.getRouterId()).append("-").append(entry.getDistance()).append("-").append(entry.getGatewayRouterId()).append("\n");
                }

            }
            networkUtility.write(hopString.toString());
            System.out.println();
            return true;
        }*/
        return false;
    }

    public void disconnectClient(EndDevice endDevice) {
        Integer count = NetworkLayerServer.clientInterfaces.get(endDevice.getIpAddress());
        count--;
        NetworkLayerServer.clientInterfaces.put(endDevice.getIpAddress(), count);

        //NetworkLayerServer.endDeviceMap.remove()
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }
}
