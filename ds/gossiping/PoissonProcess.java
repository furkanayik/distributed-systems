package ds.gossiping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

class PoissonProcess implements Runnable {
  private static final int WORD_COUNT = 37100;
  private static final double POISSON_PROCESS_RATE = 30;
  private static final String FILE_LOCATION = "ds/assignment/gossiping/words/english_words_alpha.txt";
  private static final Random RANDOM = new Random();

  private double timeForNextEvent;
  private static String generatedWord;
  private Peer hostPeer;

  public PoissonProcess(Peer hostPeer) {
    this.hostPeer = hostPeer;
  }

  private static void setGeneratedWord(String word) {
    generatedWord = word;
  }

  private static String getGeneratedWord() {
    return generatedWord;
  }

  private static double poissonProcessTimeForNextEvent() {
    //Creates poisson process with the rate parameter. Returns miliseconds to use in Thread.sleep()
    double lambda = 1 / POISSON_PROCESS_RATE;
    double timeInSeconds = -Math.log(1.0 - RANDOM.nextDouble()) / lambda;
    return Math.round(timeInSeconds * 1000); // return miliseconds
  }

  private static String pickWord() {
    //pick a random word from the file
    int lineNumber = RANDOM.nextInt(WORD_COUNT);
    String line = null;

    try {
      BufferedReader reader = new BufferedReader(new FileReader(FILE_LOCATION));
      for (int i = 0; i < lineNumber; i++) {
        reader.readLine();
      }
      line = reader.readLine();
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return line;
  }

  private static void generateRandomWord(double nextEventTime) {
    //generate a word when its the time for the nextEvent
    try {
      Thread.sleep(Math.round(nextEventTime));
      String word = pickWord();
      setGeneratedWord(word);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    try {
      System.out.println("\nPoisson Process has initialized...");
      while (true) {
        this.timeForNextEvent = poissonProcessTimeForNextEvent();
        System.out.println("\nNext word generation in " + this.timeForNextEvent / 1000 + " seconds...");
        generateRandomWord(timeForNextEvent);
        String word = getGeneratedWord();

        System.out.println("The word '" + word + "'' is generated...");
        System.out.print("$ ");

        hostPeer.WORDS.add(word);
        if (hostPeer.ipAddresses.size() < 1) {
          System.out.println("\nWaiting for a peer connection...");
        }
        while (hostPeer.ipAddresses.size() < 1) {
          Thread.sleep(1000);
        }

        //gossip it to one of the neighbour machines
        hostPeer.gossip(word);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
