package ds.multicast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Peer implements Runnable {
  public Set<String> ipAddresses;
  String host;
  int port;
  int timestamp;
  MessageQueue messageQueue;
  ServerSocket server;

  public Peer(String host, Boolean isProduceMessages, List<String> ipAddresses)
      throws Exception {
    this.host = host;
    this.port = GlobalVar.PORT;
    this.timestamp = 0;

    this.ipAddresses = new HashSet<>();
    this.ipAddresses.addAll(ipAddresses);

    this.messageQueue = new MessageQueue(this);

    server = new ServerSocket(port, 10, InetAddress.getByName(host));
  }

  public void waitUntilAllMachinesAreAvailable() {
    //this function is used initially to wait until all machines are up
    boolean isThereMachineDown = false;
    try {
      while (true) {
        for (String ip : this.ipAddresses) {
          Thread.sleep(500);
          try {
            Socket socket = new Socket(InetAddress.getByName(ip), GlobalVar.PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(GlobalVar.CONNECT_TEST_MESSAGE);
            out.flush();

            socket.close();
          } catch (Exception e) {
            isThereMachineDown = true;
            break;
          }
        }

        if (!isThereMachineDown) {
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void sendMessageToAllMachines(String message) {
    for (String ip : this.ipAddresses) {
      this.sendMessage(ip, message);
    }
  }

  public void sendMessage(String ip, String message) {
    try {
      Socket socket = new Socket(InetAddress.getByName(ip), GlobalVar.PORT);

      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      out.println(this.host + ":" + message);
      out.flush();

      socket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    try {
      while (true) {
        try {
          Socket senderSocket = this.server.accept();

          BufferedReader in = new BufferedReader(new InputStreamReader(senderSocket.getInputStream()));
          String[] receivedMessage = in.readLine().split(":");

          if (receivedMessage[0].equals(GlobalVar.CONNECT_TEST_MESSAGE)) {
            //if it is the connection check, ignore the message
            in.close();
            senderSocket.close();
          } else {
            //actual process
            in.close();
            senderSocket.close();

            int messageTimestamp = Integer.parseInt(receivedMessage[2]);

            if (messageTimestamp > this.timestamp) {
              this.timestamp = messageTimestamp;
            }

            this.messageQueue
                .addMessageToQueue(new EventMessage(receivedMessage[1], messageTimestamp, receivedMessage[0]));

            if (receivedMessage[1].equals("message")) {
              //if received message is a "message", send acknoledgments to all machines
              String ackMessage = PoissonProcess.generateMessage("ack", this.timestamp);
              this.sendMessageToAllMachines(ackMessage);
            }
          }

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
