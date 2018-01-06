package com.example.zhanglulu.redenvelopeplgu_in;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.Random;

/**
 * Created by zhanglulu on 2018/1/2.
 *
 */

public class RedEnvelopAccessibility extends AccessibilityService {
    private static final String TAG = "InstallHelperAccessibil";
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
        //event.getSource:得到的是被点击的单体对象
        //getRootInActiveWindow():整个窗口的对象
        if (event.getPackageName().equals("com.tencent.mm")) {
            try {
                click();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        showAllText(getRootInActiveWindow());
    }

    /**
     * 点击
     * @throws Exception
     */
    private void click() throws Exception{
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow != null) {
            //查看红包
            List<AccessibilityNodeInfo> checkRedNodeInfos = rootInActiveWindow.findAccessibilityNodeInfosByText("查看红包");
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
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
            //点击抢红包
            List<AccessibilityNodeInfo> payRedInfoNodes = rootInActiveWindow.findAccessibilityNodeInfosByText("发了一个红包，金额随机");
            for (int i = 0; i < payRedInfoNodes.size(); i++) {
                AccessibilityNodeInfo payNodeTextNode = payRedInfoNodes.get(i);
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
                                            performGlobalAction(GLOBAL_ACTION_BACK);
                                        }
                                    }, 800);
                                }
                            }
                        }

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
        Toast.makeText(this, "权限中断", Toast.LENGTH_SHORT).show();
    }

}
