package ds.multicast;

import java.util.Random;

class PoissonProcess implements Runnable {
  private static final double POISSON_PROCESS_RATE = 1;
  private static final Random RANDOM = new Random();

  private double timeForNextEvent;
  private Peer hostPeer;

  public PoissonProcess(Peer hostPeer) {
    //gets the Peer instance
    this.hostPeer = hostPeer;
  }

  private static double poissonProcessTimeForNextEvent() {
    //Creates poisson process with the rate parameter. Returns miliseconds to use in Thread.sleep()
    double lambda = 1 / POISSON_PROCESS_RATE;
    double timeInSeconds = -Math.log(1.0 - RANDOM.nextDouble()) / lambda;
    return Math.round(timeInSeconds * 1000); // return miliseconds
  }

  public static String generateMessage(String messageType, int timestamp) {
    //generate a message with the type "ack" or "message" with the timestamp
    return messageType + ":" + timestamp;
  }

  @Override
  public void run() {
    try {
      System.out.println("\nWaiting for all machines to be up...");

      //wait until all given machines are up
      this.hostPeer.waitUntilAllMachinesAreAvailable();

      System.out.println("\nPoisson Process has initialized...");
      while (true) {
        this.timeForNextEvent = poissonProcessTimeForNextEvent();

        //sleep until the next event time generated
        Thread.sleep(Math.round(this.timeForNextEvent));

        //increase the timestamp
        hostPeer.timestamp++;
        String message = generateMessage("message", hostPeer.timestamp);

        //send the message to all machines
        hostPeer.sendMessageToAllMachines(message);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
