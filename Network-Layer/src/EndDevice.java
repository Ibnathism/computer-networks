import java.io.Serializable;

//Done!
public class EndDevice implements Serializable {

    private IPAddress ipAddress;
    private IPAddress gateway;
    private Integer deviceID;

    public EndDevice(IPAddress ipAddress, IPAddress gateway, Integer deviceID) {
        this.ipAddress = ipAddress;
        this.gateway = gateway;
        this.deviceID = deviceID;
        if (!isValid(ipAddress,gateway)) System.out.println("Not valid client");
    }

    private boolean isValid(IPAddress ip, IPAddress gateway) {
        for (int i=0; i < 3; i++) {
            if (!ip.getBytes()[i].equals(gateway.getBytes()[i])) return false;
        }
        return true;
    }

    public IPAddress getIpAddress() {
        return ipAddress;
    }

    public IPAddress getGateway() { return gateway; }

    public Integer getDeviceID() { return deviceID; }
}
