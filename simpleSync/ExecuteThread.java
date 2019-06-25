package simpleSync;

import java.util.*;
import javax.swing.*;
import java.nio.file.*;
import java.io.*;
import java.nio.file.attribute.FileTime;
import java.net.*;

public class ExecuteThread extends Thread
{
    private SimpleSyncUI ui;
    
    public ExecuteThread(SimpleSyncUI ui)
    {
        this.ui = ui;
    }
    
    @Override
    public void run()
    {
        ArrayList<String> errorMessages = new ArrayList<String>();
        
        //execute the operations
        ArrayList<String> messages = executeOperation();
        errorMessages.addAll(messages);
        
        if (errorMessages.size()>0)
        {
            String error = "";
            for(String msg:errorMessages)
            {
                error = error + msg + ";";
            }
            JOptionPane.showMessageDialog(ui,error,"Error Message",JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            ui.setOperations(new ArrayList<Operation>());
            ui.save();
        }
    }
    
    private ArrayList<String> executeOperation()
    {
        ArrayList<String> errorMsg = new ArrayList<String>();
        JProgressBar progressBar = ui.getProgressBar();
        ArrayList<Operation> operations = ui.getOperations();
        try
        {    
            progressBar.setMaximum(operations.size());
            for(int i=0;i<operations.size();i++)
            {
                progressBar.setValue(i);
                Operation operation = operations.get(i);
                progressBar.setString(operation.getMessage());
                
                boolean succeed = operation.execute();
                if (!succeed)
                {
                    errorMsg.add(operation.toString()+" failed.");
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return errorMsg;
    }
}