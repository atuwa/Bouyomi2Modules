package module;

import java.util.Arrays;

/**@see java.util.ArrayList*/
@SuppressWarnings("unused")
public class UserIntegerMap{

	private long[] key;
	private int[] data;
	private int size;

	public int indexOf(long k){
		for(int i=0;i<size;i++){
			if(key[i]==k) return i;
		}
		return -1;
	}
	public int lastIndexOf(long k){
		for(int i=size-1;i>=0;i--){
			if(key[i]==k) return i;
		}
		return -1;
	}
    public Object clone() {
        try {
        	UserIntegerMap v = (UserIntegerMap) super.clone();
            v.key = Arrays.copyOf(key, size);
            v.data = Arrays.copyOf(data,size);
            return v;
        } catch (CloneNotSupportedException e) {
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
		int oldCapacity=key.length;
		int newCapacity=oldCapacity+(oldCapacity>>1);
		if(newCapacity-minCapacity<0)
			newCapacity=minCapacity;
		if(newCapacity-Integer.MAX_VALUE>0){
			if(minCapacity<0) throw new OutOfMemoryError();
			newCapacity=Integer.MAX_VALUE;
		}
		key=Arrays.copyOf(key,newCapacity);
		oldCapacity=data.length;
		newCapacity=oldCapacity+(oldCapacity>>1);
		if(newCapacity-minCapacity<0)
			newCapacity=minCapacity;
		if(newCapacity-Integer.MAX_VALUE>0){
			if(minCapacity<0) throw new OutOfMemoryError();
			newCapacity=Integer.MAX_VALUE;
		}
		data=Arrays.copyOf(data,newCapacity);
	}
}
