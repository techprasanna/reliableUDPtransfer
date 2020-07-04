/*
 * Client.java
 */


import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;


/*
 * This program implements a reliable data transfer which is different than TCP.
 * This program relies on UDP data sending along with some methods to ensure
 * that the data is reached successfully to the receiver and if it doesn't
 * retransmission is done.
 * 
 * @author      Prasanna Mahesh Bhope
 */


public class Client {
	
	public static final int LIMIT = 1000;
	public static int sequence_number = 0;
	public static final int port = 9999;
	public static HashMap<Integer,byte[]> buffer = new HashMap<>();
	
	/*
	   * This method provides the encapsulation of data into a segment which 
	   * consists of sequence numbers and SYN bit along with data in byte array
	   *
	   * @param    synBit    To check if segment is for handshake or normal data sending
	   * @param	   sequence_number	The default sequence number centrally managed by the program
	   * @param    info		 Information to send in byte array	
	   */
	
	public static byte[] setSegment(int synBit,int sequence_number,byte[] info) {
		String check = synBit + " " +sequence_number + " " + new String(info);
		byte[] result = check.getBytes();
		return result;
		
	}
	
	/*
	   * This method provides the handshake of the connection. It returns true if successfull else 
	   * false.
	   */
	
	
	public static boolean Set_connection(DatagramSocket socket,InetAddress ipaddr, int port) throws IOException {
		
		String no_data = "null";
		byte[] no_data1 = no_data.getBytes();
		byte[] check1 = setSegment(1,sequence_number,no_data1);
		DatagramPacket send_packet = new DatagramPacket(check1, check1.length,ipaddr,port);
		try {
			socket.send(send_packet);
		} catch (IOException e) {
			System.err.println("Host not found, please check IP or port number");;
		}
		if(receive_Data(socket,ipaddr) == sequence_number+1) {
			System.out.println("Connection successful! Sending data to server:\n");
			sequence_number = sequence_number + 1;
			return true;
		}
		return false;
			
	}
	
	/*
	   * This method provides the retransmission of data packets if 
	   * the receiver provide a bad acknowledgement.
	   */
	
	public static void Retransmission_needed(DatagramSocket socket, InetAddress ipaddr, int send_number) throws IOException {
		
		byte[] resending = setSegment(0, send_number, buffer.get(send_number));
		DatagramPacket packet = new DatagramPacket(resending,resending.length,ipaddr,port);
		socket.send(packet);
		
	}
	
	/*
	   * This method receives data from the receiver side. Particularly 
	   * acknowledgments (bad or normal). It returns the acknowledgement number
	   * received from the receiver or directs to bad acknowledgement method for
	   * retransmission.
	   *
	*/
	
	public static int receive_Data(DatagramSocket socket,InetAddress ipaddr) throws IOException {
		
		byte[] receive = new byte[1024];
		DatagramPacket receive_packet = new DatagramPacket(receive,receive.length);
		socket.receive(receive_packet);
		
		String receive_data = new String(receive_packet.getData());
		String[] receive_array = receive_data.split(" ");
		System.out.println("Data received: Acknowledgement for next sequence number: "+receive_array[0].trim());
		
		if(receive_array[1].trim().equalsIgnoreCase("BAD")) {
			Retransmission_needed(socket,ipaddr,(sequence_number-1));
		}
		else {
			buffer.remove(sequence_number-1);
		}
		
		return Integer.parseInt(receive_array[0].trim());
		
	}
	
	/*
	   * This method starts sending data once handshake is successful.
	   *
	*/

	public static void Start_Sending_Data(DatagramSocket socket, InetAddress ipaddr, int port, byte[] info) throws IOException {
		byte[] send = setSegment(0, sequence_number, info);
		DatagramPacket send_data = new DatagramPacket(send,send.length,ipaddr,port); 
		try {
			socket.send(send_data);
			buffer.put(sequence_number, info);
			sequence_number++;
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		receive_Data(socket,ipaddr);
		
		
	}
	
	/*
	   * This method reads the file and divides the file into chunks of data to be sent
	   * as it is reading the file.
	   *
	*/
	
	public static void ReadFile(DatagramSocket socket,InetAddress ipaddr, String filename) throws IOException {
		FileInputStream fin = new FileInputStream(filename);
		while(true) {
			byte[] content = new byte[LIMIT];
			if (fin.read() == -1){
				String exit = "exit";
				byte[] exit_byte = setSegment(1, sequence_number, exit.getBytes());
				DatagramPacket packet = new DatagramPacket(exit_byte,exit_byte.length,ipaddr,port);
				socket.send(packet);
				break;
			}
			for(int index = 0; index < 1000; index++) {
				content[index] = (byte) fin.read();
			
			}
			Start_Sending_Data(socket, ipaddr, port, content);
		}
		fin.close();
		
		
		
	}
	
	/*
	   * The main program.
	   *
	   * @param    args    command line arguments provided with IP address of the destination
	   * 					and filename along with path of the file from which data is to be sent.
	   */

	public static void main(String[] args) throws IOException {
		
		DatagramSocket socket = new DatagramSocket();
		String destination_addr = args[0];
		
		InetAddress ipaddr = null;
		
		ipaddr = InetAddress.getByName(destination_addr);
		if(Set_connection(socket,ipaddr, port)){
			ReadFile(socket,ipaddr,args[1]);
		}
		else {
			System.out.println("Handhake not successfull. Closing the half connection");
			socket.close();
		}
		
		socket.close();
			
		
		
		

		
	}

}
