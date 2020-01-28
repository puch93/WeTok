package kr.co.core.wetok.server.netUtil;

public class NetUrls {
//    public static final String DOMAIN = "http://app.yeobo.co.kr";
    public static final String DOMAIN = "http://wetalk.alrigo.co.kr";
    public static final String ADDRESS = DOMAIN + "/lib/control.siso";       // 로그인
    public static final String SITEURL = "wetalk.alrigo.co.kr";
//    public static final String ADDRESS = DOMAIN;       // 로그인

    /* check */
    public static final String CHECK_FIND_PW = "setUserModifyCheckPw";
    public static final String CHECK_FIND_ID = "setUserModifyCheckId";
    public static final String CHECK_JOIN_ID = "setUserCheckId";

    /* set */
    public static final String SET_MODIFY_PW = "setUserModifyPw";
    public static final String SET_MODIFY_PW_MEMBER = "setUserModifyPwConfirm";
    public static final String SET_PROFILE_NAME = "setUserModifyName";
    public static final String SET_PROFILE_BIRTH = "setUserModifyBirth";
    public static final String SET_PROFILE_INTRO = "setUserModifyIntro";
    public static final String SET_PROFILE_IMAGE = "setUserImage";
    public static final String SET_PROFILE_IMAGE_DEL = "setUserImageDel";
    public static final String SET_FRIEND_ADD = "setFriedAdd";
    public static final String SET_STORY = "setStoryContents";
    public static final String SET_STORY_DELETE = "setStoryDelete";
    public static final String SET_FRIEND_SYNC = "setFriendCheckAuto";
    public static final String SET_CHAT_ALLREAD = "setNoteAllRead";

    /* get */
    public static final String GET_FRIEND_LIST = "getFriendList";
    public static final String GET_MY_INFO = "getMemberInfo";
    public static final String GET_FRIEND_FROM_ID = "getFriendFromId";
    public static final String GET_FRIEND_FROM_HP = "getFriendFromHp";
    public static final String GET_STORY_LIST = "getStoryList";
    public static final String GET_CHAT_LIST = "getChatList";
    public static final String GET_CHECK_EXIST = "getChatExist";
    public static final String GET_NOTICE = "getNotice";
    public static final String GET_TERM = "getTerm";
    public static final String GET_VERSION = "setPlaystorUpdateCheck";

    /* others */
    public static final String USER_REGI = "setMemberUserRegi";
    public static final String USER_LOGIN = "setMemberUserLoginCk";
}
