package ds.gossiping;

import java.util.Scanner;

public class Gossiping {
  Scanner scanner;

  public static void main(String[] args) {
    String host = args[0];

    try {
      Peer peer = new Peer(host);

      new Thread(peer).start();
      new Thread(new PoissonProcess(peer)).start();
      new Thread(new Shell(peer)).start();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
