package simpleSync;

import java.io.*;
import java.nio.file.*;
import java.nio.file.StandardCopyOption;
import javafx.beans.property.SimpleStringProperty;

/**A class that define an operation to be done
*@version 20170102
*/
public class Operation
{
    public Operation(Path left, Path right, Action action,String message)
    {
        this.left = left;
        this.right = right;
        this.action = action;
        this.message = message;
        
        actionString = new SimpleStringProperty();
        leftString = new SimpleStringProperty();
        rightString = new SimpleStringProperty();
        directionString = new SimpleStringProperty();;
        
    }
    
    /**@return true is directory is empty
    */
    private static boolean isDirEmpty(final Path directory)
        throws IOException
    {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory))
        {
            return !dirStream.iterator().hasNext();
        }
    }
    
    /*
    public String toString()
    {
        if (action==Action.COPY_FROM_LEFT_TO_RIGHT)
        {
            return "Copy "+left+" to "+right;
        }
        else if (action==Action.COPY_FROM_RIGHT_TO_LEFT)
        {
            return "Copy "+right+" to "+left;
        }
        else if (action==Action.DELETE_LEFT)
        {
            return "Delete "+left;
        }
        else if (action==Action.DELETE_RIGHT)
        {
            return "Delete "+right;
        }
        else
        {
            return "No action";
        }
    }
    */
    /**execute the operation
    *@return false if it failed
    */
    public boolean execute()
        throws IOException
    {
        if (action==Action.COPY_FROM_LEFT_TO_RIGHT)
        {
            System.out.print("Copying "+left+" to "+right+" ... ");
            try
            {
                //create directories in case it doesn't exist
                Files.createDirectories(right.getParent());
                Files.copy(left,right,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.COPY_ATTRIBUTES);
                Files.setLastModifiedTime(right,Files.getLastModifiedTime(left));
                System.out.println("Done!");
            }
            catch(IOException e)
            {
                e.printStackTrace();
                System.out.println("ERROR!");
                return false;
            }
        }
        else if (action==Action.CREATE_FROM_LEFT_TO_RIGHT)
        {
            System.out.print("Creating "+left+" to "+right+" ... ");
            try
            {
                //create directories in case it doesn't exist
                Files.createDirectories(right.getParent());
                Files.copy(left,right,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.COPY_ATTRIBUTES);
                Files.setLastModifiedTime(right,Files.getLastModifiedTime(left));
                System.out.println("Done!");
            }
            catch(IOException e)
            {
                e.printStackTrace();
                System.out.println("ERROR!");
                return false;
            }
        }
        else if (action==Action.COPY_FROM_RIGHT_TO_LEFT)
        {
            System.out.print("Copying "+right+" to "+left+" ... ");
            try
            {
                //create directories in case it doesn't exist
                Files.createDirectories(left.getParent());
                Files.copy(right,left,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.COPY_ATTRIBUTES);
                Files.setLastModifiedTime(left,Files.getLastModifiedTime(right));
                System.out.println("Done!");
            }
            catch(IOException e)
            {
                e.printStackTrace();
                System.out.println("ERROR!");
                return false;
            }
        }
        else if (action==Action.CREATE_FROM_TO_RIGHT_LEFT)
        {
            System.out.print("Creating "+right+" to "+left+" ... ");
            try
            {
                //create directories in case it doesn't exist
                Files.createDirectories(left.getParent());
                Files.copy(right,left,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.COPY_ATTRIBUTES);
                Files.setLastModifiedTime(left,Files.getLastModifiedTime(right));
                System.out.println("Done!");
            }
            catch(IOException e)
            {
                e.printStackTrace();
                System.out.println("ERROR!");
                return false;
            }
        }
        else if (action==Action.DELETE_LEFT)
        {
            System.out.print("Deleting "+left+" ... ");
            try
            {
                Path parent = left.getParent();
                Files.deleteIfExists(left);
                if (isDirEmpty(parent))
                {
                    System.out.print(" "+parent+" is empty, also delete ... ");
                    Files.delete(parent);
                }
                System.out.println("Done!");
            }
            catch(IOException e)
            {
                e.printStackTrace();
                System.out.println("ERROR!");
                return false;
            }
        }
        else if (action==Action.DELETE_RIGHT)
        {
            System.out.print("Deleting "+right+" ... ");
            try
            {
                Path parent = right.getParent();
                Files.deleteIfExists(right);
                if (isDirEmpty(parent))
                {
                    System.out.print(" "+parent+" is empty, also delete ... ");
                    Files.delete(parent);
                }
                System.out.println("Done!");
            }
            catch(IOException e)
            {
                e.printStackTrace();
                System.out.println("ERROR!");
                return false;
            }
        }
        else
        {
            //do nothing
        }
        return true;
    }
    
    /**@return the message
    */
    public String getMessage()
    {
        return message;
    }
    
    public String getActionString()
    {
        return ""+action;
    }
    
    public String getLeftString()
    {
        return ""+left;
    }
    
    public String getRightString()
    {
        return ""+right;
    }
    
    public String getDirectionString()
    {
        if (action==Action.COPY_FROM_LEFT_TO_RIGHT)
        {
            return "-->";
        }
        else if (action==Action.CREATE_FROM_LEFT_TO_RIGHT)
        {
            return "-->";
        }
        if (action==Action.COPY_FROM_RIGHT_TO_LEFT)
        {
            return "<--";
        }
        else if (action==Action.CREATE_FROM_TO_RIGHT_LEFT)
        {
            return "<--";
        }
        else if (action==Action.DELETE_LEFT)
        {
            return "x";
        }
        else if (action==Action.DELETE_RIGHT)
        {
            return "x";
        }
        return "!";
    }
    
    /**message associated with his action
    */
    private String message;
    
    /**the action to be taken
    */
    private Action action;
    
    /**the "left" file
    */
    private Path left;
    
    /**the "right" file
    */
    private Path right;
    
    private final SimpleStringProperty actionString;
    private final SimpleStringProperty leftString;
    private final SimpleStringProperty rightString;
    private final SimpleStringProperty directionString;
    
    
    /**the set of actions
    */
    public enum Action
    {
        /**there is a conflict between left and right
        */
        CONFLICT,
    
        /**copy from left to right
        */
        COPY_FROM_LEFT_TO_RIGHT,
    
        /**create a file from left to right
        */
        CREATE_FROM_LEFT_TO_RIGHT,
        
        /**copy from right to left
        */
        COPY_FROM_RIGHT_TO_LEFT,
    
        /**create a file from right to left
        */
        CREATE_FROM_TO_RIGHT_LEFT,
    
        /**delete left
        */
        DELETE_LEFT,
    
        /**delete right
        */
        DELETE_RIGHT
    }
}