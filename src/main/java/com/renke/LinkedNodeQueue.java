package com.renke;
import com.renke.util.MyUnsafe;

import sun.misc.Unsafe;

public class LinkedNodeQueue{
	private static final long headOffset;
    private static final long tailOffset;
    private static final long sizeOffset;
    private volatile int size;
    private LinkedNode head;
    private LinkedNode tail;
    private static final Unsafe unsafe = MyUnsafe.getUnsafe();
    
    static{
		try {
			headOffset = unsafe.objectFieldOffset
			        (LinkedNodeQueue.class.getDeclaredField("head"));
			tailOffset = unsafe.objectFieldOffset
					(LinkedNodeQueue.class.getDeclaredField("tail"));
			sizeOffset = unsafe.objectFieldOffset
					(LinkedNodeQueue.class.getDeclaredField("size"));
		} catch (NoSuchFieldException | SecurityException | NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Error(e);
		}
    }
    
	
	public final int size(){
		return size;
	}
	
	//�������Ϊʲô�ǰ�ȫ�ģ�
	public void add(final LinkedNode ln){
		if(tail == null){
			//����ǰ�ڵ�����Ϊtail�ڵ㣬�ɹ�����tail�ڵ�����Ϊhead����ʾ���һ���ڵ�Ҳ�ǵ�һ���ڵ㣬ֱ�ӷ���
			//�����жϣ���ִ��һ��
			if(casTail(null, ln)){
				head = ln;
				incrementSize();
				return;
			}
		}
		for(;;){
			LinkedNode pred = tail;
			if(casTail(pred, ln)){
				if(pred == null){
					casHead(null, ln);
				}else{
					pred.setNext(ln);
					ln.setPrev(pred);
				}
				incrementSize();
				return;
			}
		}
	}
	
	/**
	 * Unsafe����
	 * 
	 * ʵ�ְ�ȫ����Ҫͨ��synchronized��lock
	 * ���߸���lock�߼���дһ��AbstractQueuedSynchronizer
	 * ͬʱadd����Ҳ��Ҫ�޸�
	 * ����Ŀǰremove������ʱ�����õ�������͵������
	 * @author renke.zuo@foxmail.com
	 * @time 2016-08-01 16:00:10
	 * @param ln
	 */
	public void remove(LinkedNode ln){
		LinkedNode pred = ln.getPrev();
		LinkedNode nexd = ln.getNext();
		if(pred != null ) pred.setNext(nexd);
		else head = nexd;
		if(nexd != null) nexd.setPrev(pred);
		else tail = pred;
		decrementSize();
	}
	
	public boolean casHead(LinkedNode oldNode,LinkedNode newNode){
		return unsafe.compareAndSwapObject(this, headOffset, oldNode, newNode);
	}
	
	public boolean casTail(LinkedNode oldNode,LinkedNode newNode){
		return unsafe.compareAndSwapObject(this, tailOffset, oldNode, newNode);
	}
	
	public int incrementSize(){
		return unsafe.getAndAddInt(this, sizeOffset, 1);
	}
	
	public int decrementSize(){
		return unsafe.getAndAddInt(this, sizeOffset, -1);
	}
	
	public LinkedNode getHead(){
		return head;
	}
	
	public LinkedNode getTail(){
		return tail;
	}
	
	/**
	 * if(remove function exists ) then this function is unsafe
	 * else this is save
	 * acquire nodeQueue to array up to time
	 * @author renke.zuo@foxmail.com
	 * @time 2016-08-01 16:13:54
	 * @return
	 */
	public LinkedNode[] toArray(){
		int length = size();
		LinkedNode[] lns = new LinkedNode[length];
		LinkedNode tmp = head;
		for(int i=0;i<length;i++){
			lns[i] = tmp;
			tmp = tmp.getNext();
		}
		return lns;
	}
	
}
