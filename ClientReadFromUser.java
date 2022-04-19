import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ClientReadFromUser extends Thread
{
    private ArrayList<Job> toSend;//creates new array list to hold jobs that need to send
    private Object toSendLock;//dummy object to lock on array
    private int type;//evry job has a type
    private SharedNum id;//job id
    private Object idLock;//dummy object to lock it
    private int myID;//id of user
    private int clientID;//client id
    private Socket client;//intitializes socket for client socket
    private SharedNum nextJob = new SharedNum(1);//create sharedNum object for next job
    private Object nextJobLock = new Object();//dummy object to lock nextJob

    //constructor
    public ClientReadFromUser(Socket s, ArrayList<Job> toSend, Object toSendLock, SharedNum idCount, Object idLock, int cID)
    {
        this.client = s;
        this.toSend=toSend;
        this.toSendLock= toSendLock;
        this.id = idCount;
        this.idLock= idLock;
        this.clientID = cID;
    }

    @Override
    public void run()
    {
        try(
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        )
        {
            Thread out = null;

            String readJob= "";
            // continuously asks the user to input a job until they opt to quit
            loop:
            while(!readJob.equalsIgnoreCase("q"))//as long as user did not type q for quit
            {
                System.out.println("Which type of job do you need to be completed? Choose a or b, type q to end.");
                readJob=stdIn.readLine();//read whether job type is a,b, or q to wuit

                if(!readJob.equalsIgnoreCase("a") && !readJob.equalsIgnoreCase("b"))//if job type is neither a or b
                {
                    if(readJob.equalsIgnoreCase("q"))//if its q
                    {
                        break loop;//quit
                    }
                    while(!readJob.equalsIgnoreCase("a") && !readJob.equalsIgnoreCase("b"))//validate if still not a/b
                    {
                        System.out.println("Only choose a or b, or type q to end.");
                        readJob = stdIn.readLine();//read from user
                        if(readJob.equalsIgnoreCase("q"))
                        {
                            break loop;
                        }
                    }
                }

                //create a job
                if(readJob.equalsIgnoreCase("a"))
                {
                    type = 1;//set type=1
                }
                else
                {
                    type = 2;//set type=2
                }

                //gets the next number as an id, for easy reading
                synchronized (idLock)//make sure no thread can preempt this code while setting id of user to a number
                {
                    myID = id.getNum();//get job id
                    id.setNum(id.getNum() + 1);//set next id to one after
                }

                Job currentJob = new Job(type, myID, clientID);//create new job and pass parameters

                synchronized (toSendLock)//this block of code can not be interrupted by another thread
                {
                    toSend.add(currentJob);//add current job to array list
                    System.out.println("Created Job #" + myID + " of type " + type + " from client " + clientID);
                    synchronized(nextJobLock)//lock on next job
                    {
                        nextJob.setNum(toSend.size() - 1);//set number of next job to one more than last # in list
                    }
                }

                if(toSend.size() == 1)//if there is a job in the array
                {
                    //create new thread that send job to master
                    out = new ClientWriteToMasterThread(client, toSend, toSendLock, nextJob, nextJobLock, clientID);
                    out.start();//start thread
                }

                //to let created job print before asks for a new job - purely for optics
                sleep(300);
            }


            synchronized(nextJobLock)
            {
                nextJob.setNum(-1);//-1 means done
            }
            System.out.println("Indicated that I'm done.");

            try
            {
                out.join();
            }
            catch(Exception e){
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
            System.err.println("Couldn't get I/O for the connection to server on reading cru");
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }//end of run method
}

