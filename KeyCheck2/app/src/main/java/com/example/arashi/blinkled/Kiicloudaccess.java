package com.example.arashi.blinkled;

/**
 * Created by arashi on 2016/11/17.
 */

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiGroupCallBack;
import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.cloud.storage.callback.KiiQueryCallBack;
import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.cloud.storage.query.KiiClause;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiGroupCallBack;
import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.cloud.storage.callback.KiiQueryCallBack;
import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.cloud.storage.exception.app.BadRequestException;
import com.kii.cloud.storage.exception.app.ConflictException;
import com.kii.cloud.storage.exception.app.ForbiddenException;
import com.kii.cloud.storage.exception.app.NotFoundException;
import com.kii.cloud.storage.exception.app.UnauthorizedException;
import com.kii.cloud.storage.exception.app.UndefinedException;
import com.kii.cloud.storage.query.KiiClause;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.LogRecord;

import static java.lang.System.in;

public class Kiicloudaccess extends AppCompatActivity {

    private static final String BUCKET_NAME = "myBucket";
    private static final String OBJECT_KEY = "myObjectValue";
    private Handler mHandler = new Handler();

    public String username;
    public String password;
    public String userstatus;
    public String groupID;


    //in or out
    public String inusers;
    public String outusers;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    //ユーザー登録
//    public void userregister(){
//        KiiUser user = KiiUser.createWithUsername(username);
//        user.register(new KiiUserCallBack() {
//            public void onRegisterCompleted(int token, KiiUser user, Exception e) {
//                if (e == null) {
//                    Log.d("d","seccess");
//                } else {
//                    Log.d("d","error");
//                    showToast("Error Registering: " + e.getLocalizedMessage());
//                }
//            }
//        }, password);
//    }


    //ログイン
    public void userlogin(){
        KiiUser.logIn(new KiiUserCallBack() {
            public void onLoginCompleted(int token, KiiUser user, Exception e) {
                if (e == null) {

                } else {
                    showToast("Error registering: " + e.getLocalizedMessage());
                }
            }
        }, username, password);
    }
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void kiiprocess(){
        Runnable kiiDelayRun = new Runnable() {
            @Override
            public void run() {
                kiiGroupCreateAndMemberInsert();
                kiiGroupCreateDatabase("oohira");
                kiiGroupCreateDatabase("nishimura");
                kiiSave();
                kiiGroupQueryStatus();
//                kiiGroupQuerySearch();
            }
        };

        mHandler.postDelayed(kiiDelayRun, 1000);
    }

    public void kiiSave() {
        KiiBucket bucket = KiiUser.getCurrentUser().bucket(BUCKET_NAME);
        KiiObject obj = bucket.object();

//        String value = "OUT";

        obj.set(OBJECT_KEY, userstatus);

        obj.save(new KiiObjectCallBack() {
            public void onSaveCompleted(int token, KiiObject o, Exception e) {
                if (e == null) {
                } else {
                    showToast("Error creating object: " + e.getLocalizedMessage());
                }
            }
        });


    }

    public void kiiFixed() {
        // Set key-value pairs.
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        String value = df.format(new Date());

        KiiBucket bucket = KiiUser.getCurrentUser().bucket(BUCKET_NAME);
        KiiObject obj = bucket.object();

        obj.set(OBJECT_KEY, value);

        obj.saveAllFields(new KiiObjectCallBack() {
            @Override
            public void onSaveCompleted(int token, KiiObject object, Exception exception) {
                if (exception != null) {
                    // Error handling
                    return;
                }
            }
        }, true);
    }

    public void kiiGroupCreateAndMemberInsert() {
        List<KiiUser> members = new ArrayList<KiiUser>();
        members.add(KiiUser.userWithID("c4619da00022-f1b9-6e11-dcba-05c4ba79"));
        members.add(KiiUser.userWithID("b56270b00022-b6ba-6e11-fcba-09786060"));

        KiiGroup group = Kii.group("myGroup", members);
        group.save(new KiiGroupCallBack() {
            @Override
            public void onSaveCompleted(int token, KiiGroup group, Exception exception) {
                if (exception != null) {
                    // Error handling

                    return;
                }
                Uri groupUri = group.toUri();
                String groupID = group.getID();
            }
        });
    }

    public void kiiGroupCatch() {

        // ... In another situation ...

        // Instantiate the group again.
        KiiGroup group = KiiGroup.groupWithID(groupID);

        // Refresh the instance to make it up-to-date.
        group.refresh(new KiiGroupCallBack() {
            @Override
            public void onRefreshCompleted(int token, KiiGroup group2, Exception exception) {
                if (exception != null) {
                    // Error handling
                    return;
                }

                // Do something with the group reference.
            }
        });

        group.listMembers(new KiiGroupCallBack() {
            @Override
            public void onListMembersCompleted(int token, final List<KiiUser> members, KiiGroup group, Exception exception) {
                if (exception != null) {
                    // Error handling
                    return;
                }
                if (members.size() <= 0) {
                    return;
                }
                members.get(0).refresh(new KiiUserCallBack() {
                    private int index = 0;

                    @Override
                    public void onRefreshCompleted(int token, Exception exception) {
                        if (exception != null) {
                            // Error handling
                            return;
                        }
                        KiiUser groupMember = members.get(index);
                        // Do something with groupMember

                        // Refresh next member
                        if (++index >= members.size()) {
                            return;
                        }
                        KiiUser nextMember = members.get(index);
                        nextMember.refresh(this);
                    }
                });
            }
        });
    }

    public void kiiGroupCreateDatabase(String uname){


        // ... In another situation ...

        // Instantiate the group again.
        KiiGroup group = KiiGroup.groupWithID(groupID);

        // Refresh the instance to make it up-to-date.
        group.refresh(new KiiGroupCallBack() {
            @Override
            public void onRefreshCompleted(int token, KiiGroup group2, Exception exception) {
                if (exception != null) {
                    // Error handling
                    return;
                }

                // Do something with the group reference.
            }
        });

        // Create Group Scope Bucket. (Login required)
        KiiBucket groupBucket = group.bucket("my_group");

        // Create an object in an application-scope bucket.
        KiiObject object = groupBucket.object();

        // Set key-value pairs
        object.set("username", uname);
        object.set("status", "");

        // Save the object
        object.save(new KiiObjectCallBack() {
            @Override
            public void onSaveCompleted(int token, KiiObject object, Exception exception) {
                if (exception != null) {
                    // Error handling
                    return;
                }
            }
        });
    }

    public void kiiGroupQueryStatus(){

        // ... In another situation ...

        // Instantiate the group again.
        KiiGroup group = KiiGroup.groupWithID(groupID);

        KiiBucket groupBucket = group.bucket("my_group");
        KiiQuery query = new KiiQuery(KiiClause.and(
                KiiClause.equals("username", username)));

        groupBucket.query(new KiiQueryCallBack<KiiObject>() {
            @Override
            public void onQueryCompleted(int token, KiiQueryResult<KiiObject> result, Exception exception) {
                if (exception != null) {
                    // Error handling
                    return;
                }
                List<KiiObject> objLists = result.getResult();
                for (KiiObject obj : objLists) {
                    // Do something with the first 10 objects
                    obj.set("status",userstatus);
                    obj.save(new KiiObjectCallBack() {
                        @Override
                        public void onSaveCompleted(int token, KiiObject object, Exception exception) {
                            if (exception != null) {
                                // Error handling
                                return;
                            }
                        }
                    });
                }

                // Fetching the next 10 objects (pagination handling)
                if (!result.hasNext()) {
                    return;
                }
                result.getNextQueryResult(this);
            }
        }, query);
    }

    public void kiiGroupQuerySearch(){



        // ... In another situation ...

        // Instantiate the group again.
        KiiGroup group = KiiGroup.groupWithID(groupID);

        KiiBucket groupBucket = group.bucket("my_group");

        KiiQuery all_query = new KiiQuery();

//        MainActivity.items.clear();
        groupBucket.query(new KiiQueryCallBack<KiiObject>() {
            @Override
            public void onQueryCompleted(int token, KiiQueryResult<KiiObject> result, Exception exception) {
                if (exception != null) {
                    // Error handling
                    return;
                }

                inusers = "";
                outusers = "";

                List<KiiObject> objLists = result.getResult();
                for (KiiObject obj : objLists) {
                    // Do something with objects in the result
                    Log.d("d",obj.getObject("username").toString());
                    Log.d("d",obj.getObject("status").toString());
                    if(obj.getObject("status").toString().equals("in")){
                        inusers += obj.getObject("username").toString() + " ";
                    }
                    if(obj.getObject("status").toString().equals("out")){
                        outusers += obj.getObject("username").toString() + " ";
                    }

                }
            }
        }, all_query);


    }




}

