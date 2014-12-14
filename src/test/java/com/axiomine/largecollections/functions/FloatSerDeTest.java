package com.axiomine.largecollections.functions;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

import com.axiomine.largecollections.functions.FloatSerDe;

public class FloatSerDeTest {
    
    @Test
    public void test() {
        FloatSerDe.SerFunction ser = new FloatSerDe.SerFunction();
        FloatSerDe.DeSerFunction deser = new FloatSerDe.DeSerFunction();
        
        Float f = 1f;
        byte[] ba = ser.apply(f);
        Float ff = deser.apply(ba);
        Assert.assertEquals(f, ff);
    }
    
}
