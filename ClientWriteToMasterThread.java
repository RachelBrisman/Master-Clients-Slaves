import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class ClientWriteToMasterThread extends Thread {
	Socket client;//client socket
	private ArrayList<Job> toSend;//array list of job that client needs to sedn to master
	private Object toSendLock;//lock object
	private SharedNum nextJob;//shared num object for next job
	private Object nextJobLock;//lock object for next job
	private int clientID;//client id

	//constructor
	public ClientWriteToMasterThread(Socket socket,ArrayList<Job> toSend, Object toSendLock, SharedNum nj, Object njl, int cID) {
		this.client=socket;
		this.toSend=toSend;
		this.toSendLock=toSendLock;
		this.nextJob = nj;
		this.nextJobLock=njl;
		this.clientID = cID;
	}

	@Override
	public void run() {
		try(
				// get the output stream from the socket, create printwriter
				OutputStream outputStream = client.getOutputStream();
				PrintWriter out1=new PrintWriter(outputStream,true);
			)
		{
			//new thread
			Thread in = new ClientReadFromMasterThread(client, toSend, toSendLock, clientID);//create new thread
			in.start();//start thread

			out1.println(clientID);//send client id to master

			int nextJobToSend;//id of next job
			int lastJobSent = -3;//random number that for sure less than -1
			Job curJob;

			loop:
			while (true)
			{
				synchronized (nextJobLock)
				{
					 nextJobToSend = nextJob.getNum();//get number of next job
				}
				if(nextJobToSend == -1)//means quit
				{
					break;
				}
				while(nextJobToSend > lastJobSent && nextJobToSend != -1)//as long as this job greater than last one and not indicated to quit
				{
					lastJobSent = nextJobToSend;
					synchronized (toSendLock)
					{
						curJob = toSend.get(nextJobToSend);
					}
					out1.println(curJob.getType());//send/write to master job type
					out1.println(curJob.getId());//send to master job id
					out1.println(curJob.getClientFrom());//tell master which client sent
					System.out.println("Sent Master: " + curJob);
				}
			}

			out1.println(-1);//indicate to master that it's done


			System.out.println("ClientWriteToMaster is done");



			try
			{
				in.join();
			}
			catch(Exception e)
			{
				System.out.println("Join Failed cwm");
			}
		}
		catch(UnknownHostException e) 
		{
		   System.err.println("Don't know about host ");
		   System.exit(1);
		}
		catch (IOException e)
		{
			System.err.println("Couldn't get I/O for the connection to the server on writing cwm");
			System.exit(1);
		}
	}//end of run method
}//end of class