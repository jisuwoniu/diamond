package com.taobao.diamond.client.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.diamond.client.SubscriberListener;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.configinfo.ConfigureInfomation;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.utils.LoggerInit;


/**
 * ҵ��������ľۼ���
 * 
 * @author leiwen.zh
 * 
 */
public class DefaultSubscriberListener implements SubscriberListener {

    // �ص���־������¼
    private static final Log dataLog = LogFactory.getLog(LoggerInit.LOG_NAME_CONFIG_DATA);

    private final ConcurrentMap<String/* dataId + group */, CopyOnWriteArrayList<ManagerListener>/* listeners */> allListeners =
            new ConcurrentHashMap<String, CopyOnWriteArrayList<ManagerListener>>();


    public Executor getExecutor() {
        return null;
    }


    public void receiveConfigInfo(final ConfigureInfomation configureInfomation) {
        String dataId = configureInfomation.getDataId();
        String group = configureInfomation.getGroup();
        if (null == dataId) {
            dataLog.error("[receiveConfigInfo] dataId is null");
            return;
        }

        String key = makeKey(dataId, group);
        CopyOnWriteArrayList<ManagerListener> listeners = allListeners.get(key);
        if (listeners == null || listeners.isEmpty()) {
            dataLog.warn("[notify-listener] no listener for dataId=" + dataId + ", group=" + group);
            return;
        }

        for (ManagerListener listener : listeners) {
            try {
                notifyListener(configureInfomation, listener);
            }
            catch (Throwable t) {
                dataLog.error("call listener error, dataId=" + dataId + ", group=" + group, t);
            }
        }
    }


    private void notifyListener(final ConfigureInfomation configureInfomation, final ManagerListener listener) {
        if (listener == null) {
            return;
        }

        final String dataId = configureInfomation.getDataId();
        final String group = configureInfomation.getGroup();
        final String content = configureInfomation.getConfigureInfomation();

        dataLog.info("[notify-listener] call listener " + listener.getClass().getName() + ", for " + dataId + ", "
                + group + ", " + content);

        Runnable job = new Runnable() {
            public void run() {
                try {
                    listener.receiveConfigInfo(content);
                }
                catch (Throwable t) {
                    dataLog.error(t.getMessage(), t);
                }
            }
        };

        if (null != listener.getExecutor()) {
            listener.getExecutor().execute(job);
        }
        else {
            job.run();
        }
    }


    /**
     * ���һ��DataID��Ӧ��ManagerListener
     */
    public void addManagerListener(String dataId, String group, ManagerListener listener) {
        List<ManagerListener> list = new ArrayList<ManagerListener>();
        list.add(listener);
        addManagerListeners(dataId, group, list);
    }


    public List<ManagerListener> getManagerListenerList(String dataId, String group) {
        if (null == dataId) {
            return null;
        }

        String key = makeKey(dataId, group);
        return new ArrayList<ManagerListener>(allListeners.get(key));
    }


    /**
     * ɾ��һ��DataID��Ӧ�����е�ManagerListeners
     * 
     * @param dataId
     */
    public void removeManagerListeners(String dataId, String group) {
        if (null == dataId) {
            return;
        }

        String key = makeKey(dataId, group);
        allListeners.remove(key);
    }


    /**
     * ���һ��DataID��Ӧ��һЩManagerListener
     * 
     * @param dataId
     * @param addListeners
     */
    public void addManagerListeners(String dataId, String group, List<ManagerListener> addListeners) {
        if (null == dataId || null == addListeners) {
            return;
        }
        if (addListeners.size() == 0) {
            return;
        }

        String key = makeKey(dataId, group);
        CopyOnWriteArrayList<ManagerListener> listenerList = allListeners.get(key);
        if (listenerList == null) {
            listenerList = new CopyOnWriteArrayList<ManagerListener>();
            CopyOnWriteArrayList<ManagerListener> oldList = allListeners.putIfAbsent(key, listenerList);
            if (oldList != null) {
                listenerList = oldList;
            }
        }
        listenerList.addAll(addListeners);
    }


    private String makeKey(String dataId, String group) {
        if (StringUtils.isBlank(group)) {
            group = Constants.DEFAULT_GROUP;
        }
        return dataId + "_" + group;
    }

}
