/*
 * Server.java
 *
 */


import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;


/*
* This program implements a reliable data transfer which is different than TCP.
* This program relies on UDP data sending along with some methods to ensure
* that the data is reached successfully to the receiver and if it doesn't
* retransmission is done. This program i.e Server.java receives the data
* from the client (Client.java), checks the ordering of the data and sends
* acknowledgments  according to the packet received.
* 
* @author      Prasanna Mahesh Bhope
*/

public class Server {
	
	public static ArrayList<Integer> seqList = new ArrayList<>();
	public static HashMap<Integer,byte[]> client_file = new HashMap<>();
	public static DatagramSocket socket;
	
	
/*
 * This method sends an acknowledgment of the received in order packet.
 */
	
	public static void sendACK(DatagramSocket socket,int port,InetAddress sourceIP,int ACK) throws IOException {
		String send = ACK + " " + "null";
		byte[] send_byte = send.getBytes();
		DatagramPacket server_send = new DatagramPacket(send_byte, send_byte.length,sourceIP,port);
		socket.send(server_send);
		
	}
	
/*
* This method sends a bad acknowledgment of the received out of order packet.
*/	
	public static void BAD_ACK(DatagramSocket socket,int port,InetAddress sourceIP,int ACK) throws IOException {
		String send_String = ACK + " " + "BAD";
		byte[] send = send_String.getBytes();
		DatagramPacket server_send = new DatagramPacket(send, send.length,sourceIP,port);
		socket.send(server_send);
		
	}
	
	public static boolean Checksum(byte[] info) {
		
		return true;
	}
	
	/*
	 * This method stores the data in a hashmap and whenever a packet comes, 
	 * checks if the packet is present in hashmap.If it is present,
	 * it ignores, otherwise stores the packet along with the sequence number as key.
	 */
	
	public static void StoreData(String data, int sequenceNo, int port, InetAddress sourceIP,FileOutputStream fio) throws IOException {
		
		
		byte[] byte_data = data.getBytes();
		int i = seqList.get(seqList.size()-1);
		if(sequenceNo - i == 1) {
			System.out.println("Packets in order. Sequence number:" +sequenceNo);
			if(Checksum(byte_data)) {
				seqList.add(sequenceNo);
				if(client_file.containsKey(sequenceNo)) {
					return;
				}
				client_file.put(sequenceNo, byte_data);
				fio.write(byte_data);
				sendACK(socket, port, sourceIP, sequenceNo+1);
					
			}
		}
		else {
				System.out.println("Packet not in order: Retransmission in process");
				BAD_ACK(socket, port, sourceIP, sequenceNo);
			}
		}
		
	/*
	   * The main program.
	   *
	   * @param    args    command line arguments provided (none)
	   */	
		
	public static void main(String[] args) throws IOException {
		socket = new DatagramSocket(9999);
		
		//Please enter the destination of output file with path.
		System.out.println("Server started: Ready to accept:\n");
		FileOutputStream fio = new FileOutputStream("output.txt");			
		
		while(true) {
		
		byte[] receive = new byte[1024];
		
		DatagramPacket server_receive = new DatagramPacket(receive, receive.length);
		socket.receive(server_receive);
		
		
		String client_data = new String(server_receive.getData());
		String[] receive_array = client_data.split(" ");
		
		int client_sqno = Integer.parseInt(receive_array[1].trim());
		int syn_bit = Integer.parseInt(receive_array[0].trim());
		
		int port = server_receive.getPort();
		InetAddress sourceIP = server_receive.getAddress();
		
		if(receive_array[2].trim().equalsIgnoreCase("NULL") && syn_bit == 1) {
			seqList.add(client_sqno);
			sendACK(socket,port,sourceIP,client_sqno+1);
		}
		
		else if(receive_array[2].trim().equalsIgnoreCase("exit")) {
			System.out.println("Connection is now closing");	
			break;
		}
		
		
		else {
				StoreData(client_data,client_sqno,port,sourceIP,fio);
		}
		
		}
		
		socket.close();
		fio.close();
	}

}
