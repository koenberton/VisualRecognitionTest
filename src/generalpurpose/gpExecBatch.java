package generalpurpose;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;



public class gpExecBatch {

	int BatTeller=0;
	
	int exitlevel=-1;
	
	
	ArrayList<String> list  = null;
	ArrayList<String> list2 = null;
	//
    //------------------------------------------------------------
    private void do_log(int logLevel , String sIn)
	//------------------------------------------------------------
    {
       if (logLevel == 0 ) System.err.println(sIn);
       else System.out.println(sIn);
    }
	//------------------------------------------------------------
    private void do_error(String sIn)
	//------------------------------------------------------------
    {
    	do_log(0,sIn);
    }

	//---------------------------------------------------------------------------------
    public gpExecBatch(String inBatchFile)
   	//---------------------------------------------------------------------------------
    {
    	//
    	list  = new ArrayList<String>();
    	list2 = new ArrayList<String>();
    	//
    	exitlevel = executeCommandWithExecutors(inBatchFile,true,true,100000,list,list2);
    }
    
    //---------------------------------------------------------------------------------
    public int getExitLevel()
    //---------------------------------------------------------------------------------
    {
    	return exitlevel;
    }
    //
	//---------------------------------------------------------------------------------
    private int executeCommandWithExecutors(       String inBatchFile,
                                                  final boolean printOutput,
                                                  final boolean printError,
                                                  final long timeOut,
                                                  ArrayList<String> stdout,
                                                  ArrayList<String> stderr)
    //---------------------------------------------------------------------------------
    {
    	 String command = "noppes";
    	 BatTeller++;
    	
    	 String massagedCommand = "noppes";
    	 if ( System.getProperty("file.separator").toCharArray()[0] == '/' ) {
 			massagedCommand = "/bin/sh " + inBatchFile;
 	     }
    	 else {
    	    massagedCommand = "cmd.exe /C " + inBatchFile;
    	 }
    	 do_log(5,"running -" + massagedCommand );
    	 
         try
         {
            // create the process which will run the command
            Runtime runtime = Runtime.getRuntime();
            final Process process = runtime.exec(massagedCommand);

            // consume and display the error and output streams
            gpStreamGobbler outputGobbler = new gpStreamGobbler(process.getInputStream(), "OUTPUT", printOutput,stdout,process);
            gpStreamGobbler errorGobbler  = new gpStreamGobbler(process.getErrorStream(), "ERROR" , printError, stderr,process);
            outputGobbler.start();
            errorGobbler.start();

            
            // create a Callable for the command's Process which can be called by an Executor 
            Callable<Integer> call = new Callable<Integer>()
            {
                public Integer call()
                    throws Exception
                {
                    process.waitFor();
                    return process.exitValue();
                }
            };

            // submit the command's call and get the result from a 
            Future<Integer> futureResultOfCall = Executors.newSingleThreadExecutor().submit(call);
            try
            {
                int exitValue = futureResultOfCall.get(timeOut, TimeUnit.MILLISECONDS);
                return exitValue;
            }
            catch (TimeoutException ex)
            {
                String errorMessage = "The command [" + command + "] timed out.";
                do_error(errorMessage);
                throw new RuntimeException(errorMessage, ex);
            }
            catch (ExecutionException ex)
            {
                String errorMessage = "The command [" + command + "] did not complete due to an execution error.";
                do_error(errorMessage);
                throw new RuntimeException(errorMessage, ex);
            }
            finally {
            	/*
            	if( this.IsBestand(FNaam) ) {
            		this.VerwijderBestand(FNaam);
            		logit("Removed " + FNaam);
            	}
            	*/
            }
        }
        catch (InterruptedException ex)
        {
            String errorMessage = "The command [" + command + "] did not complete due to an unexpected interruption.";
            do_error(errorMessage);
            throw new RuntimeException(errorMessage, ex);
        }
        catch (IOException ex)
        {
            String errorMessage = "The command [" + command + "] did not complete due to an IO error.";
            do_error(errorMessage);
            throw new RuntimeException(errorMessage, ex);
        }
   
    }
    
    public ArrayList<String> getSTDOUT()
    {
    	return list;
    }

    public ArrayList<String> getSTDERR()
    {
    	return list2;
    }
}
