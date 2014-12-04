package com.konglong.test;

public class TrimTest {
	public static void main(String[] args) {
		String value = "�������Ӷ� ";
		char[] val = new char[value.length()];
		value.getChars(0, value.length(), val, 0);//�ַ���ת�����ַ�����

		System.out.println(val.length);
		
		System.out.println(value.replaceAll("��| ", ""));
		
		System.out.println(myTrim(value, " ��"));
	}

	static String myTrim(String source, String toTrim) {//���ַ������ߵİ�ǿո�ȫ�ǿո�ȥ��������Ҳ����
		StringBuffer sb = new StringBuffer(source);
		while (toTrim.indexOf(new Character(sb.charAt(0)).toString()) != -1) {
			sb.deleteCharAt(0);
		}
		while (toTrim.indexOf(new Character(sb.charAt(sb.length() - 1))
				.toString()) != -1) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
}
