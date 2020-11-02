//Work needed

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class Router {
    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddresses;//list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    private ArrayList<Integer> neighborRouterIDs;//Contains both "UP" and "DOWN" state routers
    private Boolean state;//true represents "UP" state and false is for "DOWN" state
    private Map<Integer, IPAddress> gatewayIDtoIP;
    public Router() {
        interfaceAddresses = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIDs = new ArrayList<>();

        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p < 0.80) state = true;
        else state = false;

        numberOfInterfaces = 0;
    }

    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddresses, Map<Integer, IPAddress> gatewayIDtoIP) {
        this.routerId = routerId;
        this.interfaceAddresses = interfaceAddresses;
        this.neighborRouterIDs = neighborRouters;
        this.gatewayIDtoIP = gatewayIDtoIP;
        routingTable = new ArrayList<>();



        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p < 0.80) state = true;
        else state = false;

        //state = true;

        numberOfInterfaces = interfaceAddresses.size();
    }

    @Override
    public String toString() {
        String string = "";
        string += "Router ID: " + routerId + "\n" + "Interfaces: \n";
        for (int i = 0; i < numberOfInterfaces; i++) {
            string += interfaceAddresses.get(i).getString() + "\t";
        }
        string += "\n" + "Neighbor Router IDs: \n";
        for(int i = 0; i < neighborRouterIDs.size(); i++) {
            string += neighborRouterIDs.get(i) + "\t";
        }
        /*string += "\n" + "GatewayIDtoIp Map:";
        for(Map.Entry<Integer, IPAddress> item: gatewayIDtoIP.entrySet()) {
            string += "\n" + item.getKey() + " -> " +item.getValue();
        }*/
        return string;
    }



    /**
     * Initialize the distance(hop count) for each router.
     * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable() {
        for (Router router: NetworkLayerServer.routers) {
            double distance;
            int gatewayID;
            if(router.routerId == this.routerId) {
                distance = 0;
                gatewayID = router.routerId;
            }
            else if(this.neighborRouterIDs.contains(router.routerId) && router.state) {
                distance = 1;
                gatewayID = router.routerId;
            }
            else {
                distance = Constants.INFINITY;
                gatewayID = -1;
            }
            RoutingTableEntry entry = new RoutingTableEntry(router.routerId, distance, gatewayID);
            routingTable.add(entry);
        }
        /*System.out.println("Router: "+this.routerId);
        for(RoutingTableEntry entry: routingTable) {
            System.out.println("   "+entry.getRouterId()+"  "+entry.getDistance()+"    "+entry.getGatewayRouterId());
        }*/

    }

    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable() {
        routingTable.clear();
    }

    /**
     * Update the routing table for this router using the entries of Router neighbor
     * @param neighbor
     */
    public boolean updateRoutingTable(Router neighbor) {
        boolean isChanged = false;
        double baseDistance = this.getRTEntry(neighbor.getRouterId()).getDistance();
        for (RoutingTableEntry entry : this.routingTable) {
            RoutingTableEntry neighbourEntry = neighbor.getRTEntry(entry.getRouterId());
            if (neighbourEntry != null) {
                if (this.routerId == entry.getRouterId()) continue;
                double distPrev = entry.getDistance();
                double distNew = baseDistance + neighbourEntry.getDistance();
                if (distPrev > distNew) {
                    entry.setDistance(distNew);
                    entry.setGatewayRouterId(neighbor.routerId);
                    isChanged = true;
                }
            }
        }
        return isChanged;
    }

    public RoutingTableEntry getRTEntry(int routerId) {
        for (RoutingTableEntry entry: this.routingTable) {
            if (entry.getRouterId()==routerId) return entry;
        }
        return null;
    }

    public boolean sfupdateRoutingTable(Router neighbor) {
        // x = this
        // y = entry
        // z = neighbor
        boolean isChanged = false;
        double baseDistance = this.getRTEntry(neighbor.getRouterId()).getDistance();
        for (RoutingTableEntry entry : this.routingTable) {
            RoutingTableEntry neighbourEntry = neighbor.getRTEntry(entry.getRouterId());
            if (neighbourEntry != null) {
                if (this.routerId == entry.getRouterId()) continue;
                double distPrev = entry.getDistance();
                double distNew = baseDistance + neighbourEntry.getDistance();
                int nextHopXY = entry.getGatewayRouterId();
                int nextHopZY = neighbourEntry.getGatewayRouterId();
                if ((distPrev > distNew && nextHopZY!=this.routerId)) {   ///TODO: How to do force update?
                    entry.setDistance(distNew);
                    entry.setGatewayRouterId(neighbor.routerId);
                    isChanged = true;
                }
            }
        }
        return isChanged;
    }

    /**
     * If the state was up, down it; if state was down, up it
     */
    public void revertState() {
        state = !state;
        if(state) { initiateRoutingTable(); }
        else { clearRoutingTable(); }
    }

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddresses() {
        return interfaceAddresses;
    }

    public void setInterfaceAddresses(ArrayList<IPAddress> interfaceAddresses) {
        this.interfaceAddresses = interfaceAddresses;
        numberOfInterfaces = interfaceAddresses.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIDs() {
        return neighborRouterIDs;
    }

    public void setNeighborRouterIDs(ArrayList<Integer> neighborRouterIDs) { this.neighborRouterIDs = neighborRouterIDs; }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Map<Integer, IPAddress> getGatewayIDtoIP() { return gatewayIDtoIP; }

    public void printRoutingTable() {
        System.out.println("Router " + routerId);
        System.out.println("DestID Distance Nexthop");
        for (RoutingTableEntry routingTableEntry : routingTable) {
            System.out.println(routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId());
        }
        System.out.println("-----------------------");
    }
    public String strRoutingTable() {
        String string = "Router" + routerId + "\n";
        string += "DestID Distance Nexthop\n";
        for (RoutingTableEntry routingTableEntry : routingTable) {
            string += routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId() + "\n";
        }

        string += "-----------------------\n";
        return string;
    }

}
