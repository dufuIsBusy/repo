package com.konglong.test;

import java.sql.Time;
import java.util.Date;

public class Message {
	public static void main(String[] args) {
		long l = 1394761301000l;
		System.out.println(new Date(l).toLocaleString());
		
		String str = "�����������ء�sdfds";
		System.out.println(str.split("��|��")[0]);
		System.out.println("�������ˣ�һ��ԭ�棩���ﲨ���У�1982��4��18�ճ���".length());
	}
}
