package org.go3k.highlordconf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Helper {
	private static Helper instance;
	private HelloWorldBuilder builder;
	
	public static Helper Instance() {
		if (instance == null)
			instance = new Helper();
		
		return instance;
	}
	
	public Helper() {
	}
	
	public HelloWorldBuilder getMainBuilder() {
		return builder;
	}

	public void setMainBuilder(HelloWorldBuilder builder) {
		this.builder = builder;
	}
	
    public static String Read2String(File file){
        String result = "";
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                result = result + "\n" +s;
            }
            br.close();    
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
