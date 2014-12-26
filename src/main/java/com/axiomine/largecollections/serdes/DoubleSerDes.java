package com.axiomine.largecollections.serdes;

import com.google.common.primitives.Longs;

public class DoubleSerDes {
    public static class SerFunction implements TurboSerializer<Double>{
        private static final long serialVersionUID = 3L;

        public byte[] apply(Double arg) {
            if(arg==null){
                return null;
            }
            else{
                byte [] bytes = Longs.toByteArray(Double.doubleToLongBits(arg));
                return bytes;
            }
        }    
    }

    
    public static class DeSerFunction implements TurboDeSerializer<Double>{
        private static final long serialVersionUID = 3L;
        public Double apply(byte[] arg) {
            if(arg==null){
                return null;
            }
            else{
                return Double.longBitsToDouble(Longs.fromByteArray(arg));    
            }
        }    
    }

}
