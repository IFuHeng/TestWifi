package com.changhong.wifimng.http;

public interface Config {
    int CodeSuccess = 1000;
    int NoData = 2003;
    int CodeUpdateNotExisted = 1401;

    //    String HOST_TEST = "http://test.chwliot.com";
    String HOST_TEST = "http://dev.chwliot.com";
    String HOST = "https://home1.chwliot.com";

    int PORT = 12281;

    String ALGORITHM_MODE = "DES";
    String ALGORITHM_DES = "DES/CBC/PKCS5Padding";
    String ivString = "12345678";
    String codeType = "UTF-8";

    String METHOD_BINDING = "api/smart_home_app/appDevice/binding";
    String METHOD_UNBINDING = "api/smart_home_app/appDevice/unbinding";
    String METHOD_DEVICE_DETAIL = "api/smart_home_app/appDevice/get";
    String METHOD_DEVICE_UPDATE = "api/smart_home_app/appDevice/update";
    String METHOD_DEVICE_LIST = "api/smart_home_app/appDevice/list";
    String METHOD_UPGRADE_CHECK = "api/smart_home_app/upgrade/checkList";
    String METHOD_UPGRADE_PUSH = "api/smart_home_app/upgrade/push";
    String METHOD_DEVICE_GET_CONFIG = "api/smart_home_app/device/getConfiguration";

    String METHOD_GROUP_LIST = "api/smart_home_app/group/list";
    String METHOD_CREATE_GROUP = "api/smart_home_app/group/new";
    String METHOD_DELETE_GROUP = "api/smart_home_app/group/delete";
    String METHOD_DEVICE_GROUP = "api/smart_home_app/group/deviceGroup";
    String METHOD_GET_GROUP = "api/smart_home_app/group/get";
    String METHOD_UPDATE_GROUP = "api/smart_home_app/group/update";

    String METHOD_DEVICE_SHARE = "api/smart_home_app/device/share";
    String METHOD_SHARED_LIST = "api/smart_home_app/device/share/shared/list";
    String METHOD_DEAL_SHARED = "api/smart_home_app/device/share/deal";
    String METHOD_FAMILY_MEMBER = "api/smart_home_app/device/share/family/list";
    String METHOD_GET_SHARED_LIST = "api/smart_home_app/device/share/sharer/list";
}
