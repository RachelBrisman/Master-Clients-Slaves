import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MasterReadsFromClientThread extends Thread {
	private Socket client;//client socket
	private ArrayList<Job> jobs;//array list of jobs
	private Object jobLock;//lock object on it
	private SharedNum slaveALoad;//sharedNum object of load for slave a
	private Object slaveALoadLock;//lock object on load for slave a
	private SharedNum slaveBLoad;//sharedNum object of load for slave b
	private Object slaveBLoadLock;//lock object on load for slave b
	private SharedNum nextJobForSlave;//sharednum object for next job id
	private Object nextJFSLock;//lock for next job id
	private SharedNum nextDoneJob;//sharedNum object of id of job done
	private Object nextDJLock;//lock object for id of job done

	//constructor
	public MasterReadsFromClientThread(Socket s,ArrayList<Job>jobs, Object jobLock,
									   SharedNum SAL, Object SALL, SharedNum SBL, Object SBLL,
										SharedNum NJS, Object NJSL, SharedNum NDJ, Object NDJL){
		this.client=s;
		this.jobs=jobs;
		this.jobLock=jobLock;
		this.slaveALoad = SAL;
		this.slaveALoadLock = SALL;
		this.slaveBLoad = SBL;
		this.slaveBLoadLock = SBLL;
		this.nextJobForSlave = NJS;
		this.nextJFSLock = NJSL;
		this.nextDoneJob = NDJ;
		this.nextDJLock = NDJL;
	}
	@Override
	public void run() {
		try(
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))
		)
		{
			//read jobs from user, calculate which slave to send it too, change the slave id, and add it to the arraylist

			String read;
			int clientID;

			loop:
			while(true)
			{
				while((read = in.readLine()) != null)//read until receives a valid id, which is the cue to start
				{
					clientID = Integer.parseInt(read);//client id
					break loop;
				}
			}

			//create new thread for master write t client
			Thread outClient = new MasterWritesToClientThread(client, jobs, jobLock, nextDoneJob, nextDJLock, clientID);
			outClient.start();//start thread

			Job currentJob;//current job
			int slaveA;//slave a
			int slaveB;//slave b
			int id;//job id
			int type;//job type
			int clientFrom;//id of client who sent job

			//continuously loops through what it receives from the client
			loop:
			while(true) {
				while ((read = in.readLine()) != null)//read value/text received from client
				{
					type = Integer.parseInt(read);
					if (type == -1)//means he wants to quit
					{
						break loop;
					}
					id = Integer.parseInt(in.readLine());//assign # to id
					clientFrom = Integer.parseInt(in.readLine());//note which client its from
					currentJob = new Job(type, id, clientFrom);//create new job with type, id, and which client sent
					System.out.println("Received Job: " + currentJob);
					//calculate which slave to set it to
					synchronized (slaveALoadLock)
					{
						slaveA = slaveALoad.getNum();//check how much time of job work slave a has so far to do
					}
					synchronized (slaveBLoadLock)
					{
						slaveB = slaveBLoad.getNum();//check how much time of job work slave b has so far to do
					}
					if (currentJob.getType() == 1)//if the job type=1
					{
						if ((slaveA + 2) > (slaveB + 10))//check to see if adding 2 seconds to slave a's load> adding 20 seconds to slave b's load
						{
							currentJob.setSlave(2);
							synchronized (slaveBLoadLock)
							{
								slaveBLoad.setNum(slaveBLoad.getNum() + 10);//then give it to slave b and add time to do the job
							}
						}
						else
						{
							currentJob.setSlave(1);//other wise set type to 1 and give it to slave a to do
							synchronized (slaveALoadLock)
							{
								slaveALoad.setNum(slaveALoad.getNum() + 2);//and add time to slave a's load
							}
						}
					}
					else//if job=type 2
					{
						if ((slaveB + 2) > (slaveA + 10))//if slave bs load greater than slave as load if it would take this job
						{
							currentJob.setSlave(1);
							synchronized (slaveALoadLock)
							{
								slaveALoad.setNum(slaveALoad.getNum() + 10);//add job time of 10 seconds to slave as load
							}
						}
						else//job type=2 and slave as load would be greater than slave bs job if it would take the job
						{
							currentJob.setSlave(2);
							synchronized (slaveBLoadLock)
							{
								slaveBLoad.setNum(slaveBLoad.getNum() + 2);//add 2 seconds to slave b load b/c its taking the job
							}
						}
					}
					synchronized (jobLock)
					{
						jobs.add(currentJob);
						synchronized (nextJFSLock)
						{
							nextJobForSlave.setNum(jobs.size());
						}
					}
				}
			}

			System.out.println("MasterReadFromClient is done");

			// reaches here once you quit (by the type being -1)
			synchronized (nextJFSLock)
			{
				nextJobForSlave.setNum(-1);
			}

			try{
				outClient.join();
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		}
		catch(UnknownHostException e) 
		{
		   System.err.println("Don't know about host ");
		   System.exit(1);
		}
		catch (IOException e)
		{
			System.err.println("Couldn't get I/O for the connection to a client mrc");
			System.exit(1);
		}
	}//end of run method
}
