package ds.tokenring;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

class Peer implements Runnable {
  String host;
  int port;
  String nextPeer;
  ServerSocket server;
  boolean isInitiator;

  int lastToken;
  boolean isLocked = false;

  public Peer(String host, String nextPeer, boolean isInitiator) throws Exception {
    this.host = host;
    this.port = GlobalVar.PORT;
    this.nextPeer = nextPeer;
    this.isInitiator = isInitiator;

    server = new ServerSocket(port, 1, InetAddress.getByName(host));
  }

  public String readReceivedMessage() {
    try {
      Socket previousSocket = this.server.accept();

      BufferedReader in = new BufferedReader(new InputStreamReader(previousSocket.getInputStream()));
      String receivedMessage = in.readLine();

      System.out.println("\nReceived message: " + receivedMessage);
      System.out.print("$ ");

      in.close();
      previousSocket.close();
      return receivedMessage;
    } catch (Exception e) {
      e.printStackTrace();
      return "lock";
    }
  }

  public void sendToken(int token) {
    Socket nextSocket;
    try {
      while (true) {
        try {
          nextSocket = new Socket(InetAddress.getByName(this.nextPeer), port);
          break;
        } catch (Exception e) {
          Thread.sleep(1000);
          System.out.println("waiting for next Peer to be available");
          continue;
        }
      }

      PrintWriter out = new PrintWriter(nextSocket.getOutputStream(), true);

      out.println(token);
      out.flush();

      nextSocket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    try {
      while (true) {
        Thread.sleep(2000);
        if (this.isInitiator) {
          this.sendToken(0);
          this.isInitiator = false;
        } else {
          String message = this.readReceivedMessage();

          if (message.equals("lock")) {
            this.isLocked = true;
          } else if (message.equals("unlock")) {
            this.isLocked = false;
            this.sendToken(this.lastToken);
          } else if (message.equals("exit")) {
            System.out.println("BREAK!");
            break;
          } else {
            if (!this.isLocked) {
              int token = Integer.parseInt(message);
              token++;
              this.lastToken = token;
              this.sendToken(token);
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
