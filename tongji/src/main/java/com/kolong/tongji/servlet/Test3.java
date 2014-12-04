package com.kolong.tongji.servlet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.kolong.tongji.util.JsonUtil;

public class Test3 {
	public static void main(String[] args) {
		System.out.println(convertFromDateStr("2014-6-1 00:00:00"));
		System.out.println(convertFromDateStr("2014-7-1 23:59:59"));
		
		System.out.println(JsonUtil.format(JsonUtil.convertToDate(1415030399)));
	}
	
	/**
	 * ������������ת����long����
	 * @param rq �ַ�����ʽ������
	 * @return  ����long���͵�ʱ��=datetime/1000
	 */
	public static long convertFromDateStr(String rq) {
		long created = 0;
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rq);
			created = date.getTime()/1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return created;
	}
	
}
