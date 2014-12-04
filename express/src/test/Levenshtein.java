package test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Levenshtein {
	private static Properties p = new Properties();
	private static int titles = 0;
	private static List<Long> ids = new ArrayList<Long>();

	public static void main(String[] args) throws IOException {
//		String str1 = "���������ͽ�4�� �õ��׷ݹ����ɻ�������(ͼ)";
//		String str2 = "����������4����ϻ����� ��н1000��Ԫ(ͼ)";
//		String str3 = "��ԩ����11��ũ������������4��� ����׷ݹ���";
//		String str4 = "�������̨�ع��˲���˫���Ǽ�ҩ����ƽ������˶����Ѱɣ�\r\n\r\n\r\n[��������]";
		String str5 = "�����������ϳ����ź�ɨ���\\ �����ȼ�������";
////		double d = getSimi2(str2, str1);
////		System.out.println(d);
//		str1 = rmHolder(str1);
//		str2 = rmHolder(str2);
//		str3 = rmHolder(str3);
//		str4 = rmHolder(str4);
		str5 = rmHolder(str5);
		System.out.println(str5);
//		System.out.println(rmChong(str1,str2));
		
		try {
			warning_send_sms("1740");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static String rmHolder(String str) {
		if(str != null) {
			str = str.trim().replaceAll(" ", "").replaceAll("(\\(|��)[\u4E00-\u9FFF����,��\\w]*(��|\\))", "")
						.replaceAll("\\\\n", "").replaceAll("\\\\r", "")
						.replaceAll("\\n", "").replaceAll("\\r", "")
						.replaceAll("\\\\", "").replaceAll("&\\\\#\\d*;", "")
						.replaceAll("��", "").replaceAll("\\��", "");
		}
		return str==null ? "" : str;
	}
	
	private static double rmChong(String str1, String str2) {
		double s1 = 0;
		for(int i=0; i<str1.length(); i++) {
			if(str2.contains(str1.charAt(i) + "")) {
				s1++;
			}
			
		}
		double d1 = s1/str2.length();
		//System.out.println(s1/str2.length());
		
		double s2 = 0;
		for(int i=0; i<str2.length(); i++) {
			if(str1.contains(str2.charAt(i)+"")) {
				s2++;
			}
		}
		double d2= s2/str1.length();
		//System.out.println(s2/str1.length());
		
		return (d1+d2)/2;
 	}

	public static List<String> warning_send_sms(String user_id) throws SQLException, IOException {
		String fileName = System.getProperty("user.dir") + "/logs/max_id_"+ user_id +".txt";//��־�ļ���
		
		String words = "";
		String urls = "";
		String filter = "on";
//		String related = "";
		String filter_similar = "";
		String weight = "10"; //Ĭ��Ȩ��Ϊ10
		
		try {
System.out.println(fileName);			
			p.load(new FileReader(fileName));//������־�ļ���Ϣ
		} catch (FileNotFoundException e) {
			System.out.println("��ʼ��max_id��־�ļ���");
		}
		
		 
		Object[] obj1 = {1l,"���������ͽ�4�� �õ��׷ݹ����ɻ�������(ͼ)","���������ͽ�4�� �õ��׷ݹ����ɻ�������(ͼ)","���������ͽ�4�� �õ��׷ݹ����ɻ�������(ͼ)"};
		Object[] obj2 = {2l,"���������ͽ�4�� �õ��׷ݹ����ɻ�������(ͼ)","���������ͽ�4�� �õ��׷ݹ����ɻ�������(ͼ)","�ӡ����鷳�ꡱ�����ɻث�������ϳ����Ǻٺ�һЦ"};
		Object[] obj3 = {3l,"���������ͽ�4�� �õ��׷ݹ����ɻ�������(ͼ)","���������ͽ�4�� �õ��׷ݹ����ɻ�������(ͼ)","�����������ϳ����ź�ɨ���\\ �����ȼ�������"};
		
		List<Object[]> datas = new ArrayList<Object[]>();
		datas.add(obj1);
		datas.add(obj2);
		datas.add(obj3);
		
		List<String> content = new ArrayList<String>();
		int m =0;//��¼ѭ������
		for (Object[] objects : datas) {
			
			try {
				p.load(new FileReader(fileName));//ÿ��ѭ�������¼�����־�ļ���Ϣ
			} catch (FileNotFoundException e) {
				System.out.println("��ʼ��max_id��־�ļ���");
			}
			
			
			long id = (Long) objects[0];
			//int warning_send = (Integer) objects[1];
			String word = (String) objects[1];
			// ���˹ؼ���
			String title = (String) objects[3];
			String pubdate = "";
			
			//�������
			title = title.trim().replaceAll(" {2,}", " ").replaceAll("\\\\n", "").replaceAll("\\\\r", "").replaceAll("&nbsp;", "").replaceAll("\\n", "").replaceAll("\\r", "").replace("[��������]", "");
			if(title.contains("????")) {
				continue;
			}
			
			if("on".equals(filter)) {
				Set<String> set =  p.stringPropertyNames();
				boolean simi = false;
				for(String s : set) {
					if(s.contains("title")) {
						double v = rmChong(rmHolder(title), rmHolder(s.replace("title��", "")));
						if(v > 0.5) {//���ƶ�Ϊ0.5
							simi = true;
							System.out.println("�������һ��:" + title+ "���ƶȣ�" + v);
						}
					}
				}
				
				if(simi) {
					continue;
				}
				
				ids.add(id);
			}
			
			
			//�������
			if(p.size() > 21) {//����20��title�������
				String userId = p.getProperty(user_id);
				p.clear();
				p.setProperty(user_id, userId);
			}
			p.setProperty("title��" + title, user_id);
			
		}

		// �������id
		if (ids.size() > 0) {
			p.setProperty(user_id, Collections.max(ids) + "");
			p.store(new FileWriter(fileName), "user_id Ϊ" + user_id
					+ "������¼��id");
		}
		
		p.clear();

		return content;
	}
	
}