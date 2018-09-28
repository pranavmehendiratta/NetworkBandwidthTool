import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Iperfer {

	public static void main(String[] args) {
		Iperfer ip = new Iperfer();
		ip.validateArguments(args);
	}

	public void validateArguments(final String [] args) {
		if (args.length > 7 || args.length <= 1) {
			printInvalidArgsErr();
		}
		
		// If in client mode
		if (args[0].equalsIgnoreCase("-c")) {
			if (args.length != 7) {
				printInvalidArgsErr();
			}
			
			processClient(args);

			// If server mode
		} else if (args[0].equalsIgnoreCase("-s")) {
			if (args.length != 3) {
				printInvalidArgsErr();
			}
			
			processServer(args);
			
		} else {
			printInvalidArgsErr();
		}
	}
	
	public void printInvalidArgsErr() {
		System.out.println("Error: missing or additional arguments");
	}
	
	public boolean validatePortNumber(final String portNum) {
		try {
			int num = Integer.parseInt(portNum);
			if (num < 1024  || num > 65535) {
				System.out.println("Error: port number must be in the range 1024 to 65535");
				return false;
			}
			
			return true;
		} catch (NumberFormatException e) {
			System.out.println("Error: port number must be in the range 1024 to 65535");
			return false;
		}
	}
	
	public boolean validateHostName(final String hostname) {
		return true;
	}
	
	/**
	 * Validate that time > 0
	 * @param time
	 * @return
	 */
	public boolean validateTime(final String time) {
		try {
			
			int t = Integer.parseInt(time);
			if (t <= 0) {
				System.out.println("Error: time must be an integer and greater than 0");
				return false;
			}
			
			return true;
		} catch (NumberFormatException e) {
			System.out.println("Error: time must be an integer and greater than 0");
			return false;
		}
	}
	
	public void processClient(final String [] args) {
		if (!(args[1].equals("-h") && validateHostName(args[2]) && args[3].equals("-p") 
				&& validatePortNumber(args[4]) && args[5].equals("-t") 
				&& validateTime(args[6]))) {
			printInvalidArgsErr();
			System.exit(0);
		}
		
		// Start using the arguments
		final String hostname = args[2];
		final int portNum = Integer.parseInt(args[4]);
		final int time = Integer.parseInt(args[6]);
		
		try (
			final Socket clientSocket = new Socket(hostname, portNum);
			final OutputStream outStream = clientSocket.getOutputStream();
		){
			// 1000 bytes of data to send
			final byte [] data = new byte[1000];
			int packetCount = 0;
			
			long currTime = System.currentTimeMillis();
			
			while (currTime + (time * 1000) > System.currentTimeMillis()) {
				outStream.write(data);
				packetCount++;
			}
			System.out.println("sent=" + packetCount + " KB rate=" + (((packetCount * 8) / 1000) / time) + " Mbps");
			
		} catch (IllegalArgumentException e) {
			System.out.println("Illegal argument exception");
		} catch (SecurityException e) {
			System.out.println("Security exception");
		} catch (IOException e) {
			System.out.println("IOException exception");
		}
	}
	
	public void processServer(final String [] args) {
		if (!(args[1].equals("-p") && validatePortNumber(args[2]))) {
			printInvalidArgsErr();
			System.exit(0);
		}
		
		// Start using arguments
		final int portNum = Integer.parseInt(args[2]);
		
		try (
			final ServerSocket serverSocket = new ServerSocket(portNum);
			final Socket clientSocket = serverSocket.accept();                
			InputStream inStream = clientSocket.getInputStream();
		) {
			byte [] serverInput = new byte[1000];
			
			final long startTime = System.currentTimeMillis();
			int bytes = 0;
			int totalBytes = 0;
			while((bytes = inStream.read(serverInput)) > 0) {
				totalBytes += bytes;
			}
			
			final long duration = (System.currentTimeMillis() - startTime) / 1000;
			System.out.println("received=" + (totalBytes/1000) + " KB rate=" + ((((totalBytes * 8)/1000) / 1000) / duration) + " Mbps");		
		} catch (IllegalArgumentException e) {
			System.out.println("Illegal argument exception");
		} catch (SecurityException e) {
			System.out.println("Security exception");
		} catch (IOException e) {
			System.out.println("IOException exception");
		}
	}	
}
