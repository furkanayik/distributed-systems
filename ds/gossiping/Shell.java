package ds.gossiping;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

class Shell implements Runnable {
  Peer hostPeer;
  Scanner scanner;

  public Shell(Peer hostPeer) throws Exception {
    this.hostPeer = hostPeer;
    this.scanner = new Scanner(System.in);
  }

  public static void registerIp(String ip, Peer peer) {
    peer.ipAddresses.add(ip);
  }

  @Override
  public void run() {
    try {
      System.out.println("Peer shell has initialized...");
      System.out.println("Please give a register command with the format: 'register <IP>");
      while (true) {
        System.out.print("$ ");
        String[] input = this.scanner.nextLine().split(" ");
        String command = input[0];

        if (input.length != 2 || !command.equals("register")) {
          System.out.println("invalid command! Please give a register command with the format: 'register <IP>'");
        } else {
          String targetIp = input[1];

          registerIp(targetIp, this.hostPeer);

          Socket socket = new Socket(InetAddress.getByName(targetIp), GlobalVar.PORT);

          PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

          out.println(this.hostPeer.host + " " + this.hostPeer.port);
          out.flush();

          socket.close();

        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
