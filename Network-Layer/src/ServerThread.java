public class ServerThread implements Runnable {

    NetworkUtility networkUtility;
    EndDevice endDevice;

    ServerThread(NetworkUtility networkUtility, EndDevice endDevice) {
        this.networkUtility = networkUtility;
        this.endDevice = endDevice;
        System.out.println("Server Ready for client " + endDevice.getIpAddress());
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
            if (specialMessage.equals("SHOW_ROUTE")) showRouteAndSendBack();

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




    public Boolean deliverPacket(Packet p) {

        
        /*
        1. Find the router s which has an interface
                such that the interface and source end device have same network address.
        2. Find the router d which has an interface
                such that the interface and destination end device have same network address.
        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.
                similarly, x forwards to the next gateway router y considering d as the destination,
                and eventually the packet reaches to destination router d.

            3(a) If, while forwarding, any gateway x, found from routingTable of router r is in down state[x.state==FALSE]
                    (i) Drop packet
                    (ii) Update the entry with distance Constants.INFTY
                    (iii) Block NetworkLayerServer.stateChanger.t
                    (iv) Apply DVR starting from router r.
                    (v) Resume NetworkLayerServer.stateChanger.t

            3(b) If, while forwarding, a router x receives the packet from router y,
                    but routingTableEntry shows Constants.INFTY distance from x to y,
                    (i) Update the entry with distance 1
                    (ii) Block NetworkLayerServer.stateChanger.t
                    (iii) Apply DVR starting from router x.
                    (iv) Resume NetworkLayerServer.stateChanger.t

        4. If 3(a) occurs at any stage, packet will be dropped,
            otherwise successfully sent to the destination router
        */
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
