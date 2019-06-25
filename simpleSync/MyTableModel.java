package simpleSync;

import javax.swing.*;
import javax.swing.table.*;

/**A basic table model 
  */
public class MyTableModel extends AbstractTableModel
{
    private static final long serialVersionUID = 3246513335276446825L;
    
    /**The number of columns
      */
    private int noOfColumns;
    
    /**the array of names
      */
    private String[] names;
    
    /**the array of types
      */
    private Class<?>[] types;
    
    /**the table data, [rows][columns]
      */
    private Comparable<?>[][] data;

    
    /**Constructor, create a data model with no data
      *@param names the array of column names
      *@param types the array of class types
      */
    public MyTableModel(String[] names, Class<?>[] types)
    {
        this.names = names;
        this.types = types;
        noOfColumns = names.length;
        data = new Comparable<?>[0][noOfColumns];
    }
    
    /**Constructor
      *@param names the array of column names
      *@param types the array of class types
      *@param d the array of data
      */
    public MyTableModel(String[] names, Class<?>[] types, Comparable<?>[][] d)
    {
        this(names,types);
        int noOfRows = d.length;
        data = new Comparable<?>[noOfRows][noOfColumns];
        for(int r=0;r<noOfRows;r++)
        for(int c=0;c<noOfColumns;c++)
        {
            setData(r,c,d[r][c]);
        }
    }
    
    /**@return the column name and columnIndex
      *@param columnIndex the column index value
      */
    public String getColumnName(int columnIndex)
    {
        try
        {
            return names[columnIndex];
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            //should not have exception
            e.printStackTrace();
            return null;
        }
    }
    
    /**@return the number of columns
      */
    public int getColumnCount()
    {
        return noOfColumns;
    }
    
    /**@return the number of rows
      */
    public int getRowCount()
    {
        return data.length;
    }
    
    /**@return the value at rowIndex,columnIndex
      *@param rowIndex the row
      *@param columnIndex the column
      */
    public Comparable<?> getValueAt(int rowIndex, int columnIndex)
    {
        return data[rowIndex][columnIndex];
    } 
    
    /**Set the value at [rowIndex][columnIndex]
      *@param aValue the new value
      *@param rowIndex the row
      *@param columnIndex the column 
      */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        try
        {
            setData(rowIndex,columnIndex,(Comparable)aValue);
            fireTableCellUpdated(rowIndex,columnIndex);
        }
        catch(ClassCastException e)
        {
            e.printStackTrace();
        }
    }
    
    /**Set the value at [rowIndex][columnIndex]
      *@param aValue the new value
      *@param rowIndex the row
      *@param columnIndex the column 
      */
    public void setValueAt(Comparable<?> aValue, int rowIndex, int columnIndex)
    {
        setData(rowIndex,columnIndex,aValue);
        fireTableCellUpdated(rowIndex,columnIndex);
    }
    
    
    /**set the data at row,col, table events are not fired
      *@param row the row
      *@param col the column
      *@param d the data
      *@throws InvalidDataTypeException when there is d is of invalid type
      */
    private void setData(int row, int col, Comparable<?> d)
    {
        //check the class
        if (types[col].isInstance(d))
        {
            data[row][col] = d;
            fireTableCellUpdated(row,col);
        }
        else
        {
            //do nothing
        }
    }
    
    /**set the data, will reset all column to non editable
      *@param d the array of data
      *@throws InvalidDataTypeException when there is invalid data
      */
    public void setData(Comparable<?>[][] d)
    {
        int noOfRows = d.length;
        data = new Comparable<?>[noOfRows][noOfColumns];
        for(int row=0;row<noOfRows;row++)
        for(int col=0;col<noOfColumns;col++)
        {
            if (types[col].isInstance(d[row][col]))
            {
                data[row][col] = d[row][col];
            }
            else
            {
                //do nothing
            }
        }
        fireTableDataChanged();
    }
    
    /**Remove all data
      */
    public void clear()
    {
        //removing row will not destroy order
        int noOfRows = data.length;
        if (noOfRows>0)
        {
            data = new Comparable<?>[0][noOfColumns];
            fireTableRowsDeleted(0,noOfRows-1);
        }
        //else do nothing
    }
}