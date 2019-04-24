package simpleSync;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.*;
import java.io.*;
import java.nio.file.attribute.FileTime;
import org.apache.commons.io.*;
import java.net.*;
import myPackages.adt.*;

public class NewThread extends Thread
{
    /**the error messages
    */
    private ArrayList<String> errorMessages;
    
    /**the ui
    */
    private SimpleSyncUI ui;
    
    public NewThread(SimpleSyncUI ui)
    {
        this.ui = ui;
        errorMessages = new ArrayList<String>();
    }
    
    /**@return the error messages in the event that operation is null
    */
    public ArrayList<String> getErrorMessages()
    {
        return errorMessages;
    }
    
    @Override
    public void run()
    {
        JProgressBar progressBar = ui.getProgressBar();
        progressBar.setIndeterminate(true);
        try
        {
            FileSystem fs = ui.getFileSystem();
            ArrayList<Path> leftPaths = new ArrayList<Path>();
            ArrayList<Path> rightPaths = new ArrayList<Path>();

            Path leftDir = ui.getLeftDir();
            Path rightDir = ui.getRightDir();
            progressBar.setString("Parsing "+leftDir+" ... ");
            leftPaths = new ArrayList<Path>();
            DirectoryStream<Path> leftDirStream = Files.newDirectoryStream(leftDir);
            Utilities.parseDir(leftDirStream,leftPaths);
            
            SortedList<String> leftFiles = new SortedList<String>(false);
            for(int i=0;i<leftPaths.size();i++)
            {
                Path path = leftPaths.get(i);
                String entry = path.toString();
                entry = entry.replace(leftDir.toString(),"");
                leftFiles.add(entry);
            }
            
            progressBar.setString("Parsing "+rightDir+" ... ");
            rightPaths = new ArrayList<Path>();
            DirectoryStream<Path> rightDirStream = Files.newDirectoryStream(rightDir);
            Utilities.parseDir(rightDirStream,rightPaths);
            
            SortedList<String> rightFiles = new SortedList<String>(false);
            for(int i=0;i<rightPaths.size();i++)
            {
                Path path = rightPaths.get(i);
                String entry = path.toString();
                entry = entry.replace(rightDir.toString(),"");
                rightFiles.add(entry);
            }
            
            progressBar.setIndeterminate(false);
            
            ArrayList<Operation> operations = new ArrayList<Operation>();
            boolean conflict = false;
            
            System.out.println("left files = "+leftFiles.size());
            System.out.println("right files = "+rightFiles.size());
            
            progressBar.setString("Checking files in "+leftDir+" ... ");
            progressBar.setMaximum(leftFiles.size());
            for(int i=0;i<leftFiles.size();i++)
            {
                progressBar.setValue(i);
                String file = leftFiles.get(i);
                int pos = rightFiles.indexOf(file);
                if (pos>=0)
                {
                    File file1 = new File(leftDir+File.separator+file);
                    File file2 = new File(rightDir+File.separator+file);
                    boolean isTwoEqual = FileUtils.contentEquals(file1, file2);
                    
                    if (!isTwoEqual)
                    {
                        conflict = true;;//if not equal then its conflict
                        errorMessages.add("[CONFLICT] "+file+" does not match on both sides.");
                    }
                    rightFiles.remove(pos);
                }
                else
                {
                    //just copy to right
                    Path leftPath = fs.getPath(leftDir.toString()+file);
                    Path rightPath = fs.getPath(rightDir.toString()+file);
                    Operation operation = new Operation(leftPath,rightPath,Operation.Action.COPY_FROM_LEFT_TO_RIGHT,"[COPY] "+leftPath+ " --> "+rightPath);
                    operations.add(operation);
                }
            }
            
            progressBar.setString("Checking files in "+rightDir+" ... ");
            progressBar.setMaximum(rightFiles.size());
            for(int i=0;i<rightFiles.size();i++)
            {
                progressBar.setValue(i);
                String file = rightFiles.get(i);
                int pos = leftFiles.indexOf(file);
                if (pos>=0)
                {
                    errorMessages.add("[CONFLICT] Should not occur. Check code!");
                }
                else
                {
                    //just copy to left
                    Path leftPath = fs.getPath(leftDir.toString()+file);
                    Path rightPath = fs.getPath(rightDir.toString()+file);
                    Operation operation = new Operation(leftPath,rightPath,Operation.Action.COPY_FROM_RIGHT_TO_LEFT,"[COPY] "+leftPath+ " <-- "+rightPath);
                    operations.add(operation);
                }
            }
            
            progressBar.setString("");
            progressBar.setValue(0);
            
            if (!conflict)
            {
                ui.setOperations(operations);
            }
            else
            {
                String error = "";
                for(String msg:errorMessages)
                {
                    error = error + msg + ";";
                }
                JOptionPane.showMessageDialog(ui,error,"Error Message",JOptionPane.ERROR_MESSAGE);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}