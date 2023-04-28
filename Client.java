import java.net.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.io.*;

public class Client {
	public static class Calculator {
		public static double calculate(String formula) {
			// Queues for computing the full result after we go through all the characters
			// in our formula
			Queue<Double> numberQueue = new LinkedList<Double>();
			Queue<Character> operatorQueue = new LinkedList<>();

			// Stacks for handling all numbers and results for multiplication and division.
			Stack<Double> numberStack = new Stack<>();
			// Stores our operations. + - / *
			// If we get a + or - we put them into operatorQueue
			Stack<Character> operationStack = new Stack<>();

			String currentNumber = "";
			for (int i = 1; i < formula.length() - 1; i++) {
				char currentCharacter = formula.charAt(i);
				boolean isOperation = currentCharacter == '-' || currentCharacter == '+' || currentCharacter == '*'
						|| currentCharacter == '/' || currentCharacter == '=';
				if (currentCharacter == '=') {
					numberQueue.add(numberStack.pop());
				}
				// Is it a number?
				if (!isOperation) {
					// If so, then we can add it currentNumber
					currentNumber += currentCharacter;
					// Look ahead in the string to see if we have an operator.
					// If so parse currentNumber and add it to the number stack
					if ("+-/*=".indexOf(formula.charAt(i + 1)) != -1) {
						numberStack.add(Double.parseDouble(currentNumber));
						currentNumber = "";
					}
				} else {
					operationStack.add(currentCharacter);
				}

				// Once we have 2 numbers in our number stack we need to determine precedence
				// If we have a * or / then we do calculation here and put that result back on that stack
				// Otherwise we send the number at the bottom of the stack to the queue
				if (numberStack.size() == 2) {
					double b = numberStack.pop();
					double a = numberStack.pop();
					char sign = operationStack.pop();
					if (sign == '/') {
						numberStack.add(a / b);
					} else if (sign == '*') {
						numberStack.add(a * b);
					} else if (sign == '-' || sign == '+') {
						// Ex: Stack [8,5] -> [5]
						// Take out 8, put into our queue with the sign and put 5 back into the stack
						numberQueue.add(a);
						operatorQueue.add(sign);
						numberStack.add(b);
					}
				}
			}
			// After all of this, we now have a formula with just addition and subtraction
			// Now we can go from left to right.
			double res = numberQueue.poll();
			while (!operatorQueue.isEmpty()) {
				char sign = operatorQueue.poll();
				if (sign == '-') {
					res -= numberQueue.poll();
				} else {
					res += numberQueue.poll();
				}
			}

			return res;
		}
	}

	public static class Packetizer {
		// Since our data comes into chunks. we'll keep track of a full packet whenever we beginning
		// reading
		private String packetizerMessageState;
		// Keep track of the last state of the machine
		private int packetizerMachineState;

		public Packetizer() {
			packetizerMessageState = "";
			packetizerMachineState = 0;
		}

		public String packetize(String message) {
			if(message.length() == 0){
				return null;
			}
			System.out.println(message.length());
			char[] inputArray = message.toCharArray();
			String formula = null;
			int state = packetizerMachineState;
			
			String validNumericDigits = "0123456789";
			String validOperations = "+/-*";

			for (char input : inputArray) {
				switch (state) {
					case 0:// Beginning State
						if (input == '>') {
							this.packetizerMessageState = ">";
							state = 1;
						}
						break;
					case 1:// Packet Start - Start Receiving Numbers
						boolean isNumber_state1 = validNumericDigits.indexOf(input) != -1;
						if (isNumber_state1) {
							packetizerMessageState += input;
							state = 2;
						} else {
							packetizerMessageState = "";
							state = 6;
						}
						break;
					case 2:// Number - Start Receiving Numbers, Operation, Or Equal
						boolean isNumber_state2 = validNumericDigits.indexOf(input) != -1;
						boolean isOperation_state2 = validOperations.indexOf(input) != -1;
						if (isNumber_state2) {
							packetizerMessageState += input;
						} else if (isOperation_state2) {
							packetizerMessageState += input;
							state = 3;
						} else if (input == '=') {
							packetizerMessageState += input;
							state = 4;
						} else {
							packetizerMessageState = "";
							state = 6;
						}
						break;
					case 3:// Operation Received - Start Receiving Numbers
						boolean isNumber_state3 = validNumericDigits.indexOf(input) != -1;
						if (isNumber_state3) {
							packetizerMessageState += input;
							state = 2;
						} else {
							packetizerMessageState = "";
							state = 6;
						}
						break;
					case 4:// Equal Recived, Start Receiving End Packet
						boolean isEndPacket_state4 = input == '<';
						if (isEndPacket_state4) {
							packetizerMessageState += input;
							state = 5;
						} else {
							packetizerMessageState = "";
							state = 6;
						}
						break;
					case 5:// End packet received, Start Receiving Start Packet For New Formula
						boolean isStartPacket_state5 = input == '>';
						// For the fringe case that after where we get a number in a new message. We shouldn't
						// be setting formula to our messageState.
						if(packetizerMessageState != ""){
							formula = packetizerMessageState;
						}
						packetizerMessageState = "";
						if (isStartPacket_state5) {
							packetizerMessageState += '>';
							state = 1;
						}else{
							state = 0;
						}
						break;
					case 6:// Error State, Start Trying To Receive Start Packet or
						packetizerMessageState = "";
						boolean isStartPacket_state6 = input == '>';
						if (isStartPacket_state6) {
							packetizerMessageState += '>';
							state = 1;
						} else {
							state = 0;
						}
						break;
					default:
						return null;
				}
			}
			if (state == 5) {
				formula = packetizerMessageState;
				packetizerMessageState = "";
			}
			packetizerMachineState = state;
			return formula;
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
