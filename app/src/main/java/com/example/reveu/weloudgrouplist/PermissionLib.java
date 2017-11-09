package com.example.reveu.weloudgrouplist;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by reveu on 2017-10-23.
 */
class PermissionLib
{
    /*
        User_Manage_Group 권한이 있어도 Permission Setting은 하지 못한다. (오직 개설자만, USER_CREATOR))
        Rank Setting을 할 때 자신과 같은 Manage_Group을 가진 관리자 Rank로는 올릴 수 없으며,
        다른 관리자의 Rank를 수정하지도 못한다.

        또한 Manage_Group이 존재할 경우에면 User Ban이 가능하다. (Ban 또한 Manage_Group을 가진 관리자는 Ban을 할 수 없다.)
     */
    static final int USER_UPLOAD = 1;
    static final int USER_FILE_MODIFY_NAME = 1 << 1;
    static final int USER_FILE_MOVE = 1 << 2;
    static final int USER_FILE_DELETE = 1 << 3;
    static final int USER_FOLDER_ADD = 1 << 4;
    static final int USER_FOLDER_MODIFY = 1 << 5;
    static final int USER_FOLDER_DELETE = 1 << 6;
    static final int USER_APPROVE_JOIN = 1 << 7;
    static final int USER_INVITE = 1 << 8;
    static final int USER_MANAGE_GROUP = 1 << 9;
    static final int USER_CREATOR = 1 << 10;

    boolean isUserUpload(int permission)
    {
        return (permission & USER_UPLOAD) > 0;
    }

    boolean isUserFileModifyName(int permission)
    {
        return (permission & USER_FILE_MODIFY_NAME) > 0;
    }
    boolean isUserFileMove(int permission)
    {
        return (permission & USER_FILE_MOVE) > 0;
    }

    boolean isUserFileDelete(int permission)
    {
        return (permission & USER_FILE_DELETE) > 0;
    }

    boolean isUserFolderAdd(int permission)
    {
        return (permission & USER_FOLDER_ADD) > 0;
    }

    boolean isUserFolderModify(int permission)
    {
        return (permission & USER_FOLDER_MODIFY) > 0;
    }

    boolean isUserFolderDelete(int permission)
    {
        return (permission & USER_FOLDER_DELETE) > 0;
    }

    boolean isUserApproveJoin(int permission)
    {
        return (permission & USER_APPROVE_JOIN) > 0;
    }

    boolean isUserInvite(int permission)
    {
        return (permission & USER_INVITE) > 0;
    }

    boolean isUserManageGroup(int permission)
    {
        return (permission & USER_MANAGE_GROUP) > 0;
    }

    boolean isUserCreator(int permission)
    {
        return (permission & USER_CREATOR) > 0;
    }
}
