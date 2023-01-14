package ds.multicast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

class MessageQueue {
  //Creates a queue of EventMessages

  private PriorityQueue<EventMessage> messageQueue;
  private Map<String, Integer> messageCounter;
  private Set<String> ipAdresses;
  Peer hostPeer;

  public MessageQueue(Peer hostPeer) {
    this.messageQueue = new PriorityQueue<>();
    this.messageCounter = new HashMap<>();
    this.hostPeer = hostPeer;
    this.ipAdresses = new HashSet<>(hostPeer.ipAddresses);
  }

  public void addMessageToQueue(EventMessage message) {
    //everytime adding a new EventMessage to the queue, it orders them with the timestamp

    this.messageQueue.add(message);

    //update the messageCounter
    //message counter keeps the ip addresses with number of messages they have in the queue
    this.messageCounter.put(message.ip, messageCounter.getOrDefault(message.ip, 0) + 1);

    //if we have all the ip addresses in the queue, poll (remove the head of the queue update the counter)
    if (this.ipAdresses.stream().allMatch(ipAddress -> this.messageCounter.containsKey(ipAddress))) {
      EventMessage polledMessage = this.messageQueue.poll();
      this.messageCounter.put(polledMessage.ip,
          messageCounter.getOrDefault(polledMessage.ip, 1) - 1);

      //if polled message is a "message" but not "ack" increase the timestamp and "consume" the message
      if (polledMessage.message.equals("message")) {
        this.hostPeer.timestamp++;
        System.out.println(polledMessage.ip + ": " + polledMessage.message + ": " + polledMessage.timestamp);
      }
    }
  }
}
