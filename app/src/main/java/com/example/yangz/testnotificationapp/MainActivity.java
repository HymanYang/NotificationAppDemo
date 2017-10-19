package com.example.yangz.testnotificationapp;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * 通知栏开关专题检查
 * <p>
 * 校对手机：
 * 华为P10（6.0） huawei
 * 红米手机
 * 小米6 7.1.1
 * vivo X6SPlus
 * oppo R7(4.4.4=19) 默认通知栏关闭
 */
public class MainActivity extends AppCompatActivity {

    // 声明Notification(通知)的管理者
    private NotificationManager mNotifyMgr;
    // 声明Notification（通知）对象
    private Notification notification;
    // 消息的唯一标示id
    public static long mNotificationId = 001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotifycation();
            }
        });

        TextView title = (TextView) findViewById(R.id.btn_desc_goto);
        title.setText(Build.VERSION.RELEASE + "/" + Build.VERSION.SDK_INT + " 通知栏状态：");
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotificationId = System.currentTimeMillis();
                goToNotificationSet();
            }
        });


        TextView statue = (TextView) findViewById(R.id.btn_desc);

        String result;

        if (isOpenNotification(this)) {
            result = "通知栏已经 <font color='#ff0000'>打开</font>";
        } else {
            result = "通知栏已经 <font color='#ff0000'>关闭</font>";
        }

        statue.setText(Html.fromHtml(result));


    }

    private boolean isOpenNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return up43(context);
        } else {
            return low43(context);
        }
    }

    private void goToNotificationSet() {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
            // 进入设置系统应用权限界面
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 运行系统在5.x环境使用
            // 进入设置系统应用权限界面
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);
            return;
        }*/

        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(localIntent);
    }


    /**
     * 4.3=19以上(Google提供支持方法，api有要求)
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean up43(Context context) {

        //In App Gradle file, com.android.support:support should be minimum 24 and compileSdkVersion has to be 24
        // NotificationManagerCompat.areNotificationsEnabled();

        String CHECK_OP_NO_THROW = "checkOpNoThrow";
        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        ApplicationInfo appInfo = context.getApplicationInfo();

        String pkg = context.getApplicationContext().getPackageName();

        int uid = appInfo.uid;

        Class appOpsClass = null;

        try {

            appOpsClass = Class.forName(AppOpsManager.class.getName());

            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);

            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (int) opPostNotificationValue.get(Integer.class);

            boolean boo = ((int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
            return boo;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 4.3以下
     * 该方法不能获取通知栏是否打开状态功能
     */
    public static boolean low43(Context context) {

        boolean boo = true;

        NotificationManager nm = (NotificationManager)
                context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);

        String pkg = context.getApplicationContext().getPackageName();

        try {
            Class NotificationManagerClass;
            // step1
            NotificationManagerClass = Class.forName(NotificationManager.class.getName());

            Method getServiceMethod = NotificationManagerClass.getMethod("getService");
            //Log.LOGD(TAG, "getServiceMethod: " + getServiceMethod);
            Object obj_inm = getServiceMethod.invoke(nm);
            //Log.LOGD(TAG, "obj_inm: " + obj_inm);

            // step2
            Class INotificationManagerClass;
            INotificationManagerClass = Class.forName("android.app.INotificationManager");

            // 多余步骤，看看nm里面有哪些方法，打出来看看
            Method[] list = INotificationManagerClass.getMethods();
            for (int i = 0; i < list.length; i++) {
                //Logs.LOGD(TAG, i + ": " + list[i].toString());
            }

            Method areNotificationsEnabledForPackage_Method = INotificationManagerClass.getMethod("areNotificationsEnabledForPackage", String.class);
            //Logs.LOGD(TAG, "areNotificationsEnabledForPackage_Method: " + areNotificationsEnabledForPackage_Method);
            boo = (boolean) areNotificationsEnabledForPackage_Method.invoke(obj_inm, pkg);
            //Logs.LOGD(TAG, "low43_invoke boo: " + boo);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Logs.LOGD(TAG, "low43: " + boo);
        return boo;
    }

    private void sendNotifycation() {
        // 创建一个即将要执行的PendingIntent对象
        Intent resultIntent = new Intent(MainActivity.this,
                ResultActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                MainActivity.this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // 建立所要创建的Notification的配置信息，并有notifyBuilder来保存。
        notification = new Notification.Builder(MainActivity.this)
                // 触摸之后，通知立即消失
                .setAutoCancel(true)
                // 显示的时间
                .setWhen(System.currentTimeMillis())
                // 设置通知的小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                // 设置状态栏显示的文本
                .setTicker("状态栏提示消息")
                // 设置通知的标题
                .setContentTitle("通知的标题！")
                // 设置通知的内容
                .setContentText("通知的内容！")
                // 设置声音（系统默认的）
                // .setDefaults(Notification.DEFAULT_SOUND)
                // 设置声音（自定义）
                //.setSound(Uri.parse("android.resource://org.crazyit.ui/" + R.raw.msg))
                // 设置跳转的activity
                .setContentIntent(resultPendingIntent)
                .getNotification();

        // 创建NotificationManager对象，并发布和管理所要创建的Notification
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify((int) mNotificationId, notification);
    }
}
