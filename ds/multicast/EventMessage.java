package ds.multicast;

class EventMessage implements Comparable<EventMessage> {
  //Single EventMessage, implements Comparable to keep them in order in the queue

  String ip;
  int timestamp;
  String message;

  public EventMessage(String message, int timestamp, String ip) {
    this.ip = ip;
    this.timestamp = timestamp;
    this.message = message;
  }

  @Override
  public int compareTo(EventMessage other) {
    // Compare the timestamp of this message with the timestamp of the other message
    return Integer.compare(this.timestamp, other.timestamp);
  }
}
