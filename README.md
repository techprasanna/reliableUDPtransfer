# reliableUDPtransfer
This Java application provides reliable data transfer using UDP

# 1. The protocol uses a two way handshake as only the client is sending data to the server
# and so two way handshake suffices for this protocol. In TCP protocol, a 3 way
# handshake is used which causes an overhead.
2. In TCP, multiple acknowledgments cause the traffic to increase in the network leading to
congestion. In this protocol. In this protocol, the acknowledements are sent only once
and hence congestion is avoided.
3. The reliable protocol designed in this project makes use of HashMap as buffer to store
the data packets in case retransmission is needed. On the sending side, whenver a
packet is sent, it is stored in the hashmap with the sequence number of the packet as
the key. This entry is deleted when the acknowledgment for this data packet arrives from
the sender side and in this way, the hashmap is maintained. In case a bad ack comes
from the receiver side, the sender searches for the sequence number for which a bad
ack came in O(1) time and retransmits the data stored as a value for that sequence
number.
Working of the protocol:
1. From the client side (sender side), intially a SYN segment is created in which SYN bit is
set to 1, this bit is sent to the server side. If the server responds with an ack, a
handshake is successfully implemented and only after this, the data sending can be
done. If the handshake is not successfully done, it returns a false value and exits the
program.
2. After handshake is successfully done, the sender starts to send the data from the file
mentioned in the command line arguments using a stream. While reading the data from
the stream, it stores the data of 1000 bytes each in an byte array and passes this array
to the segment method where this data is encapsulated into a segment and sent to the
receiver along the sequence number.
3. After receiving the data from the sender side, the receiver checks if the packet is recived
in order or not. If not, there is a previous packet which has not been delivered yet.
Hence, the server, sends a BAD_ACK stating the sequence number missing on the
server side and asks for retransmission of the packet.
4. After receiving a BAD_ACK from the receiver, the sender calls the
retransmission_needed method to resend the packet that is dropped previously.
