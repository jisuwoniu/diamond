/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.io.watch;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.diamond.io.Path;


/**
 * Watch����Ϊ�ļ�ϵͳ�ṩ���ӹ��ܣ����ļ�����Ŀ¼��ӡ�ɾ�����߸��ĵ�ʱ���ṩ����֪ͨ����
 * 
 * @author boyan
 * @date 2010-5-4
 */
public final class WatchService {
    private BlockingQueue<WatchKey> changedKeys = new LinkedBlockingQueue<WatchKey>();

    private BlockingQueue<WatchKey> watchedKeys = new LinkedBlockingQueue<WatchKey>();

    private static final Log log = LogFactory.getLog(WatchService.class);

    private ScheduledExecutorService service;


    public WatchService(long checkInterval) {
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new CheckThread(), checkInterval, checkInterval, TimeUnit.MILLISECONDS);
    }

    private final class CheckThread implements Runnable {
        public void run() {
            check();
        }

    }


    /**
     * ����check
     */
    public void check() {
        synchronized (this) {
            Iterator<WatchKey> it = watchedKeys.iterator();
            while (it.hasNext()) {
                WatchKey key = it.next();
                try {
                    if (key.check()) {
                        changedKeys.add(key);
                        it.remove();
                    }
                }
                catch (Throwable t) {
                    log.error("���WatchKey�쳣,key=" + key, t);
                }
            }
        }
    }


    /**
     * ע��Ŀ¼
     * 
     * @param root
     * @param events
     * @return
     */
    public WatchKey register(Path root, WatchEvent.Kind<?>... events) {
        if (events == null || events.length == 0)
            throw new UnsupportedOperationException("null events");
        if (this.service.isShutdown())
            throw new IllegalStateException("�����Ѿ��ر�");
        if (!root.exists())
            throw new IllegalArgumentException("���ӵ�Ŀ¼������");
        WatchKey key = new WatchKey(root, this, false, events);
        resetKey(key);
        return key;
    }


    public WatchKey register(Path root, boolean fireCreatedEventOnIndex, WatchEvent.Kind<?>... events) {
        if (events == null || events.length == 0)
            throw new UnsupportedOperationException("null events");
        if (this.service.isShutdown())
            throw new IllegalStateException("�����Ѿ��ر�");
        if (!root.exists())
            throw new IllegalArgumentException("���ӵ�Ŀ¼������");
        WatchKey key = new WatchKey(root, this, fireCreatedEventOnIndex, events);
        resetKey(key);
        return key;
    }


    boolean resetKey(WatchKey key) {
        return this.watchedKeys.add(key);
    }


    /**
     * ֹͣ����
     */
    public void close() {
        this.service.shutdown();
    }


    /**
     * ��ȡ�ı��WatchKey
     * 
     * @return
     */
    public WatchKey poll() {
        return changedKeys.poll();
    }


    /**
     * ��ȡ�ı��WatchKey
     * 
     * @return
     */
    public WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
        return changedKeys.poll(timeout, unit);
    }


    /**
     * ��ȡ�ı��WatchKey
     * 
     * @return
     */
    public WatchKey take() throws InterruptedException {
        return changedKeys.take();
    }
}
