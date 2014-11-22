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

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.mockserver.MockServer;


/**
 * Diamond�ͻ��˵�������Ϣ
 * 
 * @author aoqiong
 * 
 */
public class DiamondConfigure {

    private volatile int pollingIntervalTime = Constants.POLLING_INTERVAL_TIME;// �첽��ѯ�ļ��ʱ��
    private volatile int onceTimeout = Constants.ONCE_TIMEOUT;// ��ȡ����һ��DiamondServer����Ӧ�Ĳ�ѯһ��DataID��Ӧ��������Ϣ��Timeoutʱ��
    private volatile int receiveWaitTime = Constants.RECV_WAIT_TIMEOUT;// ͬ����ѯһ��DataID�����ѵ�ʱ��

    private volatile List<String> domainNameList = new LinkedList<String>();

    private volatile boolean useFlowControl = true;

    private boolean localFirst = false;

    // ���²�����֧�����к�̬����
    private int maxHostConnections = 1;
    private boolean connectionStaleCheckingEnabled = true;
    private int maxTotalConnections = 20;
    private int connectionTimeout = Constants.CONN_TIMEOUT;
    private int port = Constants.DEFAULT_PORT;
    private int scheduledThreadPoolSize = 1;
    // ��ȡ����ʱ�����Դ���
    private int retrieveDataRetryTimes = Integer.MAX_VALUE / 10;

    private String configServerAddress = null;
    private int configServerPort = Constants.DEFAULT_PORT;

    // �������ݱ���·��
    private String filePath;


    public DiamondConfigure() {
        filePath = System.getProperty("user.home") + "/diamond";
        File dir = new File(filePath);
        dir.mkdirs();

        if (!dir.exists()) {
            throw new RuntimeException("����diamondĿ¼ʧ�ܣ�" + filePath);
        }
    }


    /**
     * ��ȡ��ͬһ��DiamondServer�����������
     * 
     * @return
     */
    public int getMaxHostConnections() {
        return maxHostConnections;
    }


    /**
     * ���ú�ͬһ��DiamondServer�����������<br>
     * ��֧������ʱ��̬����
     * 
     * @param maxHostConnections
     */
    public void setMaxHostConnections(int maxHostConnections) {
        this.maxHostConnections = maxHostConnections;
    }


    /**
     * �Ƿ�����Գ¾ɵ�����������м�⡣<br>
     * �������⣬�����ϻ��������������ǣ�����ʹ�ò��������ӵķ��յ��µ�IO Exception
     * 
     * @return
     */
    public boolean isConnectionStaleCheckingEnabled() {
        return connectionStaleCheckingEnabled;
    }


    /**
     * �����Ƿ�����Գ¾ɵ�����������м�⡣<br>
     * ��֧������ʱ��̬����
     * 
     * @param connectionStaleCheckingEnabled
     */
    public void setConnectionStaleCheckingEnabled(boolean connectionStaleCheckingEnabled) {
        this.connectionStaleCheckingEnabled = connectionStaleCheckingEnabled;
    }


    /**
     * ��ȡ�������������������
     * 
     * @return
     */
    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }


    /**
     * �����������������������<br>
     * ��֧������ʱ��̬����
     * 
     * @param maxTotalConnections
     */
    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }


    /**
     * ��ȡ��ѯ�ļ��ʱ�䡣��λ����<br>
     * �˼��ʱ�������ѯ����һ��������Ϣ�ļ��ʱ�䣬����������أ������ö�һЩ��<br>
     * �����������ɱ��������Ϣ�������ó�һЩ
     * 
     * @return
     */
    public int getPollingIntervalTime() {
        return pollingIntervalTime;
    }


    /**
     * ������ѯ�ļ��ʱ�䡣��λ����
     * 
     * @param pollingIntervalTime
     */
    public void setPollingIntervalTime(int pollingIntervalTime) {
        if (pollingIntervalTime < Constants.POLLING_INTERVAL_TIME && !MockServer.isTestMode()) {
            return;
        }
        this.pollingIntervalTime = pollingIntervalTime;
    }


    /**
     * ��ȡ��ǰ֧�ֵ����е�DiamondServer�����б�
     * 
     * @return
     */
    public List<String> getDomainNameList() {
        return domainNameList;
    }


    /**
     * ���õ�ǰ֧�ֵ����е�DiamondServer�����б��������������б��ȱʡ�������б�ʧЧ
     * 
     * @param domainNameList
     */
    public void setDomainNameList(List<String> domainNameList) {
        if (null == domainNameList) {
            throw new NullPointerException();
        }
        this.domainNameList = new LinkedList<String>(domainNameList);
    }


    /**
     * ���һ��DiamondServer�������������������б��ȱʡ�������б�ʧЧ
     * 
     * @param domainName
     */
    public void addDomainName(String domainName) {
        if (null == domainName) {
            throw new NullPointerException();
        }
        this.domainNameList.add(domainName);
    }


    /**
     * ��Ӷ��DiamondServer�������������������б��ȱʡ�������б�ʧЧ
     * 
     * @param domainNameList
     */
    public void addDomainNames(Collection<String> domainNameList) {
        if (null == domainNameList) {
            throw new NullPointerException();
        }
        this.domainNameList.addAll(domainNameList);
    }


    /**
     * ��ȡDiamondServer�Ķ˿ں�
     * 
     * @return
     */
    public int getPort() {
        return port;
    }


    /**
     * ����DiamondServer�Ķ˿ں�<br>
     * ��֧������ʱ��̬����
     * 
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }


    /**
     * ��ȡ̽�Ȿ���ļ���·��
     * 
     * @return
     */
    public String getFilePath() {
        return filePath;
    }


    /**
     * ����̽�Ȿ���ļ���·��<br>
     * ��֧������ʱ��̬����
     * 
     * @param filePath
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    /**
     * ��ȡ����һ��DiamondServer����Ӧ�Ĳ�ѯһ��DataID��Ӧ��������Ϣ��Timeoutʱ��<br>
     * ��һ��HTTP����ĳ�ʱʱ��<br>
     * ��λ������<br>
     * 
     * @return
     */
    public int getOnceTimeout() {
        return onceTimeout;
    }


    /**
     * ���ö���һ��DiamondServer����Ӧ�Ĳ�ѯһ��DataID��Ӧ��������Ϣ��Timeoutʱ��<br>
     * ��λ������<br>
     * ������ϢԽ���뽫��ֵ���õ�Խ��
     * 
     * @return
     */
    public void setOnceTimeout(int onceTimeout) {
        this.onceTimeout = onceTimeout;
    }


    /**
     * ��ȡ��DiamondServer�����ӽ�����ʱʱ�䡣��λ������
     * 
     * @return
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }


    /**
     * ���ú�DiamondServer�����ӽ�����ʱʱ�䡣��λ������<br>
     * ��֧������ʱ��̬����
     * 
     * @param connectionTimeout
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }


    /**
     * ͬ����ѯһ��DataID����ȴ�ʱ��<br>
     * ʵ����ȴ�ʱ��С��receiveWaitTime + min(connectionTimeout, onceTimeout)
     * 
     * @return
     */
    public int getReceiveWaitTime() {
        return receiveWaitTime;
    }


    /**
     * ����һ��DataID����ȴ�ʱ��<br>
     * ʵ����ȴ�ʱ��С��receiveWaitTime + min(connectionTimeout, onceTimeout)
     * �����ֵ����ΪOnceTimeout * ��DomainName���� + 1��
     * 
     * @param receiveWaitTime
     */
    public void setReceiveWaitTime(int receiveWaitTime) {
        this.receiveWaitTime = receiveWaitTime;
    }


    /**
     * ��ȡ�̳߳ص��߳�����
     * 
     * @return
     */
    public int getScheduledThreadPoolSize() {
        return scheduledThreadPoolSize;
    }


    /**
     * �����̳߳ص��߳�������ȱʡΪ1
     * 
     * @param scheduledThreadPoolSize
     */
    public void setScheduledThreadPoolSize(int scheduledThreadPoolSize) {
        this.scheduledThreadPoolSize = scheduledThreadPoolSize;
    }


    /**
     * �Ƿ�ʹ��ͬ���ӿ�����
     * 
     * @return
     */
    public boolean isUseFlowControl() {
        return useFlowControl;
    }


    /**
     * �����Ƿ�ʹ��ͬ���ӿ�����
     * 
     * @param useFlowControl
     */
    public void setUseFlowControl(boolean useFlowControl) {
        this.useFlowControl = useFlowControl;
    }


    public String getConfigServerAddress() {
        return configServerAddress;
    }


    public void setConfigServerAddress(String configServerAddress) {
        this.configServerAddress = configServerAddress;
    }


    public int getConfigServerPort() {
        return configServerPort;
    }


    public void setConfigServerPort(int configServerPort) {
        this.configServerPort = configServerPort;
    }


    public int getRetrieveDataRetryTimes() {
        return retrieveDataRetryTimes;
    }


    public void setRetrieveDataRetryTimes(int retrieveDataRetryTimes) {
        this.retrieveDataRetryTimes = retrieveDataRetryTimes;
    }


    public boolean isLocalFirst() {
        return localFirst;
    }


    public void setLocalFirst(boolean localFirst) {
        this.localFirst = localFirst;
    }

}
