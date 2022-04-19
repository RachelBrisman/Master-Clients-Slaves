import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class MasterWritesToSlaveBThread extends Thread{
	Socket slaveB;
	private ArrayList<Job> jobs;//array list of jobs
	private Object jobLock;//dummy object to synchronize on
	private SharedNum nextJobForSlave;//a shared number object to hold the next job that the slave will need
	private Object nextJFSLock;//dummy object to lock on for the next job the slave need

	//constructor
	public MasterWritesToSlaveBThread (Socket socket,ArrayList<Job>jobs, Object jobLock, SharedNum NJS, Object NJSL) {
		this.slaveB=socket;
		this.jobs=jobs;
		this.jobLock=jobLock;
		this.nextJobForSlave = NJS;
		this.nextJFSLock = NJSL;
	}

	public void run() {
		try (
				OutputStream outputStream = slaveB.getOutputStream();
				PrintWriter out1=new PrintWriter(outputStream,true);

		) {

			//loops until the nextJobForSlave changes, which indicates that it can start
			int canIstart;
			while(true)
			{
				synchronized(nextJFSLock)
				{
					canIstart = nextJobForSlave.getNum();//same value as next job
				}
				if(canIstart != 0)//as soon as not 0
				{
					break;
				}
			}

			// master sends jobs objects to slave b
			// if nextJobforSlave's slave number is 2, sends it to slave B
			int nextNum;
			int prevNum = -3;//for sure less than -2
			Job currentJob;

			while(true)
			{
				synchronized (nextJFSLock)
				{
					nextNum = nextJobForSlave.getNum() - 1;//get next job number index
				}
				if(nextNum == -2)
				{
					break;
				}
				if(nextNum != prevNum)//new job
				{
					synchronized (jobLock)
					{
						currentJob = jobs.get(nextNum);
					}
					if(currentJob.getSlave() == 2)
					{
						out1.println(currentJob.getType());//write job type to slave b
						out1.println(currentJob.getId());//write job id to slave b
						out1.println(currentJob.getClientFrom());//tell slave b which client sent job
						System.out.println("Sent to Slave B: " + currentJob);
					}
					prevNum = nextNum;//most recent job
				}
			}

			System.out.println("MasterWriteToSBis done");
			out1.println(-1);
		}
		catch (IOException e)
		{
			System.out.println("Exception caught when trying to listen on port or listening for a connection mwsb");
			System.out.println(e.getMessage());
		}
	}
}
