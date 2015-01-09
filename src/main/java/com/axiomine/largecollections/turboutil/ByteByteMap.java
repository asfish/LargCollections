/*
 * Copyright 2014 Axiomine
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.axiomine.largecollections.turboutil;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.iq80.leveldb.WriteBatch;

import com.google.common.base.Function;


import com.axiomine.largecollections.utilities.*;
import com.axiomine.largecollections.util.*;
import com.axiomine.largecollections.*;
import com.axiomine.largecollections.serdes.*;
import com.axiomine.largecollections.kryo.serializers.*;



public class ByteByteMap extends LargeCollection implements   Map<Byte,Byte>, Serializable{
    public static final long               serialVersionUID = 2l;
    private transient TurboSerializer<Byte> keySerFunc       = new ByteSerDes.SerFunction();
    private transient TurboSerializer<Byte> valSerFunc       = new ByteSerDes.SerFunction();
    private transient TurboDeSerializer<Byte> keyDeSerFunc     = new ByteSerDes.DeSerFunction();
    private transient TurboDeSerializer<Byte> valDeSerFunc     = new ByteSerDes.DeSerFunction();
    
    public ByteByteMap() {
        super();
    }
    
    public ByteByteMap(String dbName) {
        super(dbName);
    }
    
    public ByteByteMap(String dbPath, String dbName) {
        super(dbPath, dbName);
    }
    
    public ByteByteMap(String dbPath, String dbName, int cacheSize) {
        super(dbPath, dbName, cacheSize);
    }
    
    public ByteByteMap(String dbPath, String dbName, int cacheSize,
            int bloomFilterSize) {
        super(dbPath, dbName, cacheSize, bloomFilterSize);
    }
    
    @Override
    public void optimize() {
        MapKeySet<Byte> keys = new MapKeySet<Byte>(this, keyDeSerFunc);
        try {
            this.initializeBloomFilter();
            for (Byte entry : keys) {
                this.bloomFilter.put(entry);
            }
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
        finally{
            if(keys!=null){
                try{
                    keys.close();
                }
                catch(Exception ex){
                    throw Throwables.propagate(ex);
                }                
            }
        }
    }
    
    @Override
    public boolean containsKey(Object key) {
        byte[] valBytes = null;
        if (key != null) {
            Byte ki = (Byte) key;
            if (this.bloomFilter.mightContain(ki)) {
                byte[] keyBytes = keySerFunc.apply(ki);
                valBytes = db.get(keyBytes);
            }
        }
        return valBytes != null;
    }
    
    @Override
    public boolean containsValue(Object value) {
        // Will be very slow unless a seperate DB is maintained with values as
        // keys
        throw new UnsupportedOperationException();
        
    }
    
    @Override
    public Byte get(Object key) {
        byte[] vbytes = null;
        if (key == null) {
            return null;
        }
        Byte ki = (Byte) key;
        if (bloomFilter.mightContain((Byte) key)) {
            vbytes = db.get(keySerFunc.apply(ki));
            if (vbytes == null) {
                return null;
            } else {
                return valDeSerFunc.apply(vbytes);
            }
        } else {
            return null;
        }
        
    }
    
    @Override
    public int size() {
        return (int) size;
    }
    
    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    /* Putting null values is not allowed for this map */
    @Override
    public Byte put(Byte key, Byte value) {
        if (key == null)
            return null;
        if (value == null)// Do not add null key or value
            return null;
        byte[] fullKeyArr = keySerFunc.apply(key);
        byte[] fullValArr = valSerFunc.apply(value);
        if (!this.containsKey(key)) {
            bloomFilter.put(key);
            db.put(fullKeyArr, fullValArr);
            size++;
        } else {
            db.put(fullKeyArr, fullValArr);
        }
        return value;
    }
    
    @Override
    public Byte remove(Object key) {
        Byte v = null;
        if (key == null)
            return v;
        if (this.size > 0 && this.bloomFilter.mightContain((Byte) key)) {
            v = this.get(key);
        }
        
        if (v != null) {
            byte[] fullKeyArr = keySerFunc.apply((Byte) key);
            db.delete(fullKeyArr);
            size--;
        }
        return v;
    }
    
    @Override
    public void putAll(Map<? extends Byte, ? extends Byte> m) {
        try {
            WriteBatch batch = db.createWriteBatch();
            int counter = 0;
            for (Map.Entry<? extends Byte, ? extends Byte> e : m
                    .entrySet()) {
                byte[] keyArr = keySerFunc.apply(e.getKey());
                Byte v = null;
                Byte k = e.getKey();
                if (this.size > 0 && this.bloomFilter.mightContain(k)) {
                    v = this.get(k);
                }
                if (v == null) {
                    bloomFilter.put(k);
                    this.size++;
                }
                batch.put(keyArr, valSerFunc.apply(e.getValue()));
                counter++;
                if (counter % 1000 == 0) {
                    db.write(batch);
                    batch.close();
                    batch = db.createWriteBatch();
                }
            }
            db.write(batch);
            batch.close();
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
        
    }
    
    @Override
    public void clear() {
        this.clearDB();
    }
    
    /* Iterators and Collections based on this Map */
    @Override
    public Set<Byte> keySet() {
        return new MapKeySet<Byte>(this, keyDeSerFunc);
    }
    
    @Override
    public Collection<Byte> values() {
        return new ValueCollection<Byte>(this, this.getDB(),
                this.valDeSerFunc);
    }
    
    @Override
    public Set<java.util.Map.Entry<Byte, Byte>> entrySet() {
        return new MapEntrySet<Byte, Byte>(this, this.keyDeSerFunc,
                this.valDeSerFunc);
    }
    

    
    /* Serialization functions go here */
    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        this.serialize(stream);
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        keySerFunc = new ByteSerDes.SerFunction();
        valSerFunc = new ByteSerDes.SerFunction();
        keyDeSerFunc = new ByteSerDes.DeSerFunction();
        valDeSerFunc = new ByteSerDes.DeSerFunction();
        this.deserialize(in);
    }
    /* End of Serialization functions go here */
    
}
