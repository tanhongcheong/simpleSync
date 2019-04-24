package simpleSync;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/**A class that provides utilities functions
*/
public class Utilities
{
    /**parse a directory
    *@param dir
    *@param list the list of paths in the directory
    */
    public static void parseDir(DirectoryStream<Path> dirStream,List<Path> paths)
        throws IOException
    {
        for (Path file:dirStream)
        {
            if (Files.isDirectory(file))
            {
                parseDir(Files.newDirectoryStream(file),paths);
            }
            else
            {
                paths.add(file);
            }
        }
    }
}
