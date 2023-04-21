import java.net.*;
import java.io.*;

public class Client {
	public static class Calculator {
		public static double calculate(String formula) {
			System.out.println("We Got A FORMULA "+ formula);
			String sanetizedFormula = formula.replaceAll(">|=|<", "");
			System.out.println("SanititizedString: "+ sanetizedFormula);
			// WRITE CODE HERE
			return 0; // UPDATE RETURN VALUE
		}
	}

	public static class Packetizer {
		// DEFINE ANY MEMBER VARIABLES
		// WRITE CODE HERE
		private String packetizerState;

		public Packetizer() {
			// INITIALIZE ANY MEMBER VARIABLES AS NEEDED
			// WRITE CODE HERE
			packetizerState = "";
		}

		public String packetize(String message) {
			String newMessage = packetizerState + message;
			char[] inputArray = newMessage.toCharArray();
			String formula = null;
			int state = 0;
			// char[] validNumericDigits = new char[] { '0', '1', '2', '3', '4', '5', '6',
			// '7', '8', '9' };
			String validNumericDigits = "0123456789";
			String validOperations = "+/-*";

			for (char input : inputArray) {
				switch (state) {
					case 0:// Beginning State
						if (input == '>') {
							this.packetizerState = ">";
							state = 1;
						}
						break;
					case 1://Packet Start - Start Receiving Numbers
						boolean isNumber_state1 = validNumericDigits.indexOf(input) != -1;
						if (isNumber_state1) {
							packetizerState += input;
							state = 2;
						} else {
							packetizerState = "";
							state = 6;
						}
						break;
					case 2://Number - Start Receiving Numbers, Operation, Or Equal
						boolean isNumber_state2 = validNumericDigits.indexOf(input) != -1;
						boolean isOperation_state2 = validOperations.indexOf(input) != -1;
						if (isNumber_state2) {
							packetizerState += input;
						} else if (isOperation_state2) {
							packetizerState += input;
							state = 3;
						} else if (input == '=') {
							packetizerState += input;
							state = 4;
						} else {
							packetizerState = "";
							state = 6;
						}
						break;
					case 3://Operation Received - Start Receiving Numbers
						boolean isNumber_state3 = validNumericDigits.indexOf(input) != -1;
						if(isNumber_state3){
							packetizerState+=input;
							state = 2;
						}
						else{
							packetizerState = "";
							state = 6;
						}
						break;
					case 4://Equal Recived, Start Receiving End Packet
						boolean isEndPacket_state4 = input == '<';
						if(isEndPacket_state4){
							packetizerState+=input;
							state = 5;
						}else{
							packetizerState = "";
							state = 6;
						}
						break;
					case 5://End packet received, Start Receiving Start Packet For New Formula
						boolean isStartPacket_state5 = input == '>';
						if(isStartPacket_state5){
							formula = packetizerState;
							packetizerState = "";
							state = 1;
						}
						break;
					case 6:// Error State, Start Trying To Receive Start Packet or 
						packetizerState="";
						boolean isStartPacket_state6 = input == '>';
						if(isStartPacket_state6){
							packetizerState += '>';
							state = 1;
						}else{
							state = 0;
						}
						break;
					default:
						return null;
				}
			}
			if(state == 5){
				formula = packetizerState;
				packetizerState = "";
			}
			// WRITE CODE HERE
			return formula; // UPDATE RETURN VALUE
		}
	}

	// DO NOT MODIFY ANY CODE BELOW
	public static void main(String[] args) {
		if (args.length < 2)
			return;

		String hostname = args[0];
		int port = Integer.parseInt(args[1]);

		Packetizer packetizer = new Packetizer();
		String formula = null;
		double result = 0;

		try (Socket socket = new Socket(hostname, port)) {

			while (true) {
				InputStream input = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				String message = reader.readLine();
				System.out.println("Message Received: " + message);

				formula = packetizer.packetize(message);
				if (formula != null) {
					result = Calculator.calculate(formula);
					System.out.println("====================================");
					System.out.println("Formula: " + formula);
					System.out.println("Result : " + result);
					System.out.println("====================================");
				}
			}

		} catch (UnknownHostException ex) {
			System.out.println("Server not found: " + ex.getMessage());

		} catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		}
	}
}
