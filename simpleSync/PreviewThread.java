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

public class PreviewThread extends Thread
{
    /**the error messages
    */
    private List<String> errorMessages;
    
    /**the input file
    */
    private Path input;
    
    /**the file system
    */
    private FileSystem fs;
    
    /**the left folder
    */
    private Path leftDir;
    
    /**the right folder
    */
    private Path rightDir;
    
    /**the list of files in the left dir
    */
    private ArrayList<Path> leftPaths;
    
    /**the list of files in the right dir
    */
    private ArrayList<Path> rightPaths;
    
    /**the list of files in the left db
    */
    private HashMap<String,Long> leftHashMap;
    
    /**the list of files in the right db
    */
    private HashMap<String,Long> rightHashMap;
    
    /**the progress bar
    */
    private JProgressBar progressBar;
    
    /**the ui
    */
    private SimpleSyncUI ui;
    
    /**constructor
    *@param ui the ui
    */
    public PreviewThread(SimpleSyncUI ui)
    {
        this.ui = ui;
        this.fs = ui.getFileSystem();
        this.input = ui.getInput();
        errorMessages = new ArrayList<String>();
        this.progressBar = ui.getProgressBar();
    }
    
    @Override
    public void run()
    {
        try
        {
            ArrayList<Operation> operations = new ArrayList<Operation>();
            boolean conflict = false;
            
            progressBar.setIndeterminate(true);
            progressBar.setString("Reading "+input+" ... ");
            readFile(input);
            progressBar.setString("Parsing "+leftDir+" ... ");
            leftPaths = new ArrayList<Path>();
            DirectoryStream<Path> leftDirStream = Files.newDirectoryStream(leftDir);
            Utilities.parseDir(leftDirStream,leftPaths);
            
            progressBar.setString("Parsing "+rightDir+" ... ");
            rightPaths = new ArrayList<Path>();
            DirectoryStream<Path> rightDirStream = Files.newDirectoryStream(rightDir);
            Utilities.parseDir(rightDirStream,rightPaths);
            progressBar.setIndeterminate(false);
            
            progressBar.setString("Finding files that had changed in the left folder ...");
            ArrayList<String> leftChanged = filesChanged(leftDir,leftPaths,leftHashMap);
            System.out.println(""+leftChanged.size()+" files on left got changes.");
            
            progressBar.setString("Finding files that had changed in the right folder ...");
            ArrayList<String> rightChanged = filesChanged(rightDir,rightPaths,rightHashMap);
            System.out.println(""+rightChanged.size()+" files on right got changes.");
            
            progressBar.setString("Finding files that had been deleted in the left folder ...");
            ArrayList<String> leftDeleted = filesDeleted(leftDir,leftPaths,leftHashMap);
            System.out.println(""+leftDeleted.size()+" files on left got deleted.");
            
            progressBar.setString("Finding files that had been deleted in the right folder ...");
            ArrayList<String> rightDeleted = filesDeleted(rightDir,rightPaths,rightHashMap);
            System.out.println(""+rightDeleted.size()+" files on right got deleted.");
            
            
            progressBar.setString("Checking for conflicts from left -> right ...");
            progressBar.setMaximum(leftChanged.size());
            for(int i=0;i<leftChanged.size();i++)
            {
                progressBar.setValue(i);
                String file = leftChanged.get(i);
                //check if right got delete or changes
                if (rightChanged.contains(file))
                {
                    //check if both file are the same
                    File file1 = new File(leftDir+File.separator+file);
                    File file2 = new File(rightDir+File.separator+file);
                    boolean isTwoEqual = FileUtils.contentEquals(file1, file2);
                    
                    if (!isTwoEqual)
                    {
                        conflict = true;;//if not equal then its conflict
                        errorMessages.add("[CONFLICT] "+file+" got updates on both sides.");
                    }    
                    rightChanged.remove(file);
                }
                else if (rightDeleted.contains(file))
                {
                    conflict = true;
                    //System.out.println("[CONFLICT] "+file+" got updates on left but deleted on right.");
                    errorMessages.add("[CONFLICT] "+file+" got updates on left but deleted on right.");
                    rightDeleted.remove(file);
                }
                else
                {
                    //no conflict
                    Path leftPath = fs.getPath(leftDir.toString()+file);
                    Path rightPath = fs.getPath(rightDir.toString()+file);
                    Operation operation = new Operation(leftPath,rightPath,Operation.Action.COPY_FROM_LEFT_TO_RIGHT,"[UPDATE] "+leftPath+ " --> "+rightPath);
                    operations.add(operation);
                }
            }
            
            progressBar.setString("Checking for conflicts from left delete ...");
            progressBar.setMaximum(leftDeleted.size());
            for(int i=0;i<leftDeleted.size();i++)
            {
                String file = leftDeleted.get(i);
                progressBar.setValue(i);
                //check if right got changes
                if (rightChanged.contains(file))
                {
                    conflict = true;
                    //System.out.println("[CONFLICT] "+file+" got updates on right but deleted on left.");
                    errorMessages.add("[CONFLICT] "+file+" got updates on right but deleted on left.");
                    rightChanged.remove(file);
                }
                else if (rightDeleted.contains(file))
                {
                    //do nothing
                }
                else
                {
                    //no conflict
                    Path leftPath = fs.getPath(leftDir.toString()+file);
                    Path rightPath = fs.getPath(rightDir.toString()+file);
                    Operation operation = new Operation(leftPath,rightPath,Operation.Action.DELETE_RIGHT,"[DELETE] "+rightPath);
                    operations.add(operation);
                }
            }
            
            
            
            for(String file:rightChanged)
            {
                //check if left got delete or changes
                if (leftChanged.contains(file))
                {
                    conflict = true;
                    errorMessages.add("[CONFLICT] "+file+" should not reach here! Check code!");
                    //should not reach here
                }
                else if (leftDeleted.contains(file))
                {
                    conflict = true;
                    errorMessages.add("[CONFLICT] "+file+" should not reach here! Check code!");
                    //should not reach here
                }
                else
                {
                    //no conflict
                    Path leftPath = fs.getPath(leftDir.toString()+file);
                    Path rightPath = fs.getPath(rightDir.toString()+file);
                    Operation operation = new Operation(leftPath,rightPath,Operation.Action.COPY_FROM_RIGHT_TO_LEFT,"[UPDATE] "+leftPath+ " <-- "+rightPath);
                    operations.add(operation);
                }
            }
            
            for(String file:rightDeleted)
            {
                //check if right got changes
                if (leftChanged.contains(file))
                {
                    conflict = true;
                    errorMessages.add("[CONFLICT] "+file+" should not reach here! Check code!");
                    //should not reach here
                }
                else if (leftDeleted.contains(file))
                {
                    //do nothing
                }
                else
                {
                    //no conflict
                    Path leftPath = fs.getPath(leftDir.toString()+file);
                    Path rightPath = fs.getPath(rightDir.toString()+file);
                    Operation operation = new Operation(leftPath,rightPath,Operation.Action.DELETE_LEFT,"[DELETE] "+leftPath);
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
            errorMessages.add(e.toString());
        }
    }
    
    /**@return the list of files that changes since last creation of database
    *@param dir the directory
    *@param files the list of files
    /@param hashMap the database
    */
    private ArrayList<String> filesChanged(Path dir,List<Path> files,HashMap<String,Long> hashMap)
        throws IOException
    {
        ArrayList<String> list = new ArrayList<String>();
        progressBar.setMaximum(files.size());
        for(int i=0;i<files.size();i++)
        {
            Path path = files.get(i);
            String entry = path.toString();
            entry = entry.replace(dir.toString(),"");
            
            long currentLastChanged = Files.getLastModifiedTime(path).toMillis();
            Long prevLastChanged = hashMap.get(entry);
            
            //prevLastChanged == null means new file
            
            if (prevLastChanged==null)
            {
                list.add(entry);
            }
            else 
            {
                if (currentLastChanged!=prevLastChanged)
                {
                    //System.out.println("entry = "+entry+" current = "+currentLastChanged+" prev="+prevLastChanged); 
                    list.add(entry);
                }
                //remove entry from hashmap since file exist, need not check for deletion later
                hashMap.remove(entry);
            }
            progressBar.setValue(i);
        }
        progressBar.setValue(files.size());
        return list;
    }
    
    
    /**@return the list of files that had been deleted since last creation of database
    *@param dir the directory
    *@param files the list of files
    /@param hashMap the database
    */
    private ArrayList<String> filesDeleted(Path dir,List<Path> files,HashMap<String,Long> hashMap)
        throws IOException
    {
        ArrayList<String> list = new ArrayList<String>();
        ArrayList<String> filesList = new ArrayList<String>();
        progressBar.setMaximum(files.size());
        for(int i=0;i<files.size();i++)
        {
            
            Path path = files.get(i);
            String entry = path.toString();
            entry = entry.replace(dir.toString(),"");
            filesList.add(entry);
            progressBar.setValue(i);
        }
        
        
        for (String key : hashMap.keySet())
        {
            if (!filesList.contains(key))
            {
                list.add(key);
            }
        }
        progressBar.setValue(files.size());
        return list;
    }
    
    /**@return the db read
    */
    private HashMap<String,Long> readDB(BufferedReader reader)
        throws IOException
    {
        HashMap<String,Long> hashMap = new HashMap<String,Long>();
        String line1 = reader.readLine();
        String line2 = reader.readLine();
        while((line2!=null)&&(!line1.startsWith("<")))
        {
            long l = Long.parseLong(line2);
            hashMap.put(line1,l);
            line1 = reader.readLine();
            line2 = reader.readLine();
        }
        System.out.println("db size = "+hashMap.size());
        return hashMap;
    }
    
    /**@return the db read
    */
    private void readFile(Path path)
        throws IOException
    {
        
        System.out.println("Reading file "+path+" ...");
        //create a new db
        //Files.createFile(db);
        String url = "jar:"+path.toUri();
        
        URI uri = URI.create(url);
        Map<String, String> env = new HashMap<>();
        FileSystem zipFileSystem = FileSystems.newFileSystem(uri, env);
        Path document = zipFileSystem.getPath("/db.txt");
        
        leftHashMap = new HashMap<String,Long>();
        rightHashMap = new HashMap<String,Long>();
        
        BufferedReader reader = Files.newBufferedReader(document,java.nio.charset.StandardCharsets.UTF_16);
        String line = reader.readLine();
        while((line!=null)&&(line.length()>0))
        {
            //System.out.println("line="+line);
            if (line.startsWith("<"))
            {
                //left or right folder
                if (leftDir==null)
                {
                    System.out.println("Left dir="+line.substring(1,line.length()-1));
                    leftDir = fs.getPath(line.substring(1,line.length()-1));
                    ui.setLeftDir(leftDir);
                }
                else
                {
                    System.out.println("Right dir="+line.substring(1,line.length()-1));
                    rightDir = fs.getPath(line.substring(1,line.length()-1));
                    ui.setRightDir(rightDir);
                }
            }
            else
            {
                String line2 = reader.readLine();
                long l = Long.parseLong(line2);
                if (rightDir==null)
                {
                    leftHashMap.put(line,l);
                }
                else
                {
                    rightHashMap.put(line,l);
                }
            }
            line = reader.readLine();
        }
        reader.close();
        zipFileSystem.close();
        System.out.println("Done!");
    }
    
    /**@return the error messages in the event that operation is null
    */
    public List<String> getErrorMessages()
    {
        return errorMessages;
    }
}