package com.imooc.game2048.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.GridLayout;

import com.imooc.game2048.activity.Game;
import com.imooc.game2048.bean.GameItem;
import com.imooc.game2048.config.Config;

import java.util.ArrayList;
import java.util.List;

public class GameView extends GridLayout implements OnTouchListener {

    // GameView对应矩阵
    private GameItem[][] mGameMatrix;
    // 空格List
    private List<Point> mBlanks;
    // 矩阵行列数
    private int mGameLines;
    // 记录坐标
    private int mStartX, mStartY, mEndX, mEndY;
    // 辅助数组
    private List<Integer> mCalList;
    private int mKeyItemNum = -1;
    // 历史记录数组
    private int[][] mGameMatrixHistory;
    // 历史记录分数
    private int mScoreHistory;
    // 最高记录
    // TODO： 每个游戏难度都有最高记录
    private int mHighScore;
    // 目标分数
    private int mTarget;

    public GameView(Context context) {
        super(context);
        mTarget = Config.mSp.getInt(Config.KEY_GAME_GOAL, 2048);
        initGameMatrix();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGameMatrix();
    }

    public void startGame() {
        initGameMatrix();
        initGameView(Config.mItemSize);
    }

    private void initGameView(int cardSize) {
        removeAllViews();
        GameItem card;
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                card = new GameItem(getContext(), 0);
                addView(card, cardSize, cardSize);
                // 初始化GameMatrix全部为0 空格List为所有
                mGameMatrix[i][j] = card;
                mBlanks.add(new Point(i, j));
            }
        }
        // 添加随机数字
        addRandomNum();
        addRandomNum();
    }

    /**
     * 撤销上次移动
     */
    public void revertGame() {
        // 第一次不能撤销
        int sum = 0;
        for (int[] element : mGameMatrixHistory) {
            for (int i : element) {
                sum += i;
            }
        }
        if (sum != 0) {
            Game.getGameActivity().setScore(mScoreHistory, 0);
            Config.SCROE = mScoreHistory;
            for (int i = 0; i < mGameLines; i++) {
                for (int j = 0; j < mGameLines; j++) {
                    mGameMatrix[i][j].setNum(mGameMatrixHistory[i][j]);
                }
            }
        }
    }

    /**
     * 添加随机数字
     */
    private void addRandomNum() {
        getBlanks();
        if (mBlanks.size() > 0) {
            int randomNum = (int) (Math.random() * mBlanks.size());

            // 随机的取一个空的位置
            Point randomPoint = mBlanks.get(randomNum);
            mGameMatrix[randomPoint.x][randomPoint.y]
                    .setNum(Math.random() > 0.2d ? 2 : 4); // 随机的生成2,4
                                                            // 2出现的概率的更大
            animCreate(mGameMatrix[randomPoint.x][randomPoint.y]);
        }
    }

    /**
     * 生成动画
     *
     * @param target GameItem
     */
    private void animCreate(GameItem target) {
        ScaleAnimation sa = new ScaleAnimation(0.1f, 1, 0.1f, 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(200);
        target.setAnimation(null);
        target.getItemView().startAnimation(sa);
    }

    /**
     * super模式下添加一个指定数字
     */
    private void addSuperNum(int num) {
        if (checkSuperNum(num)) {
            getBlanks();
            if (mBlanks.size() > 0) {
                int randomNum = (int) (Math.random() * mBlanks.size());
                Point randomPoint = mBlanks.get(randomNum);
                mGameMatrix[randomPoint.x][randomPoint.y].setNum(num);
                animCreate(mGameMatrix[randomPoint.x][randomPoint.y]);
            }
        }
    }

    /**
     * 检查添加的数是否是指定的数
     *
     * @param num num
     * @return 添加的数
     */
    private boolean checkSuperNum(int num) {
        boolean flag = (num == 2 || num == 4 || num == 8 || num == 16
                || num == 32 || num == 64 || num == 128 || num == 256
                || num == 512 || num == 1024);
        return flag;
    }

    /**
     * 获取所有没有数字的Item，存储在mBlanks数组
     */
    private void getBlanks() {
        mBlanks.clear();
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                if (mGameMatrix[i][j].getNum() == 0) {
                    mBlanks.add(new Point(i, j));
                }
            }
        }
    }

    /**
     * 初始化View
     */
    private void initGameMatrix() {
        // 初始化矩阵
        removeAllViews();
        mScoreHistory = 0;
        Config.SCROE = 0;
        Config.mGameLines = Config.mSp.getInt(Config.KEY_GAME_LINES, 4);
        mGameLines = Config.mGameLines;
        mGameMatrix = new GameItem[mGameLines][mGameLines];
        mGameMatrixHistory = new int[mGameLines][mGameLines];
        mCalList = new ArrayList<Integer>();
        mBlanks = new ArrayList<Point>();
        mHighScore = Config.mSp.getInt(Config.KEY_HIGH_SCROE, 0);
        setColumnCount(mGameLines);
        setRowCount(mGameLines);
        setOnTouchListener(this);
        // 初始化View参数
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(metrics);
        Config.mItemSize = metrics.widthPixels / Config.mGameLines;
        initGameView(Config.mItemSize);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                saveHistoryMatrix();
                mStartX = (int) event.getX();
                mStartY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //TODO: 把触发移动的条件放到这里，比放在UP事件里响应更快
                // 在DOWN时设置flag,当已经移动了的话（judgeDirection中有swipeXXX调用），就把flag清掉
                // 后面判断flag以免重复移动
                break;
            case MotionEvent.ACTION_UP:
                mEndX = (int) event.getX();
                mEndY = (int) event.getY();
                judgeDirection(mEndX - mStartX, mEndY - mStartY);
                if (isMoved()) {
                    addRandomNum();
                    // 修改显示分数
                    Game.getGameActivity().setScore(Config.SCROE, 0);
                }
                checkCompleted();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 保存历史记录
     */
    private void saveHistoryMatrix() {
        mScoreHistory = Config.SCROE;
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                mGameMatrixHistory[i][j] = mGameMatrix[i][j].getNum();
            }
        }
    }

    private int getDeviceDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        return (int) metrics.density;
    }

    /**
     * 根据偏移量判断移动方向
     *
     * @param offsetX offsetX
     * @param offsetY offsetY
     */
    private void judgeDirection(int offsetX, int offsetY) {
        int density = getDeviceDensity();
        int slideDis = 5 * density;
        int maxDis = 200000 * density;
        boolean flagNormal =
                (Math.abs(offsetX) > slideDis ||
                        Math.abs(offsetY) > slideDis) &&
                        (Math.abs(offsetX) < maxDis) &&
                        (Math.abs(offsetY) < maxDis);
        boolean flagSuper = Math.abs(offsetX) > maxDis ||
                Math.abs(offsetY) > maxDis;
        if (flagNormal && !flagSuper) {
            if (Math.abs(offsetX) > Math.abs(offsetY)) { //左右滑动
                if (offsetX > slideDis) { // 右滑
                    swipeRight();
                } else { //左滑
                    swipeLeft();
                }
            } else { // 上下滑动
                if (offsetY > slideDis) { //下滑
                    swipeDown();
                } else { // 上滑
                    swipeUp();
                }
            }
        } else if (flagSuper) { // 启动超级用户权限来添加自定义数字
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getContext());
            final EditText et = new EditText(getContext());
            builder.setTitle("Back Door")
                    .setView(et)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                    if (!TextUtils.isEmpty(et.getText())) {
                                        addSuperNum(Integer.parseInt(et
                                                .getText().toString()));
                                        checkCompleted();
                                    }
                                }
                            })
                    .setNegativeButton("ByeBye",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                    arg0.dismiss();
                                }
                            }).create().show();
        }
    }

    /**
     * 检测所有数字 看是否有满足条件的
     *
     * @return 0:结束 1:正常 2:成功
     */
    private int checkNums() {
        getBlanks();
        if (mBlanks.size() == 0) { // 填满了数字
            //TODO: 优化
            // i: 0-->N-2, j: 0-->N-2
            // 最后单独处理最后的一列和最后一行，可避免一些循环内的if跳转
            for (int i = 0; i < mGameLines; i++) {
                for (int j = 0; j < mGameLines; j++) {
                    if (j < mGameLines - 1) {
                        if (mGameMatrix[i][j].getNum() == mGameMatrix[i][j + 1]
                                .getNum()) { // 水平方向还有相同的数字，还能玩下去
                            return 1;
                        }
                    }
                    if (i < mGameLines - 1) {
                        if (mGameMatrix[i][j].getNum() == mGameMatrix[i + 1][j]
                                .getNum()) { //垂直方向还有相同的数字，还能玩下去
                            return 1;
                        }
                    }
                }
            }
            return 0;
        }
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                if (mGameMatrix[i][j].getNum() == mTarget) { // 有一个数字到了 2048，成功了
                    return 2;
                }
            }
        }
        return 1; // 没有填满，没有到2048，可以继续玩
    }

    /**
     * 判断是否结束
     * <p/>
     * 0:结束 1:正常 2:成功
     */
    private void checkCompleted() {
        int result = checkNums();
        if (result == 0) {
            if (Config.SCROE > mHighScore) {
                Editor editor = Config.mSp.edit();
                editor.putInt(Config.KEY_HIGH_SCROE, Config.SCROE);
                editor.apply();
                Game.getGameActivity().setScore(Config.SCROE, 1);
                Config.SCROE = 0;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Game Over")
                    .setPositiveButton("Again",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                    startGame();
                                }
                            }).create().show();
            Config.SCROE = 0;
        } else if (result == 2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Mission Accomplished")
                    .setPositiveButton("Again",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                    // 重新开始
                                    startGame();
                                }
                            })
                    .setNegativeButton("Continue",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                    // 继续游戏 修改target
                                    Editor editor = Config.mSp.edit();
                                    if (mTarget == 1024) {
                                        editor.putInt(Config.KEY_GAME_GOAL, 2048);
                                        mTarget = 2048;
                                        Game.getGameActivity().setGoal(2048);
                                    } else if (mTarget == 2048) {
                                        editor.putInt(Config.KEY_GAME_GOAL, 4096);
                                        mTarget = 4096;
                                        Game.getGameActivity().setGoal(4096);
                                    } else {
                                        editor.putInt(Config.KEY_GAME_GOAL, 4096);
                                        mTarget = 4096;
                                        Game.getGameActivity().setGoal(4096);
                                    }
                                    editor.apply();
                                }
                            }).create().show();
            Config.SCROE = 0;
        }
    }

    //TODO: 核心算法应该独立出来，不应该和业务/视图合并在一起
    // 算法提供如下API：
    // 1. 设置游戏维度,目标
    // 2. 初始化算法内部结构，开始游戏
    // 3. 输入一个方向，向上/下/左/右移动，返回结果矩阵，以及新插入的随机数位置
    // 4. 是否游戏结束
    // 5. 是否有移动过isMoved
    // 6. 撤回一步

    /**
     * 判断是否移动过(是否需要新增Item)
     *
     * @return 是否移动
     */
    private boolean isMoved() {
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                if (mGameMatrixHistory[i][j] != mGameMatrix[i][j].getNum()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 滑动事件：上
     */
    private void swipeUp() {
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                int currentNum = mGameMatrix[j][i].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) { //当前值和上次的一样，合并后加入列表
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCROE += mKeyItemNum * 2; // 增加分数
                            mKeyItemNum = -1;
                        } else { // 当前值和上次的不一样，加入列表
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else { // 忽略空项
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);
            }
            // 填入处理过的值
            // 改变Item值
            for (int j = 0; j < mCalList.size(); j++) {
                mGameMatrix[j][i].setNum(mCalList.get(j));
            }
            for (int m = mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[m][i].setNum(0);
            }
            // 重置行参数
            mKeyItemNum = -1;
            mCalList.clear();
        }
    }

    /**
     * 滑动事件：下
     */
    private void swipeDown() {
        for (int i = mGameLines - 1; i >= 0; i--) {
            for (int j = mGameLines - 1; j >= 0; j--) {
                int currentNum = mGameMatrix[j][i].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCROE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);
            }
            // 改变Item值
            for (int j = 0; j < mGameLines - mCalList.size(); j++) {
                mGameMatrix[j][i].setNum(0);
            }
            int index = mCalList.size() - 1;
            for (int m = mGameLines - mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[m][i].setNum(mCalList.get(index));
                index--;
            }
            // 重置行参数
            mKeyItemNum = -1;
            mCalList.clear();
            index = 0;
        }
    }

    /**
     * 滑动事件：左
     */
    private void swipeLeft() {
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                int currentNum = mGameMatrix[i][j].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCROE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);
            }
            // 改变Item值
            for (int j = 0; j < mCalList.size(); j++) {
                mGameMatrix[i][j].setNum(mCalList.get(j));
            }
            for (int m = mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[i][m].setNum(0);
            }
            // 重置行参数
            mKeyItemNum = -1;
            mCalList.clear();
        }
    }

    /**
     * 滑动事件：右
     */
    private void swipeRight() {
        for (int i = mGameLines - 1; i >= 0; i--) {
            for (int j = mGameLines - 1; j >= 0; j--) {
                int currentNum = mGameMatrix[i][j].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCROE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);
            }
            // 改变Item值
            for (int j = 0; j < mGameLines - mCalList.size(); j++) {
                mGameMatrix[i][j].setNum(0);
            }
            int index = mCalList.size() - 1;
            for (int m = mGameLines - mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[i][m].setNum(mCalList.get(index));
                index--;
            }
            // 重置行参数
            mKeyItemNum = -1;
            mCalList.clear();
            index = 0;
        }
    }
}
