import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SlaveAWritesToMasterThread extends Thread{
	Socket slaveA;
	private ArrayList<Job> slaveAJobs;//array list of job
	private Object slaveAJobsLock;//lock object for array list
	private SharedNum nextJob;//sharedNum object for next job
	private Object nextJobLock;//dummy object to lock on for the next job the slave need

	//constructor
	public SlaveAWritesToMasterThread(Socket s,ArrayList<Job>jobs, Object lock, SharedNum nj, Object njLock)
	{
		this.slaveA = s;
		this.slaveAJobs=jobs;
		this.slaveAJobsLock=lock;
		this.nextJob = nj;
		this.nextJobLock = njLock;
	}

	@Override
	public void run() {
		try(
			PrintWriter out=new PrintWriter(slaveA.getOutputStream(),true);
		)
		{
			//loops until the nextJob changes, which indicates that it can start
			int canIstart;
			while(true)
			{
				synchronized(nextJobLock)
				{
					canIstart = nextJob.getNum();//job number
				}
				if(canIstart != 0)
				{
					break;
				}
			}

			// when there is a new done job, send it back to the master

			int nextNum;
			int prevNum = -3;//for sure less than -1
			Job currentJob;

			while(true)
			{
				synchronized (nextJobLock)
				{
					nextNum = nextJob.getNum() - 1;
				}
				if(nextNum == -2)
				{
					break;
				}
				if(nextNum != prevNum)
				{
					synchronized (slaveAJobsLock)
					{
						currentJob = slaveAJobs.get(nextNum);
					}
					out.println(currentJob.getType());//tell master job type
					out.println(currentJob.getId());//tell master job id
					out.println(currentJob.getClientFrom());//tell job which client sent job
					System.out.println("Sent Back to Master: " + currentJob);
					prevNum = nextNum;
				}
			}

			System.out.println("SlaveAWriteToMaster is done");
			out.println(-1);
		}
		catch(UnknownHostException e)
		{
			System.err.println("Don't know about host " );
			System.exit(1);
		}
		catch (IOException e)
		{
			System.err.println("Couldn't get I/O for the connection to sawm");
			System.exit(1);
		}
	}//end of run method
}