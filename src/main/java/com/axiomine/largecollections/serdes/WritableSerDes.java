package com.axiomine.largecollections.serdes;

import org.apache.hadoop.io.ArrayPrimitiveWritable;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.ByteWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.ShortWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.axiomine.largecollections.utilities.*;
import com.google.common.base.Function;
import com.google.common.base.Throwables;

public class WritableSerDes {
    public static class SerFunction implements TurboSerializer<Writable>{
        private static final long serialVersionUID = 12L;
        public byte[] apply(Writable arg) {
            if (arg == null) {
                return null;
            }
            else{
                return SerDeUtils.serializeWritable(arg);
            }
        }    
    }
    
    public static class DeSerFunction implements TurboDeSerializer<Writable>{
        private static final long serialVersionUID = 12L;
        private Class<? extends Writable> writableCls = null; 
        public DeSerFunction(Class<? extends Writable> wCls){
            this.writableCls = wCls;
        }
        public DeSerFunction(String sCls){
            try{
                this.writableCls = (Class<? extends Writable>) Class.forName(sCls);    
            }
            catch(Exception ex){
                throw Throwables.propagate(ex);
            }
            
        }
        public Writable apply(byte[] arg) {
            if (arg == null) {
                return null;
            }
            else{
                Writable w=null;
                try {
                    w = this.writableCls.newInstance();
                    return SerDeUtils.deserializeWritable(w,arg);
                } catch (InstantiationException ex) {
                    Throwables.propagate(ex);
                }   
                catch(IllegalAccessException ex){
                    Throwables.propagate(ex);
                }
            
                return null;
                
            }
        }    

    }
    
    
    public static class IntWritableDeSerFunction implements TurboDeSerializer<IntWritable>{
        public IntWritable apply(byte[] arg) {
            if (arg == null) {
                return null;
            }
            else{
                IntWritable w = new IntWritable();
                return (IntWritable) SerDeUtils.deserializeWritable(w, arg);
            }
        }    
    }
    public static class ArrayPrimitiveWritableDeSerFunction implements TurboDeSerializer<ArrayPrimitiveWritable>{
        public ArrayPrimitiveWritable apply(byte[] arg) {
            if (arg == null) {
                return null;
            }
            else{
                ArrayPrimitiveWritable w = new ArrayPrimitiveWritable();
                return (ArrayPrimitiveWritable) SerDeUtils.deserializeWritable(w, arg);
            }
        }    
    }

    public static class BooleanWritableDeSerFunction implements TurboDeSerializer<BooleanWritable>{
        public BooleanWritable apply(byte[] arg) {
            if (arg == null) {
                return null;
            }
            else{
                BooleanWritable w = new BooleanWritable();
                return (BooleanWritable) SerDeUtils.deserializeWritable(w, arg);
            }
        }    
    }

    public static class BytesWritableDeSerFunction implements TurboDeSerializer<BytesWritable>{
        public BytesWritable apply(byte[] arg) {
            if (arg == null) {
                return null;
            }
            else{
                BytesWritable w = new BytesWritable();
                return (BytesWritable) SerDeUtils.deserializeWritable(w, arg);
            }
        }    
    }

    public static class ByteWritableDeSerFunction implements TurboDeSerializer<ByteWritable>{
        public ByteWritable apply(byte[] arg) {
            if (arg == null) {
                return null;
            }
            else{
                ByteWritable w = new ByteWritable();
                return (ByteWritable) SerDeUtils.deserializeWritable(w, arg);
            }
        }    
    }

    public static class DoubleWritableDeSerFunction implements TurboDeSerializer<DoubleWritable>{
        public DoubleWritable apply(byte[] arg) {
            if (arg == null) {
                return null;
            }
            else{
                DoubleWritable w = new DoubleWritable();
                return (DoubleWritable) SerDeUtils.deserializeWritable(w, arg);
            }
        }    
    }
    public static class FloatWritableDeSerFunction implements TurboDeSerializer<FloatWritable>{
        public FloatWritable apply(byte[] arg) {
            if (arg == null) {
                return null;
            }
            else{
                FloatWritable w = new FloatWritable();
                return (FloatWritable) SerDeUtils.deserializeWritable(w, arg);
            }
        }    
    }

    public static class LongWritableDeSerFunction implements TurboDeSerializer<LongWritable>{
        public LongWritable apply(byte[] arg) {
            if (arg == null) {
                return null;
            }
            else{
                LongWritable w = new LongWritable();
                return (LongWritable) SerDeUtils.deserializeWritable(w, arg);
            }
        }    
    }
    
    public static class MapWritableDeSerFunction implements TurboDeSerializer<MapWritable>{
        public MapWritable apply(byte[] arg) {
            if (arg == null) {
                return null;
            }
            else{
                MapWritable w = new MapWritable();
                return (MapWritable) SerDeUtils.deserializeWritable(w, arg);
            }
        }    
    }

    public static class ShortWritableDeSerFunction implements TurboDeSerializer<ShortWritable>{
        public ShortWritable apply(byte[] arg) {
            if (arg == null) {
                return null;
            }
            else{
                ShortWritable w = new ShortWritable();
                return (ShortWritable) SerDeUtils.deserializeWritable(w, arg);
            }
        }    
    }

    public static class TextDeSerFunction implements TurboDeSerializer<Text>{
        public Text apply(byte[] arg) {
            if (arg == null) {
                return null;
            }
            else{
                Text w = new Text();
                return (Text) SerDeUtils.deserializeWritable(w, arg);
            }
        }    
    }
    
}
