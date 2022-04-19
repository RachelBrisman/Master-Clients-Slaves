import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MasterReadsFromSlaveAThread extends Thread{
	Socket slaveA;
	private ArrayList<Job> jobs;//array list to hold jobs
	private Object jobLock;//dummy object to lock on array list
	private SharedNum slaveALoad;//shared number to know how much time of work slave a has
	private Object slaveALoadLock;//dummy object for the shared number
	private SharedNum nextJobForSlave;//value for the next job slave will work
	private Object nextJFSLock;//dummy object to lock on it
	private SharedNum nextDoneJob;
	private Object nextDJLock;

	//constructor
	public MasterReadsFromSlaveAThread(Socket s,ArrayList<Job>jobs, Object jobLock, SharedNum SAL, Object SALL,
									   SharedNum NJS, Object NJSL, SharedNum NDJ, Object NDJL){
		this.slaveA = s;
		this.jobs=jobs;
		this.jobLock=jobLock;
		this.slaveALoad = SAL;
		this.slaveALoadLock = SALL;
		this.nextJobForSlave = NJS;
		this.nextJFSLock = NJSL;
		this.nextDoneJob = NDJ;
		this.nextDJLock = NDJL;
	}

	@Override
	public void run() {
		try(
				BufferedReader in = new BufferedReader(new InputStreamReader(slaveA.getInputStream()));
		)
		{
			//new thread
			Thread outSlaveA = new MasterWritesToSlaveAThread(slaveA,jobs, jobLock, nextJobForSlave, nextJFSLock);
			outSlaveA.start();//start thread

			// receive id from slave a, indicate that it is ready to be sent back to the master
			//detract time from slave a load

			int type;//job type
			int id;//job id
			int clientFrom;//id of client who sent job
			String read;//read from slave a

			loop:
			while(true)
			{
				while ((read = in.readLine()) != null)//as long as slave a sent s/t not null set to read
				{
					type = Integer.parseInt(read);//master reads what slave a sent and assigns that to job type
					if (type == -1) //means quit
					{
						break loop;
					}
					if (type == 1)//if type is 1
					{
						synchronized (slaveALoadLock)//critical section
						{
							slaveALoad.setNum(slaveALoad.getNum() - 2);//take away 2 seconds from slave as load
						}
					}
					else//if type is 2
					{
						synchronized (slaveALoadLock)
						{
							slaveALoad.setNum(slaveALoad.getNum() - 10);//take away 10 seconds of slave a load
						}
					}

					id = Integer.parseInt(in.readLine());//assign the next thing it reads to job id

					clientFrom = Integer.parseInt(in.readLine());//assign it to id of which client sent
					synchronized (jobLock)
					{
						for (int i = 0; i < jobs.size(); i++)//loop through array of jobs
						{
							if (jobs.get(i).getId() == id && jobs.get(i).getClientFrom() == clientFrom)//if id of the job at i in array==id of client who sent it
							{
								synchronized (nextDJLock)
								{
									//sets the next done job to be the job at i index
									nextDoneJob.setNum(i);
									break;
								}
							}
						}
					}
					synchronized (jobLock)
					{
						synchronized (nextDJLock)
						{
							System.out.println("Slave A returned: " + jobs.get(nextDoneJob.getNum()));//value of job that was completed
						}
					}
				}
			}

			System.out.println("MasterReadFromSA is done");

			synchronized(nextDJLock)
			{
				nextDoneJob.setNum(-1);//finished
			}


			try
			{
				outSlaveA.join();
			}
			catch(Exception e)
			{
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
			System.err.println("Couldn't get I/O for the connection to slave a");
			System.exit(1);
		}
	}//end of run method
}
