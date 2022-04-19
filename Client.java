import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    //this class will ask the server to perform specific tasks
    public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);//create scanner object
		System.out.println("What port would you like to run on?");//user input
		int port = scanner.nextInt();//port number

		Socket clientSocket = new Socket("127.0.0.1", port);//create socket object
		ArrayList<Job> toSend = new ArrayList<Job>();//create array list of jobs to send
		Object toSendLock = new Object();//dummy object to lock array that will hold jobs to send
		SharedNum jobID= new SharedNum(0);//creates a sharedNum object for jobID so can be shared memory and accessed by other threads
		Object idLock = new Object(); //dummy lock object for id
		int clientID = (int)(Math.random() * 100);//generate random # for client ID

		//creates new thread that has the client read from the user and passes a few parameters
		Thread userIn=new ClientReadFromUser(clientSocket, toSend, toSendLock, jobID, idLock, clientID);

		userIn.start();//start thread

		try {
			userIn.join();
		}catch (Exception e){
			System.out.println("Join Failed cwm");
		}

    }
}
