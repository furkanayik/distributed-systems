package ds.gossiping;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

class Peer implements Runnable {
  public final List<String> WORDS = new ArrayList<>();
  public List<String> RECEIVED_MESSAGES = new ArrayList<>();
  private static final double POISSON_POSSIBILITY_K = 5.0;

  public Set<String> ipAddresses;
  String host;
  int port;
  ServerSocket server;

  public Peer(String host) throws Exception {
    this.host = host;
    this.port = GlobalVar.PORT;

    this.ipAddresses = new HashSet<>();

    server = new ServerSocket(port, 1, InetAddress.getByName(host));
  }

  private String pickTargetPeer() {
    //pick a machine to gossip randomly
    int size = this.ipAddresses.size();
    String[] array = this.ipAddresses.toArray(new String[size]);
    int index = new Random().nextInt(size);

    return array[index];
  }

  public boolean isGossiping() {
    //when there is a repeated message, decide if keep gossiping with probability
    double k = POISSON_POSSIBILITY_K;
    double randomVal = new Random().nextDouble();
    return randomVal > (1.0 / k);
  }

  public void gossip(String gossipWord) {
    //pick a target machine and gossip the message
    String nextPeer = pickTargetPeer();
    try {
      System.out.println("Picked the peer " + nextPeer + " to gossip...");
      System.out.println("Gossiping...");
      Socket socket = new Socket(InetAddress.getByName(nextPeer), GlobalVar.PORT);

      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      out.println("message " + this.host + " " + gossipWord);
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
          String[] receivedMessage = in.readLine().split(" ");

          if (!receivedMessage[0].equals("message")) {
            //if its a register command, pair the machines
            Shell.registerIp(receivedMessage[0], this);
            System.out.println("\n" + "-> " + receivedMessage[0] + " is registered...");
            System.out.println("Updated IP Table; \n");

            for (String ip : this.ipAddresses) {
              System.out.println("\n" + ip);
            }

            System.out.print("$ ");

          } else {
            //if its a message, process the message
            String receivedWord = receivedMessage[2];
            System.out.println("\nReceived the word: " + receivedWord);

            if (!this.WORDS.contains(receivedWord)) {
              this.WORDS.add(receivedWord);
            }

            boolean isGossiping = true;
            if (this.RECEIVED_MESSAGES.contains(receivedWord)) {
              //if its a repeated word, decide if keep gossiping
              isGossiping = this.isGossiping();
            } else {
              this.RECEIVED_MESSAGES.add(receivedWord);
            }

            if (isGossiping) {
              this.gossip(receivedWord);
              System.out.println("Updated words list; ");
              for (String word : this.WORDS) {
                System.out.println(word);
              }
            } else {
              System.out.println("Repeated message. Gossiping stoped with probability...");
            }
          }

          in.close();
          senderSocket.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
