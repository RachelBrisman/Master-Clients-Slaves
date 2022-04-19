import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ClientReadFromMasterThread extends Thread {

	Socket client;
	private ArrayList<Job> toSend;//array list of jobs
	private Object toSendLock;//lock object
	private int clientID;//client id

	//constructor
	public ClientReadFromMasterThread(Socket socket, ArrayList<Job> toSend, Object toSendLock, int cID) {
		this.client=socket;
		this.toSend=toSend;
		this.toSendLock=toSendLock;
		this.clientID=cID;
	}

	@Override
	public void run() {
		try(
			BufferedReader in=new BufferedReader(new InputStreamReader(client.getInputStream()));
			)
		{
			//read id's of jobs that are done, switch status to finished, print that done
			int id;
			Job finished;
			String read;

			loop:
			while(true)
			{
				while ((read = in.readLine()) != null)
				{
					id = Integer.parseInt(read);//id of job
					if (id == -1)
					{
						break loop;
					}
					synchronized (toSendLock)
					{
						finished = toSend.get(id);//this job is finished
					}
					if(finished.getClientFrom() == clientID)
					{
						System.out.println("Received Completed: " + finished);
					}
				}
			}

			System.out.println("All jobs have been completed. Thanks for using.");


		}
		catch(UnknownHostException e) 
		{
		   System.err.println("Don't know about host ");
		   System.exit(1);
		} catch (IOException e) 
		{
			System.err.println("Couldn't get I/O for the connection to server on reading");
			System.exit(1);
		}
	}//end of run method
}
