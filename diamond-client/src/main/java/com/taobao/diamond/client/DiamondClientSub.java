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

/**
 * Diamond��һ��֧�ֳ־õĿɿ����ı�������Ϣ�������ģ������ı�������Ϣ�Ķ��ġ�<br>
 * DiamondĿǰ��ʹ��ops�����־�������Ϣ��<br>
 * Diamond����ʹ�ü��е����ݿⱣ��־õ�������Ϣ��������Ϣʮ�ְ�ȫ�����ң�Diamond�ܹ�ʹ�ͻ���Զ��ȡ���µĶ�����Ϣ��<br>
 * ĿǰDiamond�ͻ���ӵ�����¼��ַ�ʽ�� <br>
 * 1.������ȡ<br>
 * 2.��ʱ��ȡ<br>
 * Diamond�ͻ��˻�֧�����Զ��������Ϣ���ԣ����ȼ����ߵı�������������Ϣ�Ļ�ȡ��ʹ��Properties����xml��
 * 
 * @author aoqiong
 * 
 */
public interface DiamondClientSub {

    public void setDiamondConfigure(DiamondConfigure diamondConfigure);


    public DiamondConfigure getDiamondConfigure();


    public void start();


    public void close();
}
