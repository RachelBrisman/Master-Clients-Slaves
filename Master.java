import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Master {
    //this class will be asked to perform tasks by the clients,
    //and send the workout to different slaves, determining which is the best to take the job

    public static void main(String[] args) throws IOException {
        ServerSocket masterSocket;//initialize server socket
        Socket client1 = null;//4 client sockets to both clients and both slaves
        Socket client2 = null;
        Socket slaveA = null;
        Socket slaveB = null;
        ArrayList<Job> jobsC = new ArrayList<Job>();
        Object jobsCLock = new Object();
        SharedNum slaveALoad = new SharedNum(0);//create sharedNum objects to hold load of time of work for slave a
        SharedNum slaveBLoad = new SharedNum(0);//create sharedNum objects to hold load of time of work for slave b
        Object slaveALoadLock = new Object();//lock object
        Object slaveBLoadLock = new Object();//lock object
        SharedNum nextJobForSlave = new SharedNum(0);//sharedNum object for next job
        Object nextJFSLock = new Object();//lock
        SharedNum nextDoneJob = new SharedNum(-1);//shared num object for job that complete
        Object nextDJLock = new Object();//lock

        try
        {
            masterSocket = new ServerSocket(43321);
            client1 = masterSocket.accept();
            masterSocket = new ServerSocket(43331);
            client2 = masterSocket.accept();
            masterSocket = new ServerSocket(43341);
            slaveA = masterSocket.accept();
            masterSocket = new ServerSocket(43351);
            slaveB = masterSocket.accept();
        }catch (IOException e) {
            System.out.println("I/O error: " + e);
        }

        System.out.println("Clients Accepted");//create threads
        Thread inSlaveA = new MasterReadsFromSlaveAThread(slaveA,jobsC, jobsCLock, slaveALoad, slaveALoadLock, nextJobForSlave, nextJFSLock,
                                                        nextDoneJob, nextDJLock);
        Thread inSlaveB = new MasterReadsFromSlaveBThread(slaveB,jobsC, jobsCLock, slaveBLoad, slaveBLoadLock, nextJobForSlave, nextJFSLock,
                                                                nextDoneJob, nextDJLock);
        Thread inClient1 = new MasterReadsFromClientThread(client1,jobsC, jobsCLock, slaveALoad, slaveALoadLock,
                            slaveBLoad, slaveBLoadLock, nextJobForSlave, nextJFSLock, nextDoneJob, nextDJLock);
        Thread inClient2 = new MasterReadsFromClientThread(client2,jobsC, jobsCLock, slaveALoad, slaveALoadLock,
                            slaveBLoad, slaveBLoadLock, nextJobForSlave, nextJFSLock, nextDoneJob, nextDJLock);

        //start threads
        inSlaveA.start();//
        inSlaveB.start();
        inClient1.start();
        inClient2.start();
        System.out.println("Threads Started");

        try {
            inSlaveA.join();
            inSlaveB.join();
            inClient1.join();
            inClient2.join();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Join Failed in master");
        }
    }
}