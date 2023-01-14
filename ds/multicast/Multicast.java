package ds.multicast;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Multicast {
  Scanner scanner;

  public static void main(String[] args) {
    String host = args[0];

    //boolean parameter to decide if peer is producing messages or just consuming
    Boolean isProduceMessages = false;

    isProduceMessages = Boolean.parseBoolean(args[1]);

    List<String> ipAdresses = new ArrayList<>();

    for (int i = 2; i < args.length; i++) {
      ipAdresses.add(args[i]);
    }

    try {
      Peer peer = new Peer(host, isProduceMessages, ipAdresses);

      new Thread(peer).start();

      if (isProduceMessages) {
        //if message producer, initialize the poisson process
        new Thread(new PoissonProcess(peer)).start();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
