package ds.tokenring;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

class Shell implements Runnable {
  String peer;
  int port;
  Scanner scanner;
  List<String> commands = Arrays.asList("lock", "unlock", "exit");

  public Shell(String host) throws Exception {
    this.peer = host;
    this.port = GlobalVar.PORT;
    this.scanner = new Scanner(System.in);
  }

  @Override
  public void run() {
    try {
      while (true) {
        String command = this.scanner.nextLine();
        if (!this.commands.contains(command)) {
          System.out.println("invalid command! Please give one of the commands 'lock', 'unlock' or 'exit'");
        } else {
          Socket socket = new Socket(InetAddress.getByName(this.peer), this.port);

          PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

          out.println(command);
          out.flush();

          socket.close();
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
