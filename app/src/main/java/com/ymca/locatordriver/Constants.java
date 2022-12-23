package com.ymca.locatordriver;


public class Constants {
    public static final String SHARED_PREF = "notificationapp";
    public static final String REGISTERED = "registered";
    public interface ACTION {
        public static String STARTFOREGROUND_ACTION = "com.truiton.foregroundservice.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.truiton.foregroundservice.action.stopforeground";
        public static String MAIN_ACTION = "com.truiton.foregroundservice.action.main";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
