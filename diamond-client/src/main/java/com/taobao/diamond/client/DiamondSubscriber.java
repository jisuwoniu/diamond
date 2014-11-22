/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.client;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.taobao.diamond.configinfo.CacheData;


/**
 * DiamondSubscriber���ڶ��ĳ־õ��ı�������Ϣ��<br>
 * 
 * @author aoqiong
 * 
 */
public interface DiamondSubscriber extends DiamondClientSub {
    /**
     * �����첽���ĵ�Listener�����Զ�̬�滻
     * 
     * @param subscriberListener
     */
    public void setSubscriberListener(SubscriberListener subscriberListener);


    /**
     * ��ȡ�첽���ĵ�Listener
     * 
     * @return
     */
    public SubscriberListener getSubscriberListener();


    /**
     * ��ȡgroup��DataIDΪdataId��ConfigureInfomation��������start()���������,,�˷������ȴ�${user.
     * home}/diamond/data�»�ȡ�����ļ������û�У����diamond server��ȡ������Ϣ
     * 
     * @param dataId
     * @param group
     * @param timeout
     * @return
     */
    public String getConfigureInfomation(String dataId, String group, long timeout);


    /**
     * ��ȡȱʡ���DataIDΪdataId��ConfigureInfomation��������start()���������,�˷������ȴ�${user.home
     * }/diamond/data�»�ȡ�����ļ������û�У����diamond server��ȡ������Ϣ
     * 
     * @param dataId
     * @param timeout
     * @return
     */
    public String getConfigureInfomation(String dataId, long timeout);


    /**
     * ��ȡһ�ݿ��õ�������Ϣ������<strong>�����ļ�->diamond������->������һ�α����snapshot</strong>
     * ������˳���ȡһ����Ч��������Ϣ���������;�����޷���ȡһ����Ч������Ϣ �� �򷵻�null
     * 
     * @param dataId
     * @param group
     * @param timeout
     * @return
     */
    public String getAvailableConfigureInfomation(String dataId, String group, long timeout);


    /**
     * ���һ��DataID�����ԭ���д�DataID��Group�����滻����
     * 
     * @param dataId
     * @param group
     *            ��������Ϊnull������ʹ��ȱʡ������
     */
    public void addDataId(String dataId, String group);


    /**
     * ���һ��DataID��ʹ��ȱʡ�����������ԭ���д�DataID��Group�����滻����
     * 
     * @param dataId
     */
    public void addDataId(String dataId);


    /**
     * Ŀǰ�Ƿ�֧�ֶ�DataID��Ӧ��ConfigInfo
     * 
     * @param dataId
     * @return
     */
    public boolean containDataId(String dataId);


    /**
     * 
     * @param dataId
     * @param group
     * @return
     */
    public boolean containDataId(String dataId, String group);


    /**
     * 
     * @param dataId
     */
    public void removeDataId(String dataId);


    /**
     * 
     * @param dataId
     * @param group
     */
    public void removeDataId(String dataId, String group);


    /**
     * ������е�DataID
     */
    public void clearAllDataIds();


    /**
     * ��ȡ֧�ֵ����е�DataID
     * 
     * @return
     */
    public Set<String> getDataIds();


    /**
     * ��ȡ�ͻ���cache
     * 
     * @return
     */
    public ConcurrentHashMap<String, ConcurrentHashMap<String, CacheData>> getCache();


    /**
     * ��ȡһ�ݿ��õ�������Ϣ�����ձ���snapshot -> �����ļ� -> server��˳��
     * 
     * @param dataId
     * @param group
     * @param timeout
     * @return
     */
    public String getAvailableConfigureInfomationFromSnapshot(String dataId, String group, long timeout);

}
