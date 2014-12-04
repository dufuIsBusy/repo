package com.kolong.tongji.vo;

import com.kolong.tongji.util.Params;

/**
 * һ��ָ��
 * @author dfg
 *
 */
public class FirstLevelIndicator {
	//����ָ��
	public SecondLevelIndicator second;

	public FirstLevelIndicator(SecondLevelIndicator second) {
		this.second = second;
	}
	
	//�ع�̶�ָ��
	private double exposIndex;
	//����̶�ָ��
	private double participationIndex;
	//����۵�ָ��
	private double publicOpinionIndex;
	//��վ�ص�ָ��
	private double siteFeatureIndex;
	
	/**
	 * �ع�̶�ָ��
	 * @return
	 */
	public double getExposIndex() {
		exposIndex = second.getInfoIndex()*Params.get("infoWeight");
		return exposIndex;
	}
	/**
	 * ����̶�ָ��
	 * @return
	 */
	public double getParticipationIndex() {
		participationIndex = second.getViewIndex()*Params.get("viewWeight") + second.getReprintIndex()*Params.get("reprintWeight") + second.getReplyIndex()*Params.get("replyWeight");
		return participationIndex;
	}
	/**
	 * ����۵�ָ��
	 * @return
	 */
	public double getPublicOpinionIndex() {
		publicOpinionIndex = second.getContIndex()*Params.get("contWeight") + second.getOpinionIndex()*Params.get("opinionWeight");
		return publicOpinionIndex;
	}
	
	/**
	 * ��վ�ص�ָ��
	 * @return
	 */
	public double getSiteFeatureIndex() {
		siteFeatureIndex = second.getSiteIndex()*Params.get("siteWeight") + second.getAreaIndex()*Params.get("areaWeight") + second.getTypeIndex()*Params.get("typeWeight") + second.getInfluIndex()*Params.get("influWeight") + second.getNumIndex()*Params.get("numWeight");
		return siteFeatureIndex;
	}
	
	public class Rate {
		
		//�ع�仯��
		private double ExposIndexRate;
		
		public Rate(double exposIndexBefore) {//ǰһ��������ع�̶�
			getExposIndex();
			ExposIndexRate = (double)(exposIndex-exposIndexBefore)/exposIndexBefore;
		}
		
		public double getExposIndexRate() {
			return ExposIndexRate;
		}
	}
	
}
