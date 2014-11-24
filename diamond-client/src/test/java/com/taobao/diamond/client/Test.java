//package com.taobao.diamond.client;
//
//import com.taobao.diamond.manager.ManagerListener;
//import com.taobao.diamond.manager.impl.DefaultDiamondManager;
//
//import java.util.concurrent.Executor;
//
///**
//* Created by yuanfei on 14/11/22.
//*/
//public class Test {
//
//    public static void main(String[] args) {
//        ManagerListener testManagerListener = new TestDiamond();
//        DefaultDiamondManager defaultDiamondManager = new DefaultDiamondManager("aaa",testManagerListener);
//    }
//    static class TestDiamond implements ManagerListener{
//
//        @Override
//        public Executor getExecutor() {
//            return null;
//        }
//
//        @Override
//        public void receiveConfigInfo(String configInfo) {
//
//            if(null != configInfo && !configInfo.equals("")){
//                System.out.println(configInfo);
//            }
//        }
//    }
//
//}
