/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.sdkapi.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.type.TypeReference;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.domain.BatchContextResult;
import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.domain.ConfigInfoEx;
import com.taobao.diamond.domain.ContextResult;
import com.taobao.diamond.domain.DiamondConf;
import com.taobao.diamond.domain.DiamondSDKConf;
import com.taobao.diamond.domain.Page;
import com.taobao.diamond.domain.PageContextResult;
import com.taobao.diamond.sdkapi.DiamondSDKManager;
import com.taobao.diamond.util.PatternUtils;
import com.taobao.diamond.util.RandomDiamondUtils;
import com.taobao.diamond.utils.JSONUtils;


/**
 * SDK���⿪�ŵ����ݽӿڵĹ���ʵ��
 * 
 * @filename DiamondSDKManagerImpl.java
 * @author libinbin.pt
 * @datetime 2010-7-16 ����04:00:19
 */
public class DiamondSDKManagerImpl implements DiamondSDKManager {

    private static final Log log = LogFactory.getLog("diamondSdkLog");

    // DiamondSDKConf���ü�map
    private Map<String, DiamondSDKConf> diamondSDKConfMaps;

    // ���ӳ�ʱʱ��
    private final int connection_timeout;
    // ����ʱʱ��
    private final int require_timeout;


    // ����ʱ��Ҫ�������ӳ�ʱʱ�䣬����ʱʱ��
    public DiamondSDKManagerImpl(int connection_timeout, int require_timeout) throws IllegalArgumentException {
        if (connection_timeout < 0)
            throw new IllegalArgumentException("���ӳ�ʱʱ�����ñ������0[��λ(����)]!");
        if (require_timeout < 0)
            throw new IllegalArgumentException("����ʱʱ�����ñ������0[��λ(����)]!");
        this.connection_timeout = connection_timeout;
        this.require_timeout = require_timeout;
        int maxHostConnections = 50;
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

        connectionManager.getParams().setDefaultMaxConnectionsPerHost(maxHostConnections);
        connectionManager.getParams().setStaleCheckingEnabled(true);
        this.client = new HttpClient(connectionManager);
        // �������ӳ�ʱʱ��
        client.getHttpConnectionManager().getParams().setConnectionTimeout(this.connection_timeout);
        // ���ö���ʱΪ1����
        client.getHttpConnectionManager().getParams().setSoTimeout(60 * 1000);
        client.getParams().setContentCharset("GBK");
        log.info("�������ӳ�ʱʱ��Ϊ: " + this.connection_timeout + "����");
    }


    /**
     * ʹ��ָ����diamond����������
     * 
     * @param dataId
     * @param groupName
     * @param context
     * @param serverId
     * @return ContextResult ��������
     */
    public synchronized ContextResult pulish(String dataId, String groupName, String context, String serverId) {
        ContextResult response = null;
        // ����dataId,groupName,context,serverIdΪ����֤
        if (validate(dataId, groupName, context)) {
            response = this.processPulishByDefinedServerId(dataId, groupName, context, serverId);
            return response;
        }

        // δͨ��Ϊ����֤
        response = new ContextResult();
        response.setSuccess(false);
        response.setStatusMsg("��ȷ��dataId,group,content��Ϊ��");
        return response;
    }


    /**
     * ʹ��ָ����diamond�������޸ĺ������
     * 
     * @param dataId
     * @param groupName
     * @param context
     * @param serverId
     * @return ContextResult ��������
     */
    public synchronized ContextResult pulishAfterModified(String dataId, String groupName, String context,
            String serverId) {

        ContextResult response = null;
        // ����dataId,groupName,context,serverIdΪ����֤
        if (validate(dataId, groupName, context)) {
            // ��diamondserver�����޸�����
            response = this.processPulishAfterModifiedByDefinedServerId(dataId, groupName, context, serverId);
            return response;
        }
        else {
            response = new ContextResult();
            // δͨ��Ϊ����֤
            response.setSuccess(false);
            response.setStatusMsg("��ȷ��dataId,group,content��Ϊ��");
            return response;
        }

    }


    // -------------------------ģ����ѯ-------------------------------//
    /**
     * ʹ��ָ����diamond��ģ����ѯ����
     * 
     * @param dataIdPattern
     * @param groupNamePattern
     * @param serverId
     * @param currentPage
     * @param sizeOfPerPage
     * @return PageContextResult<ConfigInfo> ��������
     * @throws SQLException
     */
    public synchronized PageContextResult<ConfigInfo> queryBy(String dataIdPattern, String groupNamePattern,
            String serverId, long currentPage, long sizeOfPerPage) {
        return processQuery(dataIdPattern, groupNamePattern, null, serverId, currentPage, sizeOfPerPage);
    }


    /**
     * ����ָ���� dataId,������content��ָ�����õ�diamond����ѯ�����б� ���ģʽ�а�������'*',����Զ��滻Ϊ'%'��ʹ��[
     * like ]��� ���ģʽ�в���������'*'���Ҳ�Ϊ�մ�������" "��,��ʹ��[ = ]���
     * 
     * @param dataIdPattern
     * @param groupNamePattern
     * @param contentPattern
     * @param serverId
     * @param currentPage
     * @param sizeOfPerPage
     * @return PageContextResult<ConfigInfo> ��������
     * @throws SQLException
     */

    public synchronized PageContextResult<ConfigInfo> queryBy(String dataIdPattern, String groupNamePattern,
            String contentPattern, String serverId, long currentPage, long sizeOfPerPage) {
        return processQuery(dataIdPattern, groupNamePattern, contentPattern, serverId, currentPage, sizeOfPerPage);
    }


    // =====================��ȷ��ѯ ==================================

    /**
     * ʹ��ָ����diamond��ָ����dataId,groupName����ȷ��ѯ����
     * 
     * @param dataId
     * @param groupName
     * @param serverId
     * @return ContextResult ��������
     * @throws SQLException
     */
    public synchronized ContextResult queryByDataIdAndGroupName(String dataId, String groupName, String serverId) {
        ContextResult result = new ContextResult();
        PageContextResult<ConfigInfo> pageContextResult = processQuery(dataId, groupName, null, serverId, 1, 1);
        result.setStatusMsg(pageContextResult.getStatusMsg());
        result.setSuccess(pageContextResult.isSuccess());
        result.setStatusCode(pageContextResult.getStatusCode());
        if (pageContextResult.isSuccess()) {
            List<ConfigInfo> list = pageContextResult.getDiamondData();
            if (list != null && !list.isEmpty()) {
                ConfigInfo info = list.iterator().next();
                result.setConfigInfo(info);
                result.setReceiveResult(info.getContent());
                result.setStatusCode(pageContextResult.getStatusCode());

            }
        }
        return result;
    }

    // ========================��ȷ��ѯ����==================================

    // /////////////////////////˽�й��߶�����͹��߷���ʵ��////////////////////////////////////////

    private final HttpClient client;


    // =========================== ���� ===============================

    private ContextResult processPulishByDefinedServerId(String dataId, String groupName, String context,
            String serverId) {
        ContextResult response = new ContextResult();
        // ��¼
        if (!login(serverId)) {
            response.setSuccess(false);
            response.setStatusMsg("��¼ʧ��,��ɴ����ԭ�������ָ����serverIdΪ�ջ򲻴���");
            return response;
        }
        if (log.isDebugEnabled())
            log.debug("ʹ��processPulishByDefinedServerId(" + dataId + "," + groupName + "," + context + "," + serverId
                    + ")��������");

        String postUrl = "/diamond-server/admin.do?method=postConfig";
        PostMethod post = new PostMethod(postUrl);
        // ��������ʱʱ��
        post.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, require_timeout);
        try {
            NameValuePair dataId_value = new NameValuePair("dataId", dataId);
            NameValuePair group_value = new NameValuePair("group", groupName);
            NameValuePair content_value = new NameValuePair("content", context);

            // ���ò���
            post.setRequestBody(new NameValuePair[] { dataId_value, group_value, content_value });
            // ���ö���
            ConfigInfo configInfo = new ConfigInfo();
            configInfo.setDataId(dataId);
            configInfo.setGroup(groupName);
            configInfo.setContent(context);
            if (log.isDebugEnabled())
                log.debug("�����͵�ConfigInfo: " + configInfo);
            // ���һ�����ö�����Ӧ�����
            response.setConfigInfo(configInfo);
            // ִ�з���������http״̬��
            int status = client.executeMethod(post);
            response.setReceiveResult(post.getResponseBodyAsString());
            response.setStatusCode(status);
            log.info("״̬�룺" + status + ",��Ӧ�����" + post.getResponseBodyAsString());
            if (status == HttpStatus.SC_OK) {
                response.setSuccess(true);
                response.setStatusMsg("���ʹ���ɹ�");
                log.info("���ʹ���ɹ�, dataId=" + dataId + ",group=" + groupName + ",content=" + context + ",serverId="
                        + serverId);
            }
            else if (status == HttpStatus.SC_REQUEST_TIMEOUT) {
                response.setSuccess(false);
                response.setStatusMsg("���ʹ���ʱ, Ĭ�ϳ�ʱʱ��Ϊ:" + require_timeout + "����");
                log.error("���ʹ���ʱ��Ĭ�ϳ�ʱʱ��Ϊ:" + require_timeout + "����, dataId=" + dataId + ",group=" + groupName
                        + ",content=" + context + ",serverId=" + serverId);
            }
            else {
                response.setSuccess(false);
                response.setStatusMsg("���ʹ���ʧ��, ״̬��Ϊ:" + status);
                log.error("���ʹ���ʧ��:" + response.getReceiveResult() + ",dataId=" + dataId + ",group=" + groupName
                        + ",content=" + context + ",serverId=" + serverId);
            }
        }
        catch (HttpException e) {
            response.setStatusMsg("���ʹ�����HttpException��" + e.getMessage());
            log.error("���ʹ�����HttpException: dataId=" + dataId + ",group=" + groupName + ",content=" + context
                    + ",serverId=" + serverId, e);
        }
        catch (IOException e) {
            response.setStatusMsg("���ʹ�����IOException��" + e.getMessage());
            log.error("���ʹ�����IOException: dataId=" + dataId + ",group=" + groupName + ",content=" + context
                    + ",serverId=" + serverId, e);
        }
        finally {
            // �ͷ�������Դ
            post.releaseConnection();
        }

        return response;
    }


    // =========================== ���ͽ��� ===============================

    // =========================== �޸� ===============================

    private ContextResult processPulishAfterModifiedByDefinedServerId(String dataId, String groupName, String context,
            String serverId) {
        ContextResult response = new ContextResult();
        // ��¼
        if (!login(serverId)) {
            response.setSuccess(false);
            response.setStatusMsg("��¼ʧ��,��ɴ����ԭ�������ָ����serverIdΪ��");
            return response;
        }
        if (log.isDebugEnabled())
            log.debug("ʹ��processPulishAfterModifiedByDefinedServerId(" + dataId + "," + groupName + "," + context + ","
                    + serverId + ")���������޸�");
        // �Ƿ���ڴ�dataId,groupName�����ݼ�¼
        ContextResult result = null;
        result = queryByDataIdAndGroupName(dataId, groupName, serverId);
        if (null == result || !result.isSuccess()) {
            response.setSuccess(false);
            response.setStatusMsg("�Ҳ�����Ҫ�޸ĵ����ݼ�¼����¼������!");
            log.warn("�Ҳ�����Ҫ�޸ĵ����ݼ�¼����¼������! dataId=" + dataId + ",group=" + groupName + ",serverId=" + serverId);
            return response;
        }
        // �����ݣ����޸�
        else {
            String postUrl = "/diamond-server/admin.do?method=updateConfig";
            PostMethod post = new PostMethod(postUrl);
            // ��������ʱʱ��
            post.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, require_timeout);
            try {
                NameValuePair dataId_value = new NameValuePair("dataId", dataId);
                NameValuePair group_value = new NameValuePair("group", groupName);
                NameValuePair content_value = new NameValuePair("content", context);
                // ���ò���
                post.setRequestBody(new NameValuePair[] { dataId_value, group_value, content_value });
                // ���ö���
                ConfigInfo configInfo = new ConfigInfo();
                configInfo.setDataId(dataId);
                configInfo.setGroup(groupName);
                configInfo.setContent(context);
                if (log.isDebugEnabled())
                    log.debug("�����͵��޸�ConfigInfo: " + configInfo);
                // ���һ�����ö�����Ӧ�����
                response.setConfigInfo(configInfo);
                // ִ�з���������http״̬��
                int status = client.executeMethod(post);
                response.setReceiveResult(post.getResponseBodyAsString());
                response.setStatusCode(status);
                log.info("״̬�룺" + status + ",��Ӧ�����" + post.getResponseBodyAsString());
                if (status == HttpStatus.SC_OK) {
                    response.setSuccess(true);
                    response.setStatusMsg("�����޸Ĵ���ɹ�");
                    log.info("�����޸Ĵ���ɹ�");
                }
                else if (status == HttpStatus.SC_REQUEST_TIMEOUT) {
                    response.setSuccess(false);
                    response.setStatusMsg("�����޸Ĵ���ʱ��Ĭ�ϳ�ʱʱ��Ϊ:" + require_timeout + "����");
                    log.error("�����޸Ĵ���ʱ��Ĭ�ϳ�ʱʱ��Ϊ:" + require_timeout + "����, dataId=" + dataId + ",group=" + groupName
                            + ",content=" + context + ",serverId=" + serverId);
                }
                else {
                    response.setSuccess(false);
                    response.setStatusMsg("�����޸Ĵ���ʧ��,ʧ��ԭ����ͨ��ContextResult��getReceiveResult()�����鿴");
                    log.error("�����޸Ĵ���ʧ��:" + response.getReceiveResult() + ",dataId=" + dataId + ",group=" + groupName
                            + ",content=" + context + ",serverId=" + serverId);
                }

            }
            catch (HttpException e) {
                response.setSuccess(false);
                response.setStatusMsg("�����޸ķ���ִ�й��̷���HttpException��" + e.getMessage());
                log.error(
                    "�������޸ķ���processPulishAfterModifiedByDefinedServerId(String dataId, String groupName, String context,String serverId)ִ�й����з���HttpException��dataId="
                            + dataId + ",group=" + groupName + ",content=" + context + ",serverId=" + serverId, e);
                return response;
            }
            catch (IOException e) {
                response.setSuccess(false);
                response.setStatusMsg("�����޸ķ���ִ�й��̷���IOException��" + e.getMessage());
                log.error(
                    "�������޸ķ���processPulishAfterModifiedByDefinedServerId(String dataId, String groupName, String context,String serverId)ִ�й����з���IOException��dataId="
                            + dataId + ",group=" + groupName + ",content=" + context + ",serverId=" + serverId, e);
                return response;
            }
            finally {
                // �ͷ�������Դ
                post.releaseConnection();
            }

            return response;
        }
    }


    // =========================== �޸Ľ��� ===============================

    /**
     * ���� httpclientʵ��ҳ���¼
     * 
     * @return ��¼��� true:��¼�ɹ�,false:��¼ʧ��
     */

    private boolean login(String serverId) {
        // serverId Ϊ���ж�
        if (StringUtils.isEmpty(serverId) || StringUtils.isBlank(serverId))
            return false;
        DiamondSDKConf defaultConf = diamondSDKConfMaps.get(serverId);
        log.info("[login] ��¼ʹ��serverId:" + serverId + ",�û����������ԣ�" + defaultConf);
        if (null == defaultConf)
            return false;
        RandomDiamondUtils util = new RandomDiamondUtils();
        // ��ʼ�����ȡֵ��
        util.init(defaultConf.getDiamondConfs());
        if (defaultConf.getDiamondConfs().size() == 0)
            return false;
        boolean flag = false;
        log.info("[randomSequence] �˴η�������Ϊ: " + util.getSequenceToString());
        // ������Դ���Ϊ��ĳ�����������������õ�diamondConf�ĳ���
        while (util.getRetry_times() < util.getMax_times()) {

            // �õ����ȡ�õ�diamondConf
            DiamondConf diamondConf = util.generatorOneDiamondConf();
            log.info("��" + util.getRetry_times() + "�γ���:" + diamondConf);
            if (diamondConf == null)
                break;
            client.getHostConfiguration().setHost(diamondConf.getDiamondIp(),
                Integer.parseInt(diamondConf.getDiamondPort()), "http");
            PostMethod post = new PostMethod("/diamond-server/login.do?method=login");
            // ��������ʱʱ��
            post.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, require_timeout);
            // ����û���������
            NameValuePair username_value = new NameValuePair("username", diamondConf.getDiamondUsername());
            NameValuePair password_value = new NameValuePair("password", diamondConf.getDiamondPassword());
            // ������������
            post.setRequestBody(new NameValuePair[] { username_value, password_value });
            log.info("ʹ��diamondIp: " + diamondConf.getDiamondIp() + ",diamondPort: " + diamondConf.getDiamondPort()
                    + ",diamondUsername: " + diamondConf.getDiamondUsername() + ",diamondPassword: "
                    + diamondConf.getDiamondPassword() + "��¼diamondServerUrl: [" + diamondConf.getDiamondConUrl() + "]");

            try {
                int state = client.executeMethod(post);
                log.info("��¼����״̬�룺" + state);
                // ״̬��Ϊ200�����¼�ɹ�,����ѭ��������true
                if (state == HttpStatus.SC_OK) {
                    log.info("��" + util.getRetry_times() + "�γ��Գɹ�");
                    flag = true;
                    break;
                }

            }
            catch (HttpException e) {
                log.error("��¼���̷���HttpException", e);
            }
            catch (IOException e) {
                log.error("��¼���̷���IOException", e);
            }
            finally {
                post.releaseConnection();
            }
        }
        if (flag == false) {
            log.error("���loginʧ�ܵ�ԭ������ǣ�����diamondServer�����û���Ŀǰ�������ã�serverId=" + serverId);
        }
        return flag;
    }

    static final String LIST_FORMAT_URL =
            "/diamond-server/admin.do?method=listConfig&group=%s&dataId=%s&pageNo=%d&pageSize=%d";
    static final String LIST_LIKE_FORMAT_URL =
            "/diamond-server/admin.do?method=listConfigLike&group=%s&dataId=%s&pageNo=%d&pageSize=%d";


    /**
     * �����ѯ
     * 
     * @param dataIdPattern
     * @param groupNamePattern
     * @param contentPattern
     * @param serverId
     * @param currentPage
     * @param sizeOfPerPage
     * @return
     */
    @SuppressWarnings("unchecked")
    private PageContextResult<ConfigInfo> processQuery(String dataIdPattern, String groupNamePattern,
            String contentPattern, String serverId, long currentPage, long sizeOfPerPage) {
        PageContextResult<ConfigInfo> response = new PageContextResult<ConfigInfo>();
        // ��¼
        if (!login(serverId)) {
            response.setSuccess(false);
            response.setStatusMsg("��¼ʧ��,��ɴ����ԭ�������ָ����serverIdΪ�ջ򲻴���");
            return response;
        }
        if (log.isDebugEnabled())
            log.debug("ʹ��processQuery(" + dataIdPattern + "," + groupNamePattern + "," + contentPattern + ","
                    + serverId + ")���в�ѯ");
        boolean hasPattern =
                PatternUtils.hasCharPattern(dataIdPattern) || PatternUtils.hasCharPattern(groupNamePattern)
                        || PatternUtils.hasCharPattern(contentPattern);
        String url = null;
        if (hasPattern) {
            if (!StringUtils.isBlank(contentPattern)) {
                log.warn("ע��, ���ڸ�������������ģ����ѯ, dataIdPattern=" + dataIdPattern + ",groupNamePattern=" + groupNamePattern
                        + ",contentPattern=" + contentPattern);
                // ģ����ѯ���ݣ�ȫ�������
                url = String.format(LIST_LIKE_FORMAT_URL, groupNamePattern, dataIdPattern, 1, Integer.MAX_VALUE);
            }
            else
                url = String.format(LIST_LIKE_FORMAT_URL, groupNamePattern, dataIdPattern, currentPage, sizeOfPerPage);
        }
        else {
            url = String.format(LIST_FORMAT_URL, groupNamePattern, dataIdPattern, currentPage, sizeOfPerPage);
        }

        GetMethod method = new GetMethod(url);
        configureGetMethod(method);
        try {

            int status = client.executeMethod(method);
            response.setStatusCode(status);
            switch (status) {
            case HttpStatus.SC_OK:
                String json = "";
                try {
                    json = getContent(method).trim();

                    Page<ConfigInfo> page = null;

                    if (!json.equals("null")) {
                        page =
                                (Page<ConfigInfo>) JSONUtils.deserializeObject(json,
                                    new TypeReference<Page<ConfigInfo>>() {
                                    });
                    }
                    if (page != null) {
                        List<ConfigInfo> diamondData = page.getPageItems();
                        if (!StringUtils.isBlank(contentPattern)) {
                            Pattern pattern = Pattern.compile(contentPattern.replaceAll("\\*", ".*"));
                            List<ConfigInfo> newList = new ArrayList<ConfigInfo>();
                            // ǿ������
                            Collections.sort(diamondData);
                            int totalCount = 0;
                            long begin = sizeOfPerPage * (currentPage - 1);
                            long end = sizeOfPerPage * currentPage;
                            for (ConfigInfo configInfo : diamondData) {
                                if (configInfo.getContent() != null) {
                                    Matcher m = pattern.matcher(configInfo.getContent());
                                    if (m.find()) {
                                        // ֻ���sizeOfPerPage��
                                        if (totalCount >= begin && totalCount < end) {
                                            newList.add(configInfo);
                                        }
                                        totalCount++;
                                    }
                                }
                            }
                            page.setPageItems(newList);
                            page.setTotalCount(totalCount);
                        }
                        response.setOriginalDataSize(diamondData.size());
                        response.setTotalCounts(page.getTotalCount());
                        response.setCurrentPage(currentPage);
                        response.setSizeOfPerPage(sizeOfPerPage);
                    }
                    else {
                        response.setOriginalDataSize(0);
                        response.setTotalCounts(0);
                        response.setCurrentPage(currentPage);
                        response.setSizeOfPerPage(sizeOfPerPage);
                    }
                    response.operation();
                    List<ConfigInfo> pageItems = new ArrayList<ConfigInfo>();
                    if (page != null) {
                        pageItems = page.getPageItems();
                    }
                    response.setDiamondData(pageItems);
                    response.setSuccess(true);
                    response.setStatusMsg("ָ��diamond�Ĳ�ѯ���");
                    log.info("ָ��diamond�Ĳ�ѯ���, url=" + url);
                }
                catch (Exception e) {
                    response.setSuccess(false);
                    response.setStatusMsg("�����л�ʧ��,������ϢΪ��" + e.getLocalizedMessage());
                    log.error("�����л�page����ʧ��, dataId=" + dataIdPattern + ",group=" + groupNamePattern + ",serverId="
                            + serverId + ",json=" + json, e);
                }
                break;
            case HttpStatus.SC_REQUEST_TIMEOUT:
                response.setSuccess(false);
                response.setStatusMsg("��ѯ���ݳ�ʱ" + require_timeout + "����");
                log.error("��ѯ���ݳ�ʱ��Ĭ�ϳ�ʱʱ��Ϊ:" + require_timeout + "����, dataId=" + dataIdPattern + ",group="
                        + groupNamePattern + ",serverId=" + serverId);
                break;
            default:
                response.setSuccess(false);
                response.setStatusMsg("��ѯ���ݳ�������������״̬��Ϊ" + status);
                log.error("��ѯ���ݳ���״̬��Ϊ��" + status + ",dataId=" + dataIdPattern + ",group=" + groupNamePattern
                        + ",serverId=" + serverId);
                break;
            }

        }
        catch (HttpException e) {
            response.setSuccess(false);
            response.setStatusMsg("��ѯ���ݳ���,������Ϣ���£�" + e.getMessage());
            log.error("��ѯ���ݳ���, dataId=" + dataIdPattern + ",group=" + groupNamePattern + ",serverId=" + serverId, e);
        }
        catch (IOException e) {
            response.setSuccess(false);
            response.setStatusMsg("��ѯ���ݳ���,������Ϣ���£�" + e.getMessage());
            log.error("��ѯ���ݳ���, dataId=" + dataIdPattern + ",group=" + groupNamePattern + ",serverId=" + serverId, e);
        }
        finally {
            // �ͷ�������Դ
            method.releaseConnection();
        }

        return response;
    }


    /**
     * �鿴�Ƿ�Ϊѹ��������
     * 
     * @param httpMethod
     * @return
     */
    boolean isZipContent(HttpMethod httpMethod) {
        if (null != httpMethod.getResponseHeader(Constants.CONTENT_ENCODING)) {
            String acceptEncoding = httpMethod.getResponseHeader(Constants.CONTENT_ENCODING).getValue();
            if (acceptEncoding.toLowerCase().indexOf("gzip") > -1) {
                return true;
            }
        }
        return false;
    }


    /**
     * ��ȡResponse��������Ϣ
     * 
     * @param httpMethod
     * @return
     */
    String getContent(HttpMethod httpMethod) throws UnsupportedEncodingException {
        StringBuilder contentBuilder = new StringBuilder();
        if (isZipContent(httpMethod)) {
            // ����ѹ������������Ϣ���߼�
            InputStream is = null;
            GZIPInputStream gzin = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            try {
                is = httpMethod.getResponseBodyAsStream();
                gzin = new GZIPInputStream(is);
                isr = new InputStreamReader(gzin, ((HttpMethodBase) httpMethod).getResponseCharSet()); // ���ö�ȡ���ı����ʽ���Զ������
                br = new BufferedReader(isr);
                char[] buffer = new char[4096];
                int readlen = -1;
                while ((readlen = br.read(buffer, 0, 4096)) != -1) {
                    contentBuilder.append(buffer, 0, readlen);
                }
            }
            catch (Exception e) {
                log.error("��ѹ��ʧ��", e);
            }
            finally {
                try {
                    br.close();
                }
                catch (Exception e1) {
                    // ignore
                }
                try {
                    isr.close();
                }
                catch (Exception e1) {
                    // ignore
                }
                try {
                    gzin.close();
                }
                catch (Exception e1) {
                    // ignore
                }
                try {
                    is.close();
                }
                catch (Exception e1) {
                    // ignore
                }
            }
        }
        else {
            // ����û�б�ѹ������������Ϣ���߼�
            String content = null;
            try {
                content = httpMethod.getResponseBodyAsString();
            }
            catch (Exception e) {
                log.error("��ȡ������Ϣʧ��", e);
            }
            if (null == content) {
                return null;
            }
            contentBuilder.append(content);
        }
        return StringEscapeUtils.unescapeHtml(contentBuilder.toString());
    }


    private void configureGetMethod(GetMethod method) {
        method.addRequestHeader(Constants.ACCEPT_ENCODING, "gzip,deflate");
        method.addRequestHeader("Accept", "application/json");
        // ��������ʱʱ��
        method.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, require_timeout);
    }


    /**
     * �ֶ�dataId,groupName,contextΪ����֤,��һ��Ϊ����������false
     * 
     * @param dataId
     * @param groupName
     * @param context
     * @return
     */
    private boolean validate(String dataId, String groupName, String context) {
        if (StringUtils.isEmpty(dataId) || StringUtils.isEmpty(groupName) || StringUtils.isEmpty(context)
                || StringUtils.isBlank(dataId) || StringUtils.isBlank(groupName) || StringUtils.isBlank(context))
            return false;
        return true;
    }


    public synchronized ContextResult unpublish(String serverId, long id) {
        return processDelete(serverId, id);
    }


    /**
     * ����ɾ��
     * 
     * @param serverId
     * @param id
     * @return
     */
    private ContextResult processDelete(String serverId, long id) {
        ContextResult response = new ContextResult();
        // ��¼
        if (!login(serverId)) {
            response.setSuccess(false);
            response.setStatusMsg("��¼ʧ��,��ɴ����ԭ�������ָ����serverIdΪ�ջ򲻴���");
            return response;
        }
        log.info("ʹ��processDelete(" + serverId + "," + id);
        String url = "/diamond-server/admin.do?method=deleteConfig&id=" + id;
        GetMethod method = new GetMethod(url);
        configureGetMethod(method);
        try {

            int status = client.executeMethod(method);
            response.setStatusCode(status);
            switch (status) {
            case HttpStatus.SC_OK:
                response.setSuccess(true);
                response.setReceiveResult(getContent(method));
                response.setStatusMsg("ɾ���ɹ�, url=" + url);
                log.warn("ɾ���������ݳɹ�, url=" + url);
                break;
            case HttpStatus.SC_REQUEST_TIMEOUT:
                response.setSuccess(false);
                response.setStatusMsg("ɾ�����ݳ�ʱ" + require_timeout + "����");
                log.error("ɾ�����ݳ�ʱ��Ĭ�ϳ�ʱʱ��Ϊ:" + require_timeout + "����, id=" + id + ",serverId=" + serverId);
                break;
            default:
                response.setSuccess(false);
                response.setStatusMsg("ɾ�����ݳ�������������״̬��Ϊ" + status);
                log.error("ɾ�����ݳ���״̬��Ϊ��" + status + ", id=" + id + ",serverId=" + serverId);
                break;
            }

        }
        catch (HttpException e) {
            response.setSuccess(false);
            response.setStatusMsg("ɾ�����ݳ���,������Ϣ���£�" + e.getMessage());
            log.error("ɾ�����ݳ���, id=" + id + ",serverId=" + serverId, e);
        }
        catch (IOException e) {
            response.setSuccess(false);
            response.setStatusMsg("ɾ�����ݳ���,������Ϣ���£�" + e.getMessage());
            log.error("ɾ�����ݳ���, id=" + id + ",serverId=" + serverId, e);
        }
        finally {
            // �ͷ�������Դ
            method.releaseConnection();
        }

        return response;
    }


    @Override
    public Map<String, DiamondSDKConf> getDiamondSDKConfMaps() {
        return this.diamondSDKConfMaps;
    }


    @Override
    public BatchContextResult<ConfigInfoEx> batchQuery(String serverId, String groupName, List<String> dataIds) {
        // �������ؽ��
        BatchContextResult<ConfigInfoEx> response = new BatchContextResult<ConfigInfoEx>();

        // �ж�list�Ƿ�Ϊnull
        if (dataIds == null) {
            log.error("dataId list cannot be null, serverId=" + serverId + ",group=" + groupName);
            response.setSuccess(false);
            response.setStatusMsg("dataId list cannot be null");
            return response;
        }

        // ��dataId��list����Ϊ��һ�����ɼ��ַ��ָ����ַ���
        StringBuilder dataIdBuilder = new StringBuilder();
        for (String dataId : dataIds) {
            dataIdBuilder.append(dataId).append(Constants.LINE_SEPARATOR);
        }
        String dataIdStr = dataIdBuilder.toString();
        // ��¼
        if (!login(serverId)) {
            response.setSuccess(false);
            response.setStatusMsg("login fail, serverId=" + serverId);
            return response;
        }

        // ����HTTP method
        PostMethod post = new PostMethod("/diamond-server/admin.do?method=batchQuery");
        // ��������ʱʱ��
        post.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, require_timeout);
        try {
            // ���ò���
            NameValuePair dataId_value = new NameValuePair("dataIds", dataIdStr);
            NameValuePair group_value = new NameValuePair("group", groupName);

            post.setRequestBody(new NameValuePair[] { dataId_value, group_value });

            // ִ�з���������http״̬��
            int status = client.executeMethod(post);
            response.setStatusCode(status);
            String responseMsg = post.getResponseBodyAsString();
            response.setResponseMsg(responseMsg);

            if (status == HttpStatus.SC_OK) {
                String json = null;
                try {
                    json = responseMsg;

                    // �����л�json�ַ���, ���������������BatchContextResult��
                    List<ConfigInfoEx> configInfoExList = new LinkedList<ConfigInfoEx>();
                    Object resultObj = JSONUtils.deserializeObject(json, new TypeReference<List<ConfigInfoEx>>() {
                    });
                    if (!(resultObj instanceof List<?>)) {
                        throw new RuntimeException("batch query deserialize type error, not list, json=" + json);
                    }
                    List<ConfigInfoEx> resultList = (List<ConfigInfoEx>) resultObj;
                    for (ConfigInfoEx configInfoEx : resultList) {
                        configInfoExList.add(configInfoEx);
                    }
                    response.getResult().addAll(configInfoExList);

                    // �����л��ɹ�, ����������ѯ�ɹ�
                    response.setSuccess(true);
                    response.setStatusMsg("batch query success");
                    log.info("batch query success, serverId=" + serverId + ",dataIds=" + dataIdStr + ",group="
                            + groupName + ",json=" + json);
                }
                catch (Exception e) {
                    response.setSuccess(false);
                    response.setStatusMsg("batch query deserialize error");
                    log.error("batch query deserialize error, serverId=" + serverId + ",dataIdStr=" + dataIdStr
                            + ",group=" + groupName + ",json=" + json, e);
                }

            }
            else if (status == HttpStatus.SC_REQUEST_TIMEOUT) {
                response.setSuccess(false);
                response.setStatusMsg("batch query timeout, socket timeout(ms):" + require_timeout);
                log.error("batch query timeout, socket timeout(ms):" + require_timeout + ", serverId=" + serverId
                        + ",dataIds=" + dataIdStr + ",group=" + groupName);
            }
            else {
                response.setSuccess(false);
                response.setStatusMsg("batch query fail, status:" + status);
                log.error("batch query fail, status:" + status + ", response:" + responseMsg + ",serverId=" + serverId
                        + ",dataIds=" + dataIdStr + ",group=" + groupName);
            }
        }
        catch (HttpException e) {
            response.setSuccess(false);
            response.setStatusMsg("batch query http exception��" + e.getMessage());
            log.error("batch query http exception, serverId=" + serverId + ",dataIds=" + dataIdStr + ",group="
                    + groupName, e);
        }
        catch (IOException e) {
            response.setSuccess(false);
            response.setStatusMsg("batch query io exception��" + e.getMessage());
            log.error("batch query io exception, serverId=" + serverId + ",dataIds=" + dataIdStr + ",group="
                    + groupName, e);
        }
        finally {
            // �ͷ�������Դ
            post.releaseConnection();
        }

        return response;
    }


    @Override
    public BatchContextResult<ConfigInfoEx> batchAddOrUpdate(String serverId, String groupName,
            Map<String, String> dataId2ContentMap) {
        // �������ؽ��
        BatchContextResult<ConfigInfoEx> response = new BatchContextResult<ConfigInfoEx>();

        // �ж�map�Ƿ�Ϊnull
        if (dataId2ContentMap == null) {
            log.error("dataId2ContentMap cannot be null, serverId=" + serverId + " ,group=" + groupName);
            response.setSuccess(false);
            response.setStatusMsg("dataId2ContentMap cannot be null");
            return response;
        }

        // ��dataId��content��map����Ϊ��һ�����ɼ��ַ��ָ����ַ���
        StringBuilder allDataIdAndContentBuilder = new StringBuilder();
        for (String dataId : dataId2ContentMap.keySet()) {
            String content = dataId2ContentMap.get(dataId);
            allDataIdAndContentBuilder.append(dataId + Constants.WORD_SEPARATOR + content).append(
                Constants.LINE_SEPARATOR);
        }
        String allDataIdAndContent = allDataIdAndContentBuilder.toString();

        // ��¼
        if (!login(serverId)) {
            response.setSuccess(false);
            response.setStatusMsg("login fail, serverId=" + serverId);
            return response;
        }

        // ����HTTP method
        PostMethod post = new PostMethod("/diamond-server/admin.do?method=batchAddOrUpdate");
        // ��������ʱʱ��
        post.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, require_timeout);
        try {
            // ���ò���
            NameValuePair dataId_value = new NameValuePair("allDataIdAndContent", allDataIdAndContent);
            NameValuePair group_value = new NameValuePair("group", groupName);

            post.setRequestBody(new NameValuePair[] { dataId_value, group_value });

            // ִ�з���������http״̬��
            int status = client.executeMethod(post);
            response.setStatusCode(status);
            String responseMsg = post.getResponseBodyAsString();
            response.setResponseMsg(responseMsg);

            if (status == HttpStatus.SC_OK) {
                String json = null;
                try {
                    json = responseMsg;

                    // �����л�json�ַ���, ���������������BatchContextResult��
                    List<ConfigInfoEx> configInfoExList = new LinkedList<ConfigInfoEx>();
                    Object resultObj = JSONUtils.deserializeObject(json, new TypeReference<List<ConfigInfoEx>>() {
                    });
                    if (!(resultObj instanceof List<?>)) {
                        throw new RuntimeException("batch write deserialize type error, not list, json=" + json);
                    }
                    List<ConfigInfoEx> resultList = (List<ConfigInfoEx>) resultObj;
                    for (ConfigInfoEx configInfoEx : resultList) {
                        configInfoExList.add(configInfoEx);
                    }
                    response.getResult().addAll(configInfoExList);
                    // �����л��ɹ�, �������������ɹ�
                    response.setStatusMsg("batch write success");
                    log.info("batch write success,serverId=" + serverId + ",allDataIdAndContent=" + allDataIdAndContent
                            + ",group=" + groupName + ",json=" + json);
                }
                catch (Exception e) {
                    response.setSuccess(false);
                    response.setStatusMsg("batch write deserialize error");
                    log.error("batch write deserialize error, serverId=" + serverId + ",allDataIdAndContent="
                            + allDataIdAndContent + ",group=" + groupName + ",json=" + json, e);
                }
            }
            else if (status == HttpStatus.SC_REQUEST_TIMEOUT) {
                response.setSuccess(false);
                response.setStatusMsg("batch write timeout, socket timeout(ms):" + require_timeout);
                log.error("batch write timeout, socket timeout(ms):" + require_timeout + ", serverId=" + serverId
                        + ",allDataIdAndContent=" + allDataIdAndContent + ",group=" + groupName);
            }
            else {
                response.setSuccess(false);
                response.setStatusMsg("batch write fail, status:" + status);
                log.error("batch write fail, status:" + status + ", response:" + responseMsg + ",serverId=" + serverId
                        + ",allDataIdAndContent=" + allDataIdAndContent + ",group=" + groupName);
            }
        }
        catch (HttpException e) {
            response.setSuccess(false);
            response.setStatusMsg("batch write http exception��" + e.getMessage());
            log.error("batch write http exception, serverId=" + serverId + ",allDataIdAndContent="
                    + allDataIdAndContent + ",group=" + groupName, e);
        }
        catch (IOException e) {
            response.setSuccess(false);
            response.setStatusMsg("batch write io exception��" + e.getMessage());
            log.error("batch write io exception, serverId=" + serverId + ",allDataIdAndContent=" + allDataIdAndContent
                    + ",group=" + groupName, e);
        }
        finally {
            // �ͷ�������Դ
            post.releaseConnection();
        }

        return response;
    }

}
