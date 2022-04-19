import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MasterReadsFromSlaveBThread extends Thread{
	Socket slaveB;
	private ArrayList<Job> jobs;//array list to hold jobs
	private Object jobLock;//dummy object to lock on array list
	private SharedNum slaveBLoad;//shared number to know how much time of work slave b has
	private Object slaveBLoadLock;//dummy bject for the shared number
	private SharedNum nextJobForSlave;//value for the next job slave will work
	private Object nextJFSLock;//dummy object to lock on it
	private SharedNum nextDoneJob;
	private Object nextDJLock;

	//constructor
	public MasterReadsFromSlaveBThread(Socket s,ArrayList<Job>jobs, Object jobLock, SharedNum SBL, Object SBLL,
									   SharedNum NJS, Object NJSL, SharedNum NDJ, Object NDJL){
		this.slaveB = s;
		this.jobs=jobs;
		this.jobLock=jobLock;
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
				BufferedReader in = new BufferedReader(new InputStreamReader(slaveB.getInputStream()));
		)
		{
			//new thread
			Thread outSlaveB = new MasterWritesToSlaveBThread(slaveB,jobs, jobLock, nextJobForSlave, nextJFSLock);
			outSlaveB.start();//start it

			// receive id from slave b, indicate that it is ready to be sent back to the master
			// detract time from slave b load

			int type;//job type
			int id;//job id
			int clientFrom;//id of client sent job
			String read;//what slave b sent to master

			loop:
			while(true) {
				while ((read = in.readLine()) != null) {//as long as slave b sent, not null set to read
					type = Integer.parseInt(read);//assign it to job type
					if (type == -1) {//quit
						break loop;
					}
					if (type == 2)//if type=2
					{
						synchronized (slaveBLoadLock)
						{
							slaveBLoad.setNum(slaveBLoad.getNum() - 2);//take 2 seconds off form slave b load
						}
					}
					else //if type=2
					{
						synchronized (slaveBLoadLock)
						{
							slaveBLoad.setNum(slaveBLoad.getNum() - 10);//take 10 seconds off from slave b load
						}
					}
					id = Integer.parseInt(in.readLine());//job id

					clientFrom = Integer.parseInt(in.readLine());//client id
					synchronized (jobLock)
					{
						for (int i = 0; i < jobs.size(); i++)//loop thorough job array
						{
							if (jobs.get(i).getId() == id && jobs.get(i).getClientFrom() == clientFrom)//if id of the job at i in array==id of client who sent job
							{
								synchronized (nextDJLock)
								{
									//sets the next done job to be the job at index i
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
							System.out.println("Slave B returned: " + jobs.get(nextDoneJob.getNum()));
						}
					}
				}
			}

			System.out.println("MasterReadFromSB is done");
			synchronized(nextDJLock) {
				nextDoneJob.setNum(-1);
			}

			try
			{
				outSlaveB.join();
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
		} catch (IOException e)
		{
			System.err.println("Couldn't get I/O for the connection to a slave b");
			System.exit(1);
		}
	}//end of run method
}
