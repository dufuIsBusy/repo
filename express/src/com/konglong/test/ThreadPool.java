package com.konglong.test;


 

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;





public class ThreadPool extends ThreadGroup {

    /**
     * �̳߳عرձ�־
     */
	private boolean isClose=false;
	/**
	 * doClsoe ����ִ�б��
	 */
	private boolean doClose=false;
	/**
	 * �������
	 */
	private  Object lockObj=new Object();
	/**
	 * ���񼯺�
	 */
	private Queue<Runnable> taskList=new LinkedBlockingQueue<Runnable>();
	/**
	 * �̳߳ؽ��� ����ӿ�
	 */
	private  PoolEndHandler   poolEndHandler=null;
	public  ThreadPool(String name,int poolSize,PoolEndHandler   poolEndHandler) {
		super(name);		
		this.setDaemon(true);	
		this.poolEndHandler=poolEndHandler;
		//���������߳�
		for(int p=0;p<poolSize;p++){
			Thread t=new WorkThread();
			t.setPriority(Thread.MAX_PRIORITY);
			t.start();
		}
	}
	/**
	 * ��������߳�
	 * @param task
	 */
	public void addTask(Runnable task){
		synchronized(lockObj){
			if(!isClose){
				taskList.offer(task);
			}
		}
	}
	
	/**
	 * �̳߳� �ر�
	 */
	public void close(){	
		    isClose=true;		
	}
	
	/**
	 * �̳߳عر� ִ�� doClose 
	 */
	public void  doClose(){
		synchronized(lockObj){
			if(poolEndHandler!=null && taskList.size()==0){				
				poolEndHandler.doEnd();
				doClose=true;
			}
		}
	}
	
	/**
	 * �����߳�
	 * @author Administrator
	 *
	 */
	private class WorkThread extends Thread{
	 	     
	    @SuppressWarnings("unused")
		private int count;
	    public WorkThread(){}
			       
		public void run(){
			while(true){
				synchronized(lockObj){
				if (taskList != null && !taskList.isEmpty()) {
						taskList.poll().run();
						count++;					 
					} else if (isClose) {// ����̳߳��Ѿ��ر� ���ٹ����߳�
						// �̳߳عرպ� ִ�� doClose ����
						if(!doClose){
							doClose();
						}
						break;
					}
				}
			}
		}
		
	}
	
	
}
