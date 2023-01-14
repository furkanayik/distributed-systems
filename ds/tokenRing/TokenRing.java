package ds.tokenring;

public class TokenRing {

  public static void main(String[] args) {
    String host = args[0];
    String nextPeerIp = args[1];

    boolean isInitiator = args[2].equals("init");

    try {
      new Thread(
          new Peer(host, nextPeerIp, isInitiator))
          .start();

      new Thread(
          new Shell(host))
          .start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
