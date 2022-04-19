import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SlaveAReadsFromMasterThread extends Thread{
	Socket slaveASocket;
	private ArrayList<Job> slaveAJobs;//array list of job
	private Object slaveAJobsLock;//lock object for array list
	private SharedNum nextJob;//sharedNum object for next job
	private Object nextJobLock;//dummy object to lock on for the next job the slave need

	//constructor
	public SlaveAReadsFromMasterThread(Socket s,ArrayList<Job>jobs, Object lock, SharedNum nj, Object njLock) {
		this.slaveASocket = s;
		this.slaveAJobs=jobs;
		this.slaveAJobsLock=lock;
		this.nextJob = nj;
		this.nextJobLock = njLock;
	}

	@Override
	public void run() {
		try(
				BufferedReader in1=new BufferedReader(new InputStreamReader(slaveASocket.getInputStream()));//BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			)
		{
			//create new thread
			Thread out = new SlaveAWritesToMasterThread(slaveASocket,slaveAJobs, slaveAJobsLock, nextJob, nextJobLock);
			out.start();//start thread

			// read job from client
			// wait amount of seconds necessary, 2 is type a, 10 if type b
			// add to arraylist of jobs to be returned

			int type;//job type
			int id;//job id
			int clientFrom;//id of client who sent job
			Job currentJob;
			String read;

			loop:
			while(true)
			{
				while ((read = in1.readLine()) != null)//read what master sent, as long as it not null
				{
					type = Integer.parseInt(read);
					if (type == -1) //quit
					{
						break loop;
					}
					id = Integer.parseInt(in1.readLine());//set what master sent as id for job
					clientFrom = Integer.parseInt(in1.readLine());//read what master sent and assign to client id

					currentJob = new Job(type, id, clientFrom);//create new job
					if (type == 1) //if job type=1
					{
						System.out.println("Received from master: " + currentJob + ", sleeping for 2 seconds.");
						sleep(2000);//put to sleep for 2 seconds
					}
					else //if type=2
					{
						System.out.println("Received from master: " + currentJob + ", sleeping for 10 seconds.");
						sleep(10000);//put to sleep for 10 seconds
					}
					synchronized (slaveAJobsLock)
					{
						slaveAJobs.add(currentJob);//add job to list of slave as jobs
						synchronized (nextJobLock)
						{
							nextJob.setNum(slaveAJobs.size());//the number for the next job will be the one after last one in array list of slave a jobs
						}
					}
				}
			}

			System.out.println("SlaveAReadFromMaster is done");
			synchronized(nextJobLock)
			{
				nextJob.setNum(-1);
			}


			try
			{
				out.join();
			}
			catch(Exception e)
			{
				System.out.println("Join Failed sarm");
			}
		}
		catch(UnknownHostException e) 
		{
		   System.err.println("Don't know about host ");
		   System.exit(1);
		}
		catch (IOException e)
		{
			System.err.println("Couldn't get I/O for the connection to sarm");
			System.exit(1);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
}//end of run method
}


