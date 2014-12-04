package com.kolong.tongji.vo;

import com.kolong.tongji.util.Params;

/**
 * ����ָ��
 * @author dfg
 *
 */
public class SecondLevelIndicator {
	//����ָ��
	public ThirdLevelIndicator third;
	
	public SecondLevelIndicator(ThirdLevelIndicator third) {
		this.third = third;
	}
	
	//������ָ��
	private double infoIndex;
	//�����ָ��
	private double viewIndex;
	//ת����ָ��
	private double reprintIndex;
	//������ָ��
	private double replyIndex;
	//����������ָ��
	private double contIndex;
	//�۵�������ָ��
	private double opinionIndex;
	//��վȨ����ָ��
	private double siteIndex;
	//����ȫ����ָ��
	private double areaIndex;
	//����ȫ����ָ��
	private double typeIndex;
	//��վӰ����ָ��
	private double influIndex;
	//��վ����ָ��
	private double numIndex;

	/**
	 * ������ָ��
	 * @return
	 */
	public double getInfoIndex() {
		infoIndex = ((third.getUserCount()-Params.get("minInfo"))*100.0)/(Params.get("maxInfo")-Params.get("minInfo"));
		return infoIndex;
	}
	
	/**
	 * �����ָ��
	 * @return
	 */
	public double getViewIndex() {
		viewIndex = ((third.getViewCount()-Params.get("minView"))*100.0)/(Params.get("maxView")-Params.get("minView"));
		return viewIndex;
	}
	/**
	 * ת����ָ��
	 * @return
	 */
	public double getReprintIndex() {
		reprintIndex = ((third.getReprintCount()-Params.get("minReprint"))*100.0)/(Params.get("maxReprint")-Params.get("minReprint"));
		return reprintIndex;
	}
	/**
	 * ������ָ��
	 * @return
	 */
	public double getReplyIndex() {
		replyIndex = ((third.getReplyCount()-Params.get("minReply"))*100.0)/(Params.get("maxReply")-Params.get("minReply"));
		return replyIndex;
	}
	/**
	 * ����������ָ��
	 * @return
	 */
	public double getContIndex() {
		contIndex = third.getMinLocIndex()*Params.get("minLocWeight") + third.getMinCountIndex()*Params.get("minCountWeight");
		return contIndex;
	}
	/**
	 * �۵�������ָ��
	 * @return
	 */
	public double getOpinionIndex() {
		opinionIndex = third.getSidesCountIndex()*Params.get("sidesCountWeight") + third.getDownSideIndex()*Params.get("downSideWeight");
		return opinionIndex;
	}
	/**
	 * ��վȨ����ָ��
	 * @return
	 */
	public double getSiteIndex() {
		siteIndex = third.getQ_overlayCapacityIndex()*Params.get("q_overlayCapacityWeight") + third.getQ_coverageRateIndex()*Params.get("q_coverageRateWeight");
		return siteIndex;
	}
	/**
	 * ����ȫ����ָ��
	 * @return
	 */
	public double getAreaIndex() {
		areaIndex = third.getA_overlayCapacityIndex()*Params.get("a_overlayCapacityWeight") + third.getA_coverageRateIndex()*Params.get("a_coverageRateWeight");
		return areaIndex;
	}
	/**
	 * ����ȫ����ָ��
	 * @return
	 */
	public double getTypeIndex() {
		typeIndex = third.getT_overlayCapacityIndex()*Params.get("t_overlayCapacityWeight") + third.getT_coverageRateIndex()*Params.get("t_coverageRateWeight");
		return typeIndex;
	}
	/**
	 * ��վӰ����ָ��
	 * @return
	 */
	public double getInfluIndex() {
		influIndex = ((third.getSite_yxl()-Params.get("minSite_yxl"))*100.0)/(Params.get("maxSite_yxl")-Params.get("minSite_yxl"));
		return influIndex;
	}
	/**
	 * ��վ����ָ��
	 * @return
	 */
	public double getNumIndex() {
		numIndex = ((third.getSitesCount()-Params.get("minSitesCount"))*100.0)/(Params.get("maxSitesCount")-Params.get("minSitesCount"));
		return numIndex;
	}
}
