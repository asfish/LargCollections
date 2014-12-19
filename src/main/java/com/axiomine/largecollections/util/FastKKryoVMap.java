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
package com.axiomine.largecollections.util;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.iq80.leveldb.WriteBatch;

import com.google.common.base.Function;
import com.axiomine.largecollections.serdes.*;

import java.util.Random
;
import java.lang.Integer;
import java.lang.Integer;


public class FastKKryoVMap<K,V> extends LargeCollection implements   Map<K,V>, Serializable{
    public static final long               serialVersionUID = 2l;
    private transient Function<K, byte[]> keySerFunc       = null;
    private transient Function<V, byte[]> valSerFunc       = new KryoSerDes.SerFunction<V>();
    private transient Function<byte[], K> keyDeSerFunc     = null;
    private transient Function<byte[], V> valDeSerFunc     = new KryoSerDes.DeSerFunction<V>();
    private String keySerCls=null;
    private String keyDeSerCls=null;

    public FastKKryoVMap(String kSerCls,String kDeSerCls) {
        super();
        try{
            this.keySerCls = kSerCls;
            this.keyDeSerCls = kDeSerCls;
            this.keySerFunc = (Function<K, byte[]>) Class.forName(this.keySerCls).newInstance();
            this.keyDeSerFunc = (Function<byte[], K>) Class.forName(this.keyDeSerCls).newInstance();
        }
        catch(Exception ex){
            throw Throwables.propagate(ex);
        }
    }
    
    public FastKKryoVMap(String dbName,String kSerCls,String kDeSerCls) {
        super(dbName);
        try{
            this.keySerCls = kSerCls;
            this.keyDeSerCls = kDeSerCls;
            this.keySerFunc = (Function<K, byte[]>) Class.forName(this.keySerCls).newInstance();
            this.keyDeSerFunc = (Function<byte[], K>) Class.forName(this.keyDeSerCls).newInstance();
        }
        catch(Exception ex){
            throw Throwables.propagate(ex);
        }
    }
    
    public FastKKryoVMap(String dbPath, String dbName,String kSerCls,String kDeSerCls) {
        super(dbPath, dbName);
        try{
            this.keySerCls = kSerCls;
            this.keyDeSerCls = kDeSerCls;
            this.keySerFunc = (Function<K, byte[]>) Class.forName(this.keySerCls).newInstance();
            this.keyDeSerFunc = (Function<byte[], K>) Class.forName(this.keyDeSerCls).newInstance();
        }
        catch(Exception ex){
            throw Throwables.propagate(ex);
        }
    }
    
    public FastKKryoVMap(String dbPath, String dbName, int cacheSize,String kSerCls,String kDeSerCls) {
        super(dbPath, dbName, cacheSize);
        try{
            this.keySerCls = kSerCls;
            this.keyDeSerCls = kDeSerCls;
            this.keySerFunc = (Function<K, byte[]>) Class.forName(this.keySerCls).newInstance();
            this.keyDeSerFunc = (Function<byte[], K>) Class.forName(this.keyDeSerCls).newInstance();
        }
        catch(Exception ex){
            throw Throwables.propagate(ex);
        }
    }
    
    public FastKKryoVMap(String dbPath, String dbName, int cacheSize,
            int bloomFilterSize,String kSerCls,String kDeSerCls) {
        super(dbPath, dbName, cacheSize, bloomFilterSize);
        try{
            this.keySerCls = kSerCls;
            this.keyDeSerCls = kDeSerCls;
            this.keySerFunc = (Function<K, byte[]>) Class.forName(this.keySerCls).newInstance();
            this.keyDeSerFunc = (Function<byte[], K>) Class.forName(this.keyDeSerCls).newInstance();
        }
        catch(Exception ex){
            throw Throwables.propagate(ex);
        }
    }
    
    @Override
    public void optimize() {
        try {
            this.initializeBloomFilter();
            for (Entry<K, V> entry : this.entrySet()) {
                this.bloomFilter.put(entry.getKey());
            }
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
    }
    
    @Override
    public boolean containsKey(Object key) {
        byte[] valBytes = null;
        if (key != null) {
            K ki = (K) key;
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
    public V get(Object key) {
        byte[] vbytes = null;
        if (key == null) {
            return null;
        }
        K ki = (K) key;
        if (bloomFilter.mightContain(key)) {
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
    public V put(K key, V value) {
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
    public V remove(Object key) {
        V v = null;
        if (key == null)
            return v;
        if (this.size > 0 && this.bloomFilter.mightContain((Integer) key)) {
            v = this.get(key);
        }
        
        if (v != null) {
            byte[] fullKeyArr = keySerFunc.apply((K) key);
            db.delete(fullKeyArr);
            size--;
        }
        return v;
    }
    
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        try {
            WriteBatch batch = db.createWriteBatch();
            int counter = 0;
            for (Map.Entry<? extends K, ? extends V> e : m
                    .entrySet()) {
                byte[] keyArr = keySerFunc.apply(e.getKey());
                V v = null;
                K k = e.getKey();
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
            Throwables.propagate(ex);
        }
        
    }
    
    @Override
    public void clear() {
        this.clearDB();
    }
    
    /* Iterators and Collections based on this Map */
    @Override
    public Set<K> keySet() {
        return new MapKeySet<K>(this, keyDeSerFunc);
    }
    
    @Override
    public Collection<V> values() {
        return new ValueCollection<V>(this, this.getDB(),
                this.valDeSerFunc);
    }
    
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return new MapEntrySet<K, V>(this, this.keyDeSerFunc,
                this.valDeSerFunc);
    }
    

    
    /* Serialization functions go here */
    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        this.serialize(stream);
        stream.writeObject(this.keySerCls);
        stream.writeObject(this.keyDeSerCls);
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        valSerFunc       = new KryoSerDes.SerFunction<V>();
        valDeSerFunc     = new KryoSerDes.DeSerFunction<V>();
        this.deserialize(in);
        try{
            this.keySerCls = (String)in.readObject();
            this.keyDeSerCls = (String)in.readObject();
            this.keySerFunc = (Function<K, byte[]>) Class.forName(this.keySerCls).newInstance();
            this.keyDeSerFunc = (Function<byte[], K>) Class.forName(this.keyDeSerCls).newInstance();
        }
        catch(Exception ex){
            throw Throwables.propagate(ex);
        }
        
    }
    /* End of Serialization functions go here */
    
}
