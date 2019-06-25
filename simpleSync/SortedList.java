package simpleSync;

import java.util.*;

/**a class that implements a sorted list a Vector.
  *It is sorted using the comaparable interface
  *
  *@author Tan Hong Cheong
  *@version 20090411
  */
public class SortedList<E extends Comparable<? super E>>
{
    /**Constructor
      *Create a new sorted list that allows duplicate
      */
    public SortedList()
    {
        this(true);
    }
    
    /**Constructor
      *@param allowDuplicate whether to allow duplicate
      */
    public SortedList(boolean allowDuplicate)
    {
        this.allowDuplicate = allowDuplicate;
        list = new Vector<E>();
    }
    
    /**copy constructor
    */
    public SortedList(SortedList<E> rhs)
    {
        this.allowDuplicate = rhs.allowDuplicate;
        list = new Vector<E>();
    }
    
    /**add an element to the list between start and end inclusive
    *@param ele the element
    *@param start the start pos
    *@param end the end pos
    *@return true if e is added to the list
    */
    private boolean add(E elem, int start, int end)
    {
        //System.out.println("Adding "+elem+" between "+start+" to "+end);
        if (start>=end)
        {
            //list has only one element
            E e = list.get(start);
            int result = e.compareTo(elem);
            if (result==0)
            {
                if (allowDuplicate)
                {
                    list.add(start,elem);
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if (result<0)
            {
                list.add(start+1,elem);
                return true;
            }
            else
            {
                list.add(start,elem);
                return true;
            }
        }
        else
        {
            //get the middle element
            int index = (start+end)/2;
            Comparable<? super E> e = list.get(index);
            int result = e.compareTo(elem);
            if (result==0)
            {
                if (allowDuplicate)
                {
                    list.add(index,elem);
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if (result<0)
            {
                //search the upper half
                return add(elem,index+1,end);
            }
            else
            {
                //search the lower half
                return add(elem,start,index-1);
            }
        }
    }
    
    /**add the element e to the list
      *@return true if e is added to the list
      */
    public synchronized boolean add(E e)
    {
        int size = list.size();
        if (size==0)
        {
            //no element, just add
            list.add(e);
            return true;
        }
        else
        {
            return add(e,0,list.size()-1);
        }
        /*
        int index = 0;
        while(index<list.size())
        {
            Comparable<? super E> element = list.get(index);
            int result = element.compareTo(e);
            if (result==0)
            {
                //same if allow duplicate then add
                if (allowDuplicate)
                {
                    list.add(index,e);
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if (result>0)
            {
                //elemnt is just greater than e
                //add e at current pos
                list.add(index,e);
                return true;
            }
            else
            {
                index++;
            }
        }
        //exited loop, just add at end of list
        list.add(e);
        return true;
        */
    }
    
    /**remove all elements
      */
    public synchronized void clear()
    {
        list.clear();
    }
    
    /**Tests if the specified element is a component in this vector. 
      *@return true if elem exist in the list
      */
    public synchronized boolean contains(E elem)
    {
        return list.contains(elem);
    }
    
    /**get the element at index
      *@return the component at the specified index. 
      *throws ArrayIndexOutOfBoundsException if the index is negative or not less than the current 
      *size of this list.
      */
    public synchronized E elementAt(int index) 
    {
        return list.elementAt(index);
    }
    
    /**@return the element at the specified position in this list
      *throws ArrayIndexOutOfBoundsException if the index is negative or not less than the current 
      *size of this list.
      */
    public synchronized E get(int index) 
    {
        return list.get(index);
    }
    
    /**@return the element in the list which is the same as e (using compareTo method)
      *if non is found return null
      *@param e the element to search for
      */
    public synchronized E get(E e) 
    {
        int pos = indexOf(e);
        if (pos==-1)
        {
            return null;
        }
        else
        {
            return get(pos);
        }
    }
    
    /**Binary search for an elem, using the compareTo method
      *@param elem the element to search for
      *@param start the start pos in the list
      *@param end the end pos of the list
      *@return the pos in the list if elem exist else -1
      */
    private int binarySearch(E elem, int start, int end)
    {
        if (end<start)
        {
            return -1;
        }
        if (start==end)
        {
            //list has only one element
            E e = list.get(start);
            int result = e.compareTo(elem);
            if (result==0)
            {
                return start;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            //get the middle element
            int index = (start+end)/2;
            Comparable<? super E> e = list.get(index);
            int result = e.compareTo(elem);
            if (result==0)
            {
                return index;
            }
            else if (result<0)
            {
                //search the upper half
                return binarySearch(elem,index+1,end);
            }
            else
            {
                //search the lower half
                return binarySearch(elem,start,index-1);
            }
        }
    }
    
    /**Binary search for an elem, using the compareTo method, if allow duplicate
      *the index returned may not be the first occurrence
      *@param elem the element to search for
      *@return the pos in the list if elem exist else -1
      */
    public synchronized int binarySearch(E elem)
    {
        return binarySearch(elem,0,size()-1);
    }
    
    /**Searches for the first occurence of the given argument, testing for equality using the equals 
      *method. Binary search is used
      *@param elem the element to search for
      *@return thr index of the first occurrence of the given argument, else -1
      */
    public synchronized int indexOf(E elem) 
    {
        int pos = binarySearch(elem);
        if (pos==-1)
        {
            return -1;
        }
        else if (!allowDuplicate)
        {
            return pos;
        }
        else
        {
            //element at pos may not be first occurrence if allow duplicate
            for(int i=pos-1;i>=0;i--)
            {
                Comparable<? super E> e = list.get(i);
                if (e.compareTo(elem)!=0)
                {
                    //found element that is != elem
                    //return last compared element
                    return i+1;
                }
            }
            return 0;//first element is the first occurence
        }
    }
    
    /**@return true if list is empty
      */
    public boolean isEmpty()
    {
        return list.isEmpty();
    }
 
    /**Removes the element at the specified position in this list
      *@param index the position in the list
      *throws ArrayIndexOutOfBoundsException if the index is negative or not less than the current 
      *size of this list.
      *@return the element removed
      */
    public synchronized E remove(int index)
    {
        return list.remove(index);
    }
    
    /**Removes the first occurrence of the specified element in this Vector If the Vector does not 
      *contain the element, it is unchanged.
      *@return true if the elem is removed
      *@param elem the element to be removed
      */
    public synchronized boolean removeElement(E elem) 
    {
        int index = indexOf(elem);
        if (index==-1)
        {
            return false;
        }
        else
        {
            list.remove(index);
            return true;
        }
        
    }
    
    /**remove all elements
      */
    public synchronized void removeAllElements()
    {
        list.removeAllElements();
    }
    
    /**@return the size of the list
      */
    public synchronized int size()
    {
        return list.size();
    }
    
    /**@return array list
    */
    public ArrayList<E> getArrayList()
    {
        ArrayList<E> l = new ArrayList<E>();
        for(int i=0;i<list.size();i++)
        {
            l.add(list.get(i));
        }
        return l;
    }
    
    /**whether to allow duplicate
      */
    private boolean allowDuplicate;
    
    /**the list
      */
    private Vector<E> list;
    
    public static void main(String[] args)
    {
        /*
        SortedList<String> list = new SortedList<String>();
        list.add("Class");
        list.add("Teacher");
        list.add("Room");
        for(int i=0;i<list.size();i++)
        {
            String x = list.get(i);
            System.out.println(x);
        }
        */
        SortedList<Integer> list = new SortedList<Integer>();
        
        list.add(14);
        System.out.println("Added 14");
        list.add(13);
        System.out.println("Added 13");
        list.add(12);
        System.out.println("Added 12");
        list.add(11);
        System.out.println("Added 11");
        list.add(10);
        System.out.println("Added 10");
        for(int i=0;i<list.size();i++)
        {
            int x = list.get(i);
            System.out.println(x);
        }
        
        for(int i=0;i<5;i++)
        {
            int pos = list.indexOf(i);
            System.out.println("pos = "+pos);
        }
        
        list.removeElement(11);
        
        for(int i=0;i<list.size();i++)
        {
            int x = list.get(i);
            System.out.println(x);
        }
        
    }
}