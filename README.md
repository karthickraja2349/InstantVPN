### Instant VPN

# VPN Server-Client Setup

![connection](https://github.com/user-attachments/assets/06ac9ee7-54fc-4e30-aca2-019cba47ce88)


This project demonstrates a basic VPN (Virtual Private Network) setup between a **Linux-based server** and an **Android client**, allowing secure communication over a private tunnel.

## 🔧 Features

- Secure VPN tunnel using `tun` interface.
- NAT configuration with `iptables` for internet access.
- IP forwarding enabled on the server.
- Dynamic routing for client-server communication.
- Android app acts as a VPN client connecting to the server.

---

## 🖥️ Server Side (Linux)

There are 7 layers in OSI model

Physical Layer
DataLink Layer      
Network Layer
Transport Layer
Session Layer
Presentation Layer
Application Layer

we use TUNTAP Interface for configuring VPN .

Tap - deals with layer 2 
Tun - deals with layer 3 (network)

so , Here we Use Tun for Configuring VPN.

### PreRequisite

(i) - connect with a internet(via mobile hotspot or anything)
(ii) - verify your ip address of the linux machine by 
              "ip addr show"  or "ip a"
(iii) - Ensure Your network interface was eth0 or wlp2s0 or anything by the (ii)

### Configuration Steps

1. **Create a `tun0` interface**:  (tun is already present / configured in the kernal so don't worry about it)

       sudo ip tuntap add dev tun0 mode tun
       sudo ip addr add 10.8.0.1/24 dev tun0
       sudo ip link set dev tun0 up
   
2.  Enable IP forwarding:

        sudo sysctl -w net.ipv4.ip_forward=1
    
   (w means write)

4.  Set up iptables rules:

        sudo iptables -t nat -A POSTROUTING -o wlp2s0 -j MASQUERADE
(iptables maintain the NAT configuration )
               =>This sets up NAT (masquerading).
               =>All packets going out of your internet-facing interface wlp2s0 (like Wi-Fi) will have their source IP changed to your Server’s IP.
               =>'t' means 'table' . There are lot of tables .(we need only NAT)
                       Common tables:
                           filter (default): For allowing/blocking traffic.
                            nat: For address translation (e.g., VPN, internet sharing).
                            mangle: For modifying packet headers.
                            raw: For pre-processing.
               =>'A' means 'Append' .Add this rule to the end of the specified chain
    
        sudo iptables -A FORWARD -i tun0 -o wlp2s0 -j ACCEPT
    
  =>It allows forwarding of packets from tun0 to wlp2s0.
              => It Let packets from the VPN (tun0) go out through the internet (wlp2s0).
              => 'j' - 'Jump' (Action).Tells what to do with the packet.
                      -> j ACCEPT means allow the packet.
                      Other values:
                           ACCEPT: Let the packet pass.
                           DROP: Silently block it.
                           REJECT: Block and send back error.
                           MASQUERADE: NAT rule that hides source IP.
                           LOG: Log the packet.
               =>'i' - 'Input interface' . Interface the packet is coming in on.
                         -i tun0 means packets entering from the VPN tunnel.          
               =>'o' - 'Output interface' . Interface the packet is going out through.
                          -o wlp2s0 means the packet is leaving via your Wi-Fi interface.
    
      sudo iptables -A FORWARD -i wlp2s0 -o tun0 -m state --state RELATED,ESTABLISHED -j ACCEPT

   => It allows return traffic to come back from the internet to the VPN client.
              =>It checks if the packet is part of a connection that was already allowed (like a reply from a website).
              =>If traffic is coming from the internet (wlp2s0) and is part of a connection that started from the VPN (tun0), allow it.
  Server side finish. 
  To enable the tun0 . we need a c code (JAVA is enough but externally we need to configure the kernal) so choose c (it was in the file)
-----------------------------------------------------------------------------------------------------------------------------------------------------------------
### Client Side (Android)

Uses Android VPNService API to create a VPN tunnel.
Connects to the Linux server via the tun0 interface.
Routes traffic securely through the VPN tunnel.

### Network Information
Server VPN IP: 10.8.0.1
Client VPN IP: Assigned dynamically (e.g., 10.8.0.2)
Tunnel Interface: tun0
Server Public Interface: e.g., wlp2s0

### Testing
Ensure server and client are on the same network or connected via the internet.
Use ping, traceroute, or app-based logs to verify tunnel functionality.

---------------------------------------------------------------------------------------------------------------------------------------------------------------

Author
Karthick Raja K
Email: karthickRaja182356@gmail.com

