import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class MasterWritesToClientThread extends Thread{
	Socket client;//client socket
    private ArrayList<Job> jobs;//array list of jobs
    private Object jobLock;//lock object for jobs in array list
    private SharedNum nextDoneJob;//value of job that completed
    private Object nextDJLock;//lock object for it
    private int clientID;//client id

    //constructor
	public MasterWritesToClientThread (Socket socket,ArrayList<Job>jobs, Object jobLock, SharedNum NDJ, Object NDJL, int cID) {
		this.client=socket;
        this.jobs=jobs;
        this.jobLock=jobLock;
        this.nextDoneJob = NDJ;
        this.nextDJLock = NDJL;
        this.clientID = cID;
	}

    public void run()
    {
        try (
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        ) {

            //loops until the nextDoneJob changes, which indicates that it can start
            int canIstart;
            while(true)
            {
                synchronized(nextDJLock)
                {
                    canIstart = nextDoneJob.getNum();//next job number
                }
                if(canIstart != -1)//quit
                {
                    break;
                }
            }

            // if there are objects with done execution, send the id to the client to update that job was done
            int doneJob;//latest complete job
            int lastDoneJob = -3;
            Job currentJob;//current job

            while(true)
            {
                synchronized(nextDJLock)
                {
                    doneJob = nextDoneJob.getNum();//which job is complete
                }
                if(doneJob == -1)//quit
                {
                    break;
                }
                if(doneJob != lastDoneJob)//new job
                {
                    synchronized (jobLock)
                    {
                        currentJob = jobs.get(doneJob);//curr job number
                    }
                    if(currentJob.getClientFrom() == clientID)//client who sent curr job
                    {
                        System.out.println("Returned to client: " + currentJob);
                        out.println(currentJob.getId());//write to client job id
                    }
                }
                lastDoneJob = doneJob;
            }

            System.out.println("MasterWriteToClient is done");
            out.println(-1);

        }
         catch (IOException e)
         {
            System.out.println("Exception caught when trying to listen on port or listening for a connection mwc");
            System.out.println(e.getMessage());
        }
    }
}
