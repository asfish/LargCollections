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
package com.axiomine.largecollections.kryo.serializers;

import java.io.DataOutput;

import org.apache.hadoop.io.ArrayPrimitiveWritable;
import org.apache.hadoop.io.Text;

import com.axiomine.largecollections.utilities.SerDeUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ArrayPrimitiveWritableSerializer extends Serializer<ArrayPrimitiveWritable> {
    {
        setImmutable(true);
    }

    public void write (Kryo kryo, Output output, ArrayPrimitiveWritable object) {
        byte[] ba = SerDeUtils.serializeWritable(object);
        output.writeInt(ba.length, true);
        output.write(ba);
    }

    public ArrayPrimitiveWritable read (Kryo kryo, Input input, Class<ArrayPrimitiveWritable> type) {
        int len = input.readInt(true);
        byte[] ba = input.readBytes(len);
        return (ArrayPrimitiveWritable) SerDeUtils.deserializeWritable(new ArrayPrimitiveWritable(), ba);
    }
}