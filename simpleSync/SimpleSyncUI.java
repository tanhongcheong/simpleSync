package simpleSync;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.net.*;
import javax.swing.table.*;

public class SimpleSyncUI extends JFrame
{
    private static final long serialVersionUID = 9002554424450280236L;
    
    /**the list of operations
    */
    private ArrayList<Operation> operations;
    
    /**the input file
    */
    private Path input;
    
    /**the left folder
    */
    private Path leftDir;
    
    /**the right folder
    */
    private Path rightDir;
    
    /**the table model
    */
    private MyTableModel tableModel;
    
    /**the table
    */
    private JTable table;
    
    /**the file system
    */
    private FileSystem fs;
    
    /**the execute menu
    */
    private JMenu executeMenu;
    
    /**the progress bar
    */
    private JProgressBar progressBar;
    
    public SimpleSyncUI()
    {
        fs = FileSystems.getDefault();
        init();
        initMenuBar();
    }
    
    /**initialize the frame
    */
    private void init()
    {
        setTitle("Simple Sync");
        
        
        //set the default size to 800 by 600
        //so that when the user press restore, it is not minimized
        this.setSize(800,600);
        this.setResizable(true);
        
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        //maximize the frame
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        //show the frames
        this.setVisible(true);
        
        
        Container pane = this.getContentPane();
        pane.setLayout(new BorderLayout());

        JPanel southPanel = new JPanel(new BorderLayout());
        pane.add(southPanel,BorderLayout.SOUTH);
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        southPanel.add(progressBar,BorderLayout.CENTER);
        
        
        String[] headers = {"Operation","Left","Direction","Right"};
        Class<?>[] types = {String.class,String.class,String.class,String.class,};
        Comparable<?>[][] data = new Comparable<?>[0][4];
        
        tableModel = new MyTableModel(headers,types,data);
        
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        pane.add(new JScrollPane(table),BorderLayout.CENTER);
    }
    
    /**set the operations
    *@param operations the list of operations
    */
    public void setOperations(ArrayList<Operation> operations)
    {
        this.operations = operations;
        Comparable<?>[][] data = new Comparable<?>[operations.size()][4];
        for(int i=0;i<operations.size();i++)
        {
            Operation operation = operations.get(i);
            data[i][0] = operation.getActionString();
            data[i][1] = operation.getLeftString();
            data[i][2] = operation.getDirectionString();
            data[i][3] = operation.getRightString();
        }
        tableModel.clear();
        tableModel.setData(data);
        table.setRowSorter(new TableRowSorter<TableModel>(tableModel));
        if (operations.size()>0)
        {
            executeMenu.setEnabled(true);
        }
    }
    
    /**Method to initialize the menu bar
      */
    private void initMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);
        
        JMenuItem newMenuItem = new JMenuItem("New",KeyEvent.VK_N);
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.CTRL_MASK));
        newMenuItem.addActionListener(e->newFile());
        fileMenu.add(newMenuItem);
        
        
        JMenuItem openMenuItem = new JMenuItem("Open",KeyEvent.VK_O);
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.CTRL_MASK));
        openMenuItem.addActionListener(e->openFile());
        fileMenu.add(openMenuItem);
        
        executeMenu = new JMenu("Execute");
        menuBar.add(executeMenu);
        JMenuItem runMenuItem = new JMenuItem("Run",KeyEvent.VK_R);
        runMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,ActionEvent.CTRL_MASK));
        runMenuItem.addActionListener(e->execute());
        executeMenu.add(runMenuItem);
        executeMenu.setEnabled(false);
    }
    
    private void execute()
    {
        executeMenu.setEnabled(false);
        ExecuteThread thread = new ExecuteThread(this);
        thread.start();
    }
    
    private void newFile()
    {
        Path leftDirTemp = null;
        Path rightDirTemp = null;
        Path inputTemp = null;
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select left folder");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setMultiSelectionEnabled(false);
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File f = chooser.getSelectedFile().getAbsoluteFile();
                leftDirTemp = f.toPath();
            }
            else
            {
                return;
            }
        }
        
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select right folder");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setMultiSelectionEnabled(false);
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File f = chooser.getSelectedFile().getAbsoluteFile();
                rightDirTemp = f.toPath();
            }
            else
            {
                return;
            }
        }
        
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("Simple Sync data file","ss_file");
            chooser.addChoosableFileFilter(filter);
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setMultiSelectionEnabled(false);
            chooser.setAcceptAllFileFilterUsed(false);
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File file = chooser.getSelectedFile().getAbsoluteFile();
                inputTemp = file.toPath();
            }
            else
            {
                return;
            }
        }
        //reach here means all three things had been chosen
        System.out.println("New File");
        System.out.println("\tLeftDir="+leftDirTemp);
        System.out.println("\tRightDir="+rightDirTemp);
        System.out.println("\tinput="+inputTemp);
        leftDir = leftDirTemp;
        rightDir = rightDirTemp;
        input = inputTemp;
        
        NewThread thread = new NewThread(this);
        thread.start();
    }
    
    public void save()
    {
        try
        {
            progressBar.setIndeterminate(true);
            progressBar.setString("Saving ...");
            String url = "jar:"+input.toUri();
            URI uri = URI.create(url);
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            FileSystem zipFileSystem = FileSystems.newFileSystem(uri, env);

            Path document = zipFileSystem.getPath("/db.txt");
            BufferedWriter writer = Files.newBufferedWriter(document,java.nio.charset.StandardCharsets.UTF_16);
            
            ArrayList<Path> leftPaths = new ArrayList<Path>();
            Utilities.parseDir(Files.newDirectoryStream(leftDir),leftPaths);
            ArrayList<Path> rightPaths = new ArrayList<Path>();
            Utilities.parseDir(Files.newDirectoryStream(rightDir),rightPaths);
            
            //write the left
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(leftPaths.size()+rightPaths.size());
            
            writer.write("<"+leftDir+">");
            writer.newLine();
            for(int i=0;i<leftPaths.size();i++)
            {
                progressBar.setValue(i);
                Path path = leftPaths.get(i);
                try
                {
                    String entry = path.toString();
                    entry = entry.replace(leftDir.toString(),"");
                    String lastModified = ""+Files.getLastModifiedTime(path).toMillis();
                    writer.write(entry);
                    writer.newLine();
                    writer.write(lastModified);
                    writer.newLine();
                }
                catch(Exception e)
                {
                    //hmmm seem that after delete the dir still have reference to it and hence access denied here
                }
            }
           
            //write the right
            writer.write("<"+rightDir+">");
            writer.newLine();
            for(int i=0;i<rightPaths.size();i++)
            {
                progressBar.setValue(i+leftPaths.size());
                Path path = rightPaths.get(i);
                try
                {
                    String entry = path.toString();
                    entry = entry.replace(rightDir.toString(),"");
                    String lastModified = ""+Files.getLastModifiedTime(path).toMillis();
                    writer.write(entry);
                    writer.newLine();
                    writer.write(lastModified);
                    writer.newLine();
                }
                catch(Exception e)
                {
                    //hmmm seem that after delete the dir still have reference to it and hence access denied here
                }
            }
            
            writer.close();
            zipFileSystem.close();
            
            progressBar.setString("");
            progressBar.setValue(0);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private void openFile()
    {
        //try
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("Simple Sync data file","ss_file");
            chooser.addChoosableFileFilter(filter);
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setMultiSelectionEnabled(false);
            chooser.setAcceptAllFileFilterUsed(false);
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File file = chooser.getSelectedFile().getAbsoluteFile();
                System.out.println("f="+file);
                input = file.toPath();
                PreviewThread thread = new PreviewThread(this);
                thread.start();
            }
            executeMenu.setEnabled(false);
        }
    }
    
    /**@return the left dir
    */
    public Path getLeftDir()
    {
        return leftDir;
    }
    
    /**set the left dir
    *@param leftDir the left dir
    */
    public void setLeftDir(Path leftDir)
    {
        this.leftDir = leftDir;
    }
    
    /**@return the right dir
    */
    public Path getRightDir()
    {
        return rightDir;
    }
    
    /**set the right dir
    *@param rightDir the right dir
    */
    public void setRightDir(Path rightDir)
    {
        this.rightDir = rightDir;
    }
    
    /**@return the input file
    */
    public Path getInput()
    {
        return input;
        
    }
    /**@return the progress bar
    */
    public JProgressBar getProgressBar()
    {
        return progressBar;
    }
    
    /**@return the list of operation
    */
    public ArrayList<Operation> getOperations()
    {
        return operations;
    }
    
    /**@return the file system
    */
    public FileSystem getFileSystem()
    {
        return fs;
    }
    
    public static void main(String[] args)
    {
        try
        {
            SimpleSyncUI ui = new SimpleSyncUI();
            ui.setVisible(true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}