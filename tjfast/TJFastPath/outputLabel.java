import java.util.*;
import java.io.*;


public class outputLabel  {
	
	String tag;
	int [] labels;
	
	public outputLabel(){
		
	}//end outputLabel
	
	static void output (String tag,int [] labels){
		
		try{
		
    		RandomAccessFile r = new   RandomAccessFile("outputData\\"+tag,"rw");
    		
    		long fileLength = r.length();
    		r.seek(fileLength);
    		
    		r.writeByte(labels.length);
    		
    		for(int i=0;i<labels.length;i++)
    		r.writeInt(labels[i]);
    		
    		r.close();
    		
    		}catch(Exception e){
		System.out.println("e is "+e);
	
		}//end catch
    	
		
	}//end output
	
	static void outputUTF8 (String tag,int [] labels){
		
		try{
		
    		RandomAccessFile r = new   RandomAccessFile("outputData\\"+tag,"rw");
    		
    		long fileLength = r.length();
    		r.seek(fileLength);
    		
    		Vector v = new Vector();
    		int totalLength = 0;
    		
    		for(int i=0;i<labels.length;i++)
    		{
    			int outputByte [] = IntegerToUTF8(labels[i]);
    			totalLength += outputByte.length;
   			v.addElement(outputByte);
    		}//end for
    		
    		if (totalLength>250) System.out.println(" Too large label size !!!"+totalLength);
    		
    		r.writeByte(totalLength); 
    		
    		for(int i=0;i<v.size();i++)
    		{
   			int outputByte [] = (int [] )v.elementAt(i);
   			for(int j=0;j<outputByte.length;j++)
    			r.writeByte(outputByte[j]);
    		}//end for
    		
    		r.close();
    		
    		}catch(Exception e){
		System.out.println("e is "+e);
	
		}//end catch
    	
		
	}//end output
	
	
	static int [] IntegerToUTF8(int value){
		
		if (value<=127)
		{ 	int [] r = new int [1];
		  	r[0]=value;
		  	return r;
		}//end 
		else if (value<=2047)
		{	int [] r = new int [2];
			String s = decimalToBinary (value);
			if (s.length()<11)
				s = addZeroesAtFront(s,11); 
			String front = s.substring(0,5);
			String rear = s.substring(5,11);
			
		  	r[0]=binaryToDecimal("110"+front);
		  	r[1]=binaryToDecimal("10"+rear);
		  	
		 	 return r;
			}//end else
		else if (value<=65535)
		{	int [] r = new int [3];
			String s = decimalToBinary (value);
			if (s.length()<16)
				s = addZeroesAtFront(s,16); 
			String front = s.substring(0,4);
			String middle = s.substring(4,10);
			String rear = s.substring(10,16);
			
		  	r[0]=binaryToDecimal("1110"+front);
		  	r[1]=binaryToDecimal("10"+middle);
		  	r[2]=binaryToDecimal("10"+rear);
		  	
		 	 return r;
			}//end else
		else if (value<=2097151)
		{	int [] r = new int [4];
			String s = decimalToBinary (value);
			if (s.length()<21)
				s = addZeroesAtFront(s,21); 
			String front = s.substring(0,3);
			String middle0 = s.substring(3,9);
			String middle1 = s.substring(9,15);
			String rear = s.substring(15,21);
			
		  	r[0]=binaryToDecimal("11110"+front);
		  	r[1]=binaryToDecimal("10"+middle0);
		  	r[2]=binaryToDecimal("10"+middle1);
		  	r[3]=binaryToDecimal("10"+rear);
		  	
		 	 return r;
			}//end else
		else if (value<=67108863)
		{	int [] r = new int [5];
			String s = decimalToBinary (value);
			if (s.length()<26)
				s = addZeroesAtFront(s,26); 
			String front = s.substring(0,2);
			String middle0 = s.substring(2,8);
			String middle1 = s.substring(8,14);
			String middle2 = s.substring(14,20);
			String rear = s.substring(20,26);
			
		  	r[0]=binaryToDecimal("111110"+front);
		  	r[1]=binaryToDecimal("10"+middle0);
		  	r[2]=binaryToDecimal("10"+middle1);
		  	r[3]=binaryToDecimal("10"+middle2);
		  	r[4]=binaryToDecimal("10"+rear);
		  	
		 	 return r;
			}//end else
		else
		System.out.println("The integer "+value+" is too large so that it cannot be processed!");
		return null;
	}//end convertToUTF8
	
	static int UTF8ToIntegerOld (int []  bytes){
		
		if (bytes.length == 1)
		{ 	
		  	return bytes[0];
		}//end 
		else if (bytes.length == 2)
		{	String binary0 = decimalToBinary(bytes[0]);
			String binary1 = decimalToBinary(bytes[1]);
		//System.out.println("binary0 is "+bytes[0]);
			return binaryToDecimal(binary0.substring(3,8)+ binary1.substring(2,8));
		  	
		}//end else
		else if (bytes.length == 3)
		{	String binary0 = decimalToBinary(bytes[0]);
			String binary1 = decimalToBinary(bytes[1]);
			String binary2 = decimalToBinary(bytes[2]);
		
			return binaryToDecimal(binary0.substring(4,8)+ binary1.substring(2,8)+ binary2.substring(2,8));
		  	
		}//end else
		else if (bytes.length == 4)
		{	String binary0 = decimalToBinary(bytes[0]);
			String binary1 = decimalToBinary(bytes[1]);
			String binary2 = decimalToBinary(bytes[2]);
			String binary3 = decimalToBinary(bytes[3]);
		
			return binaryToDecimal(binary0.substring(5,8)+ binary1.substring(2,8)+ binary2.substring(2,8) + binary3.substring(2,8));
		  	
		}//end else
		else if (bytes.length == 5)
		{	String binary0 = decimalToBinary(bytes[0]);
			String binary1 = decimalToBinary(bytes[1]);
			String binary2 = decimalToBinary(bytes[2]);
			String binary3 = decimalToBinary(bytes[3]);
			String binary4 = decimalToBinary(bytes[4]);
		
			return binaryToDecimal(binary0.substring(6,8)+ binary1.substring(2,8)+ binary2.substring(2,8) + binary3.substring(2,8) + binary4.substring(2,8));
		  	
			}//end else
		
		return 0;
	}//end convertToUTF8 
	
	
	static int UTF8ToInteger (int []  bytes){
		
		if (bytes.length == 1)
		{ 	
		  	return bytes[0];
		}//end 
		else if (bytes.length == 2)
		{	
			bytes[0]= bytes[0] ^ 192 ;
			bytes[1]= bytes[1] ^ 128 ;
			bytes[0]= bytes[0] << 6 ;
			
			return (bytes[0] | bytes[1]);
			
		}//end else
		else if (bytes.length == 3)
		{	bytes[0]= bytes[0] ^ 224 ;
			bytes[1]= bytes[1] ^ 128 ;
			bytes[2]= bytes[2] ^ 128 ;
			
			bytes[1]= bytes[1] << 6 ;
			
			bytes[1] = bytes[1] | bytes[2];
			
			bytes[0]= bytes[0] << 12 ;
			
			
			return (bytes[0] | bytes[1]);
		}//end else
		else if (bytes.length == 4)
		{	bytes[0]= bytes[0] ^ 240 ;
			bytes[1]= bytes[1] ^ 128 ;
			bytes[2]= bytes[2] ^ 128 ;
			bytes[3]= bytes[3] ^ 128 ;
			
			bytes[2]= bytes[2] << 6 ;
			
			bytes[2] = bytes[2] | bytes[3];
			
			bytes[1]= bytes[1] << 12 ;
			
			bytes[1] = bytes[1] | bytes[2];
			
			bytes[0]= bytes[0] << 18 ;
			
			return (bytes[0] | bytes[1]);
			
		}//end else
		else if (bytes.length == 5)
		{	bytes[0]= bytes[0] ^ 248 ;
			bytes[1]= bytes[1] ^ 128 ;
			bytes[2]= bytes[2] ^ 128 ;
			bytes[3]= bytes[3] ^ 128 ;
			bytes[4]= bytes[4] ^ 128 ;
			
			bytes[3]= bytes[3] << 6 ;
			
			bytes[3] = bytes[3] | bytes[4];
			
			bytes[2]= bytes[2] << 12 ;
			
			bytes[2] = bytes[2] | bytes[3];
			
			bytes[1]= bytes[1] << 18 ;
			
			bytes[1] = bytes[1] | bytes[2];
			
			bytes[0]= bytes[0] << 24 ;
			
			return (bytes[0] | bytes[1]);
			
			}//end else
		
		return 0;
	}//end convertToUTF8
	
	static String addZeroesAtFront(String s,int targetlength ){
		
		
		
		if (s.length() > targetlength ) { System.out.println("Length Wrong!"); return  null; }
		
		if (s.length() == targetlength )  return  s;
		
		String result = s;
		
		String zero= "0";
		
		for(int i=0;i<targetlength-s.length();i++)
			
			result = zero.concat(result);
		
		return result;
		
		
		
		}//end addZeroesAtFront
		
	
	static String decimalToBinary (int decimal){
		
		
		int quotient = decimal/2;
		int remainder = decimal % 2;
		
		String s = String.valueOf(remainder);
		
		while ( quotient >0 )
		
		{	remainder = quotient % 2;
			quotient = quotient/2;
			String temp = String.valueOf(remainder);
			s = s.concat(temp);
		
		}//end while
		return  converse(s);
		
	}//end decimalToBinary
	
	static String converse (String s){
		
		String r = "";
		
		for(int i=s.length();i>0;i--)
			r = r.concat(s.substring(i-1,i));
		
		return r;
		
	}//end converse
	
	static int binaryToDecimal (String binary){
		
		byte [] b = binary.getBytes();
		double sum = 0 ;
		
		for (int i=0;i<b.length;i++)		
			if (b[i]=='1')
				sum = sum+ Math.pow(2.0, (b.length-1-i)*1.0 );
		
		return (new Double (sum)).intValue();
		
		
		
		
		
	}//end binaryToDecimal
	
	 static public void main(String[] args) throws Exception {
	 	
	 	outputLabel test = new outputLabel();
	 	int [] labels = new  int [3];
	 	labels[0]=23;
	 	labels[1]=33321;
	 	labels[2]=1233;
	 	test.outputUTF8 ("a",labels);
	 	
	 	 labels = new  int [1];
	 	labels[0]=283;
	 	test.outputUTF8 ("a",labels);
	 	 
	 	 
    }//end main

}//end class
  
  
 