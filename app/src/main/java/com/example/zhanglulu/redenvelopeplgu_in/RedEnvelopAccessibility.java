package com.example.zhanglulu.redenvelopeplgu_in;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;

import java.util.List;
import java.util.Random;

/**
 * Created by zhanglulu on 2018/1/2.
 *
 */

public class RedEnvelopAccessibility extends AccessibilityService {
    private static final String TAG = "RedEnvelopAccessibility";
    private Handler mHandler = new Handler();

    /**
     * 页面变化回调事件
     * @param event event.getEventType() 当前事件的类型;
     *              event.getClassName() 当前类的名称;
     *              event.getSource() 当前页面中的节点信息；得到的是被点击的单体对象
     *              event.getPackageName() 事件源所在的包名
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();

        //event.getSource:得到的是被点击的单体对象
        //getRootInActiveWindow():整个窗口的对象
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                clickNotification(rootInActiveWindow, event);
                break;
            default:
                if (event.getPackageName().equals("com.tencent.mm")) {
                    clickWeiChat(rootInActiveWindow);
                }
        }

        showAllText(getRootInActiveWindow());
    }

    private int mCurState = DEFAULT;
    public static final int DEFAULT = 0;
    public static final int CHECK_RED = 1;
    public static final int GET_RED = 2;
    /**
     * 点击
     * @throws Exception
     */
    private void clickWeiChat(final AccessibilityNodeInfo rootInActiveWindow) {

        if (rootInActiveWindow != null) {
            //自己发的红包
            if (mCurState == DEFAULT) {
                List<AccessibilityNodeInfo> checkRedNodeInfos = null;
                List<AccessibilityNodeInfo> ownerRed = rootInActiveWindow.findAccessibilityNodeInfosByText("查看红包");
                List<AccessibilityNodeInfo> otherRed = rootInActiveWindow.findAccessibilityNodeInfosByText("领取红包");
                if (ownerRed.size() > 0) {
                    checkRedNodeInfos = ownerRed;
                } else if (otherRed.size() > 0) {
                    checkRedNodeInfos = otherRed;
                }


                if (checkRedNodeInfos != null && checkRedNodeInfos.size() > 0) {
                    for (int i = 0; i < checkRedNodeInfos.size(); i++) {
                        AccessibilityNodeInfo nodeInfo = checkRedNodeInfos.get(i);
                        if (nodeInfo != null) {
                            AccessibilityNodeInfo linearNodeInfo = nodeInfo.getParent();
                            if (linearNodeInfo != null) {
                                AccessibilityNodeInfo relativeNodeInfo = linearNodeInfo.getParent();
                                if (relativeNodeInfo != null) {
                                    AccessibilityNodeInfo linearNodeInfoP = relativeNodeInfo.getParent();
                                    if (linearNodeInfoP != null) {
                                        AccessibilityNodeInfo linearNodeInfoRoot = linearNodeInfoP.getParent();
                                        if (linearNodeInfoRoot != null) {
                                            if (linearNodeInfoRoot.isClickable()) {
                                                linearNodeInfoRoot.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                mCurState = CHECK_RED;
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }

            }

            //点击抢红包
            if (mCurState == CHECK_RED) {
                List<AccessibilityNodeInfo> getRedNodeInfos = rootInActiveWindow.findAccessibilityNodeInfosByText("发了一个红包，金额随机");
                for (int i = 0; i < getRedNodeInfos.size(); i++) {
                    AccessibilityNodeInfo payNodeTextNode = getRedNodeInfos.get(i);
                    AccessibilityNodeInfo payNodeLinearNode = payNodeTextNode.getParent();
                    if (payNodeLinearNode != null) {
                        AccessibilityNodeInfo payNodeLinearNodeParent = payNodeLinearNode.getParent();
                        if (payNodeLinearNodeParent != null) {
                            if (payNodeLinearNodeParent.getChildCount() > 2) {
                                final AccessibilityNodeInfo getRedNode = payNodeLinearNodeParent.getChild(2);
                                if (getRedNode != null) {
                                    if (getRedNode.getClassName().equals(Button.class.getName())) {
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                getRedNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                mCurState = GET_RED;
                                            }
                                        }, 800);

                                    }
                                }
                            }

                        }

                    }

                }
            }
            //已经抢到了红包，之后就点击返回键
            if (mCurState == GET_RED) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCurState = DEFAULT;
                        List<AccessibilityNodeInfo> endNodeInfos1 = rootInActiveWindow.findAccessibilityNodeInfosByText("已存入零钱，可直接消费");
                        List<AccessibilityNodeInfo> endNodeInfos2 = rootInActiveWindow.findAccessibilityNodeInfosByText("手慢了，红包派完了");
                        List<AccessibilityNodeInfo> endNodeInfos3 = rootInActiveWindow.findAccessibilityNodeInfosByText("已存入零钱，可用于发红包");
                        List<AccessibilityNodeInfo> endNodeInfos4 = rootInActiveWindow.findAccessibilityNodeInfosByText("红包详情");
                        if (endNodeInfos1.size() > 0 || endNodeInfos2.size() > 0 || endNodeInfos3.size() > 0 || endNodeInfos4.size() > 0) {
                            performGlobalAction(GLOBAL_ACTION_BACK);
                            Log.d(TAG, "clickWeiChat: ddddd 点击了返回键");
                        }
                    }
                }, 500);
            }
        }
    }

    /**
     * 点击通知栏
     * @param rootInActiveWindow
     * @param event
     */
    private void clickNotification(AccessibilityNodeInfo rootInActiveWindow, AccessibilityEvent event) {
        List<CharSequence> eventText = event.getText();
        if (eventText != null) {
            for (CharSequence key : eventText) {
                Log.d(TAG, "clickNotification: key=>" + key);
                if (((String) key).contains("[微信红包]")) {
                    Notification notification = (Notification) event.getParcelableData();
                    try {
                        notification.contentIntent.send();//点击通知栏
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 点击某个位置
     * @param position
     * @return
     */
    private boolean pressLocation(Point position) {
        boolean isSuccess = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            Path p = new Path();
            p.moveTo(position.x, position.y);
            int randomDiffX = 5 - new Random().nextInt(5);
            int randomDiffY = 5 - new Random().nextInt(5);
            Log.d(TAG, "pressLocation randomDiff = " + randomDiffX
                    + "," + randomDiffY);
            p.lineTo(position.x + randomDiffX, position.y + randomDiffY);
            builder.addStroke(new GestureDescription.StrokeDescription(p, 10L, 200L));
            GestureDescription gesture = builder.build();
            isSuccess = dispatchGesture(gesture, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                }
            }, mHandler);

        }
        return isSuccess;
    }


    /**
     * 显示当前信息
     * @param accessibilityNodeInfo
     */
    private void showAllText(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo != null) {
            if (accessibilityNodeInfo.getChildCount() == 0) {
                Log.v(TAG, "找到了view的文本是:" + accessibilityNodeInfo.getText());
                Log.v(TAG, "当前应用的包名是:" + accessibilityNodeInfo.getPackageName() + "  "
                        + accessibilityNodeInfo.getClassName());
                Log.v(TAG, "当前的getViewIdResourceName：" + accessibilityNodeInfo.getViewIdResourceName());
                Log.v(TAG, "当前的getWindowId：" + accessibilityNodeInfo.getWindowId());
            } else {
                for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
                    showAllText(accessibilityNodeInfo.getChild(i));
                }
            }
        }
    }

    /**
     * 中断AccessibilityService的反馈时调用
     */
    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt");
    }

}
