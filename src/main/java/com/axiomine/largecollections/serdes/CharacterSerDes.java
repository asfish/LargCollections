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
package com.axiomine.largecollections.serdes;


public class CharacterSerDes {
    public static class SerFunction implements TurboSerializer<Character>{
        private static final long serialVersionUID = 2L;

        public byte[] apply(Character arg) {  
            if(arg==null){
                return null;
            }
            else{
                char c = arg;
                byte[] bytes = new byte[1*2];
                bytes[0*2] = (byte) (arg >> 8);
                bytes[0*2+1] = (byte) c;
                return bytes;
            }
        }    
    }
    
    public static class DeSerFunction implements TurboDeSerializer<Character>{
        private static final long serialVersionUID = 2L;
        public Character apply(byte[] arg) {
            if(arg==null){
                return null;
            }
            else{
                char c = (char) ((arg[0*2] << 8) + (arg[0*2+1] & 0xFF));
                return c;
            }
        }    
    }

}
