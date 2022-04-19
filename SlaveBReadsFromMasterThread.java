import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SlaveBReadsFromMasterThread extends Thread{
	Socket slaveB;
	private ArrayList<Job> slaveBJobs;//array list of job
	private Object slaveBJobsLock;//lock object for array list
	private SharedNum nextJob;//sharedNum object for next job
	private Object nextJobLock;//dummy object to lock on for the next job the slave need
	//constructor
	public SlaveBReadsFromMasterThread(Socket s,ArrayList<Job>jobs, Object lock, SharedNum nj, Object njLock) {
		this.slaveB = s;
		this.slaveBJobs=jobs;
		this.slaveBJobsLock=lock;
		this.nextJob = nj;
		this.nextJobLock = njLock;
	}
	@Override
	public void run() {
		try(
				BufferedReader in1=new BufferedReader(new InputStreamReader(slaveB.getInputStream()));//BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

		)
		{
			//new thread
			Thread out = new SlaveBWritesToMasterThread(slaveB,slaveBJobs, slaveBJobsLock, nextJob, nextJobLock);
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
				while ((read = in1.readLine()) != null)//as long as master sent s/t not null
				{
					type = Integer.parseInt(read);//assign what master sent to type
					if (type == -1)
					{
						break loop;//means quit
					}
					id = Integer.parseInt(in1.readLine());//master sent id of job
					clientFrom = Integer.parseInt(in1.readLine());//read what master sent as id of which client its from
					currentJob = new Job(type, id, clientFrom);//create new job
					if (type == 2) //if job type =2
					{
						System.out.println("Received from master: " + currentJob + ", sleeping for 2 seconds.");
						sleep(2000);//sleep 2 seconds
					}
					else //if job type=1
					{
						System.out.println("Received from master: " + currentJob + ", sleeping for 10 seconds.");
						sleep(10000);//sleep 10 seconds
					}
					synchronized (slaveBJobsLock)
					{
						slaveBJobs.add(currentJob);//add this job to slave b list
						synchronized (nextJobLock)
						{
							nextJob.setNum(slaveBJobs.size());//next job number is one greater than last number in list
						}
					}
				}
			}

			System.out.println("SlaveBReadFromMaster is done");
			synchronized(nextJobLock)
			{
				nextJob.setNum(-1);
			}


			try
			{
				out.join();
			}
			catch (Exception e)
			{
				System.out.println("Join Failed sbwm");
			}
		}
		catch(UnknownHostException e)
		{
			System.err.println("Don't know about host ");
			System.exit(1);
		}
		catch (IOException e)
		{
			System.err.println("Couldn't get I/O for the connection to sbrm");
			System.exit(1);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}//end of run method
}


