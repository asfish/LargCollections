package com.axiomine.largecollections.utils;

public class GenerateWritableKeyPrimitiveValueMaps {
    public static void main(String[] args) throws Exception{        
        //String[] keys={"Text","IntWritable"};
        //String[] vals={"String","Integer","Long","Double","Float"};
        //
        String[] keys={"Text"};
        String[] vals={"String"};
        
        String myPackage = "com.axiomine.largecollections";
        String customImports = "-";
        String kPackage = "com.axiomine.largecollections.functions";
        String vPackage = "com.axiomine.largecollections.functions";
        String kClass="";
        String vClass="";
        for(String k:keys){
            kClass=k;
            for(String v:vals){
                vClass=v;
                String[] myArgs = {myPackage,customImports,kPackage,vPackage,kClass,vClass};
                GeneratorWritableKeyPrimitiveValue  .main(myArgs);
            }
        }
    }
}
