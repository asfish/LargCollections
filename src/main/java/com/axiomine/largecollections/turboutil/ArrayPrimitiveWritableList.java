package com.axiomine.largecollections.turboutil;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import com.axiomine.largecollections.serdes.*;
import com.google.common.base.Throwables;
import com.google.common.primitives.Ints;
import com.axiomine.largecollections.util.*;

import org.apache.hadoop.io.*;
import org.iq80.leveldb.DBIterator;

public class ArrayPrimitiveWritableList extends LargeCollection implements List<ArrayPrimitiveWritable>, Serializable {
    public static final long               serialVersionUID = 2l;
    private transient TurboSerializer<ArrayPrimitiveWritable> tSerFunc       =  new WritableSerDes.ArrayPrimitiveWritableSerFunction();    
    private transient TurboDeSerializer<ArrayPrimitiveWritable> tDeSerFunc  = new WritableSerDes.ArrayPrimitiveWritableDeSerFunction();    
    

    
    public ArrayPrimitiveWritableList() {
        super();
    }
    
    public ArrayPrimitiveWritableList(String dbName) {
        super(dbName);
    }
    
    public ArrayPrimitiveWritableList(String dbPath,String dbName) {
        super(dbPath, dbName);
    }
    
    public ArrayPrimitiveWritableList(String dbPath,String dbName,int cacheSize) {
        super(dbPath, dbName, cacheSize);
    }
    
    public ArrayPrimitiveWritableList(String dbPath,String dbName,int cacheSize,int bloomFilterSize) {
        super(dbPath, dbName, cacheSize, bloomFilterSize);
    }


    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return (this.size==0);
    }

    @Override
    public boolean contains(Object o) {
        if (o != null) {
            return this.bloomFilter.mightContain(o);        
        }
        return false;
    }

    @Override
    public Iterator<ArrayPrimitiveWritable> iterator() {
        // TODO Auto-generated method stub
        return new MapValueIterator<ArrayPrimitiveWritable>(this.getDB(),this.tDeSerFunc);
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T>  T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(ArrayPrimitiveWritable e) {
        if (e == null)
            return false;
        else{
            byte[] fullKeyArr = Ints.toByteArray(size);
            byte[] fullValArr = this.tSerFunc.apply(e);
            db.put(fullKeyArr, fullValArr);
            this.bloomFilter.put(e);
            this.size++;
        }
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if(c.size()==0){//Optimization. Do not traverse the whole list for empty collection
            return true;
        }
        boolean mightContain=true;
        for(Object e:c){
            mightContain = this.bloomFilter.mightContain(c);
            if(!mightContain){
                break;
            }
        }
        if(!mightContain){
            return false;
        }
        
        Iterator<ArrayPrimitiveWritable> iter = this.iterator();
        while(iter.hasNext()){
            ArrayPrimitiveWritable e = iter.next();
            if(c.contains(e)){
                while(true){
                    boolean removed = c.remove(e);
                    if(!removed)
                        break;//All instances removed
                }
            }
            else{
                break;
            }
        }
        return (c.size()==0);
    }

    @Override
    public boolean addAll(Collection<? extends ArrayPrimitiveWritable> c) {
        // TODO Auto-generated method stub
        if(c.size()==0){
            return false;
        }
        else{
            for(ArrayPrimitiveWritable e:c){
                this.add(e);
            }
            return true;
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends ArrayPrimitiveWritable> c) {
        throw new UnsupportedOperationException("can only use addAll(Collection<? extends T> c)");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        this.clearDB();
    }

    @Override
    public ArrayPrimitiveWritable get(int index) {
        if(index<0 || index>=this.size()){
            throw new IndexOutOfBoundsException();
        }
        byte[] vbytes = db.get(Ints.toByteArray(index));
        if (vbytes == null) {
            return null;
        } else {
            return this.tDeSerFunc.apply(vbytes);
        }
    }

    @Override
    public ArrayPrimitiveWritable set(int index, ArrayPrimitiveWritable element) {
        if(index<0 || index>=this.size()){
            throw new IndexOutOfBoundsException();
        }
        byte[] fullKeyArr = Ints.toByteArray(index);
        byte[] fullValArr = this.tSerFunc.apply(element);
        db.put(fullKeyArr, fullValArr);
        this.bloomFilter.put(element);
        return element;
    }

    @Override
    public void add(int index, ArrayPrimitiveWritable element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayPrimitiveWritable remove(int index) {
        if(index<0 || index!=(this.size-1)){
            throw new IndexOutOfBoundsException("Can only remove the last element");
        }
        ArrayPrimitiveWritable e = this.get(index);
        db.delete(Ints.toByteArray(index));
        size--;
        return e;
    }

    @Override
    public int indexOf(Object o) {
        int index = -1;
        int myIndex = -1;
        Iterator<ArrayPrimitiveWritable> iter = this.iterator();
        while(iter.hasNext()){
            index++;
            ArrayPrimitiveWritable e = iter.next();            
            if(e.equals(o)){
                myIndex=index;
                break;
            }
        }
        return myIndex;
    }

    @Override
    public int lastIndexOf(Object o) {
        int index = -1;
        int myIndex = -1;
        Iterator<ArrayPrimitiveWritable> iter = this.iterator();
        while(iter.hasNext()){
            index++;
            ArrayPrimitiveWritable e = iter.next();            
            if(e.equals(o)){
                myIndex=index;
            }
        }
        return myIndex;

    }

    @Override
    public ListIterator<ArrayPrimitiveWritable> listIterator() {
        return new MyListIterator<ArrayPrimitiveWritable>(this,this.tSerFunc,this.tDeSerFunc);
    }

    @Override
    public ListIterator<ArrayPrimitiveWritable> listIterator(int index) {
        return new MyListIterator<ArrayPrimitiveWritable>(this,this.tSerFunc,this.tDeSerFunc,index);
    }

    @Override
    public List<ArrayPrimitiveWritable> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void optimize() {
        DBIterator iterator = this.getDB().iterator();
        try {
            this.initializeBloomFilter();            
            while(iterator.hasNext()){
                this.bloomFilter.put(tDeSerFunc.apply(iterator.next().getValue()));
            }
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
        finally{
            try{
                iterator.close();    
            }
            catch(Exception ex){
                throw Throwables.propagate(ex);
            }            
        }
        
    }
    
    
    /* Serialization functions go here */
    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        this.serialize(stream);
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        this.deserialize(in);
        tSerFunc       =  new WritableSerDes.ArrayPrimitiveWritableSerFunction();    
        tDeSerFunc  = new WritableSerDes.ArrayPrimitiveWritableDeSerFunction();    
    }
    /* End of Serialization functions go here */
    
}
