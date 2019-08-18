package module;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class UserList{

	private long[] data=new long[10];
	private int size;

	public long get(int index){
		rangeCheck(index);
		return data[index];
	}
	public long set(int index,long element){
		rangeCheck(index);
		long oldValue=data[index];
		data[index]=element;
		return oldValue;
	}
	public boolean add(long e){
		ensureCapacityInternal(size+1); // Increments modCount!!
		data[size++]=e;
		return true;
	}
	public void add(int index,long element){
		rangeCheckForAdd(index);

		ensureCapacityInternal(size+1); // Increments modCount!!
		System.arraycopy(data,index,data,index+1,size-index);
		data[index]=element;
		size++;
	}
	private void rangeCheckForAdd(int index){
		if(index>size||index<0)
			throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	}
	private String outOfBoundsMsg(int index){
		return "Index: "+index+", Size: "+size;
	}
	private void rangeCheck(int index){
		if(index>=size)
			throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	}
	public long remove(int index){
		rangeCheck(index);
		long oldValue=data[index];
		int numMoved=size-index-1;
		if(numMoved>0) System.arraycopy(data,index+1,data,index,numMoved);
		data[--size]=0;
		return oldValue;
	}
	public boolean remove(long o){
		for(int index=0;index<size;index++)
			if(o==data[index]){
				fastRemove(index);
				return true;
			}
		return false;
	}
	private void fastRemove(int index){
		int numMoved=size-index-1;
		if(numMoved>0) System.arraycopy(data,index+1,data,index,numMoved);
		data[--size]=0; // clear to let GC do its work
	}
	public void clear(){
		for(int i=0;i<size;i++)
			data[i]=0;
		size=0;
	}
	private void ensureCapacityInternal(int minCapacity){
		if(minCapacity-data.length>0) grow(minCapacity);
	}
	public int indexOf(long k){
		for(int i=0;i<size;i++){
			if(data[i]==k) return i;
		}
		return -1;
	}
	public int lastIndexOf(long k){
		for(int i=size-1;i>=0;i--){
			if(data[i]==k) return i;
		}
		return -1;
	}
	public Object clone(){
		try{
			UserList v=(UserList) super.clone();
			v.data=Arrays.copyOf(data,size);
			return v;
		}catch(CloneNotSupportedException e){
			throw new InternalError(e);
		}
	}
	public boolean contains(long k){
		return indexOf(k)>=0;
	}
	public boolean isEmpty(){
		return size==0;
	}
	public int size(){
		return size;
	}
	private void grow(int minCapacity){
		int oldCapacity=data.length;
		int newCapacity=oldCapacity+(oldCapacity>>1);
		if(newCapacity-minCapacity<0)
			newCapacity=minCapacity;
		if(newCapacity-Integer.MAX_VALUE>0){
			if(minCapacity<0) throw new OutOfMemoryError();
			newCapacity=Integer.MAX_VALUE;
		}
		data=Arrays.copyOf(data,newCapacity);
	}
	public int hashCode() {
		int hash=0;
		for(int i=0;i<size;i++)hash+=data[i];
		return hash;
	}
	public void readFromFile(String f)throws IOException{
		try{
			FileInputStream fis=new FileInputStream(f);
			BufferedInputStream bis=new BufferedInputStream(fis);
			DataInputStream dis=new DataInputStream(bis);
			try{
				size=dis.readInt();
				data=new long[size];
				for(int i=0;i<size;i++)data[i]=dis.readLong();
			}finally {
				try{
					dis.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
	}
	public void writeToFile(String f)throws IOException{
		try{
			FileOutputStream fos=new FileOutputStream(f);
			BufferedOutputStream bos=new BufferedOutputStream(fos);
			DataOutputStream dos=new DataOutputStream(bos);
			try{
				dos.writeInt(size);
				for(int i=0;i<size;i++)dos.writeLong(data[i]);
			}finally {
				try{
					bos.flush();
				}catch(IOException e){
					e.printStackTrace();
				}
				try{
					dos.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
	}
}
