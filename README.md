# PullToRefreshScrollViewDemo
Android使用PullToRefresh完成ListView下拉刷新和左滑删除
一、本文主要内容：

使用PullToRefresh完成ListView下拉、上拉刷新；
扩展PullToRefresh完美的实现ListView左滑删除效果； 注意：本文中的PullToRefresh并非完整的开源库，个人把一些不需要的和平时无相关的类已删除。看起来更加精简，更加容易理解。
附上PullToRefresh源码库下载地址：http://download.csdn.net/detail/jaynm/9670737
二、先看效果：

1.ListView下拉刷新、上拉加载更多：

这里写图片描述

2.ListView下拉刷新、上拉加载更多、左滑删除：

这里写图片描述

三、实现代码：

实现ListView下拉刷新： 
至于PullToRefreshBase类，自己修改过源码，代码太长这里就不贴出来，自己可以下载Demo自己仔细阅读，主要看如何应用到自己项目中：
/**
 * Created by caobo on 2016/11/1 0001.
 * ListView下拉刷新、上拉加载更多
 */

public class ListViewActivity extends Activity implements PullToRefreshBase.OnRefreshListener<ListView> {

    private PullToRefreshListView refreshlistview;

    private ListView mListView;

    //添加数据List集合
    //TODO：这里使用了LinkedList方便Demo中添加数据使用，实际项目中使用ArrayList即可。
    private LinkedList<String> pullData;

    private ListAdapter adapter;

    //标记下拉index
    private int pullDownIndex = 0;

    //标记上拉index
    private int pullUpIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        pullData = new LinkedList<>();
        refreshlistview = (PullToRefreshListView) findViewById(R.id.refreshlistview);
        refreshlistview.setPullLoadEnabled(false);
        refreshlistview.setScrollLoadEnabled(true);
        refreshlistview.setOnRefreshListener(this);
        mListView = refreshlistview.getRefreshableView();
        adapter = new ListAdapter(getData());
        mListView.setAdapter(adapter);
        refreshlistview.onRefreshComplete();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        onPullDown();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        onPullUp();
    }

    /**
     * 预加载初始化数据List
     * @return
     */
    public List<String> getData() {
        for (int i = 1; i <= 20; i++) {
            pullData.add("默认ListView数据" + i);
        }
        return pullData;
    }

    /**
     * 下拉刷新添加数据到List集合
     */
    public void onPullDown() {
        pullData.addFirst("下拉刷新数据" + pullDownIndex);
        pullDownIndex++;
        refreshlistview.onRefreshComplete();
        adapter.notifyDataSetChanged();
    }

    /**
     * 上拉加载添加数据到List集合
     */
    public void onPullUp() {
        pullData.addLast("上拉加载数据" + pullUpIndex);
        pullUpIndex++;
        refreshlistview.onRefreshComplete();
        adapter.notifyDataSetChanged();
    }

    public void onBackClick(View view){
        finish();
    }
}

是不是以上操作还是很简单的就完成了ListView下拉刷新，上拉加载更多。 
XML布局文件也很简单，只需要引用PullToRefreshListView的地址即可： 
这样我们就完成了一个ListView列表的下拉刷新和上拉加载更多，个人认为PullToRefresh这个库还是很强大的。

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.jaynm.pulltorefreshscrollviewdemo.refresh.PullToRefreshListView
        android:background="#000"
        android:id="@+id/refreshlistview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:cacheColorHint="@android:color/transparent"
        android:divider="@color/consumer_bg"
        android:dividerHeight="1px"
        android:fadingEdge="none"
        android:orientation="vertical"
        android:overScrollMode="never"
        android:requiresFadingEdge="none">
    </com.jaynm.pulltorefreshscrollviewdemo.refresh.PullToRefreshListView>

</LinearLayout>

实现ListView下拉刷新、左滑删除：

注意： 
a.这里重写ListView生成SwipeMenuListView，所以他仍然是ListView列表控件； 
b.既然需要左滑，必须要在onTouchEvent()方法里面来判断手势滑动的操作； 
c.需要考虑到下拉、上拉和左滑事件的冲突； 
d.需要考虑左滑删除事件每次只能有一个item处于删除状态；

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() != MotionEvent.ACTION_DOWN && mTouchView == null)
            return super.onTouchEvent(ev);
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                int oldPos = mTouchPosition;
                mDownX = ev.getX();
                mDownY = ev.getY();
                mTouchState = TOUCH_STATE_NONE;

                mTouchPosition = pointToPosition((int) ev.getX(), (int) ev.getY());

                if (mTouchPosition == oldPos && mTouchView != null
                        && mTouchView.isOpen()) {
                    mTouchState = TOUCH_STATE_X;
                    mTouchView.onSwipe(ev);
                    return true;
                }

                View view = getChildAt(mTouchPosition - getFirstVisiblePosition());

                if (mTouchView != null && mTouchView.isOpen()) {
                    mTouchView.smoothCloseMenu();
                    mTouchView = null;
                    // return super.onTouchEvent(ev);
                    // try to cancel the touch event
                    MotionEvent cancelEvent = MotionEvent.obtain(ev);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                    onTouchEvent(cancelEvent);
                    if (mOnMenuStateChangeListener != null) {
                        mOnMenuStateChangeListener.onMenuClose(oldPos);
                    }
                    return true;
                }
                if (view instanceof SwipeMenuLayout) {
                    mTouchView = (SwipeMenuLayout) view;
                    mTouchView.setSwipeDirection(mDirection);
                }
                if (mTouchView != null) {
                    mTouchView.onSwipe(ev);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //有些可能有header,要减去header再判断
                mTouchPosition = pointToPosition((int) ev.getX(), (int) ev.getY()) - getHeaderViewsCount();
                //如果滑动了一下没完全展现，就收回去，这时候mTouchView已经赋值，再滑动另外一个不可以swip的view
                //会导致mTouchView swip 。 所以要用位置判断是否滑动的是一个view
                if (!mTouchView.getSwipEnable() || mTouchPosition != mTouchView.getPosition()) {
                    break;
                }
                float dy = Math.abs((ev.getY() - mDownY));
                float dx = Math.abs((ev.getX() - mDownX));
                if (mTouchState == TOUCH_STATE_X) {
                    if (mTouchView != null) {
                        mTouchView.onSwipe(ev);
                    }
                    getSelector().setState(new int[]{0});
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(ev);
                    return true;
                } else if (mTouchState == TOUCH_STATE_NONE) {
                    if (Math.abs(dy) > MAX_Y) {
                        mTouchState = TOUCH_STATE_Y;
                    } else if (dx > MAX_X) {
                        mTouchState = TOUCH_STATE_X;
                        if (mOnSwipeListener != null) {
                            mOnSwipeListener.onSwipeStart(mTouchPosition);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_X) {
                    if (mTouchView != null) {
                        boolean isBeforeOpen = mTouchView.isOpen();
                        mTouchView.onSwipe(ev);
                        boolean isAfterOpen = mTouchView.isOpen();
                        if (isBeforeOpen != isAfterOpen && mOnMenuStateChangeListener != null) {
                            if (isAfterOpen) {
                                mOnMenuStateChangeListener.onMenuOpen(mTouchPosition);
                            } else {
                                mOnMenuStateChangeListener.onMenuClose(mTouchPosition);
                            }
                        }
                        if (!isAfterOpen) {
                            mTouchPosition = -1;
                            mTouchView = null;
                        }
                    }
                    if (mOnSwipeListener != null) {
                        mOnSwipeListener.onSwipeEnd(mTouchPosition);
                    }
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(ev);
                    return true;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

mOnSwipeListener.onSwipeStart(mTouchPosition);用来记录当前手势状态为MotionEvent.ACTION_MOVE开始向左滑动时的标记，mOnSwipeListener.onSwipeEnd(mTouchPosition);用来记录当前手势状态为MotionEvent.ACTION_UP结束向左滑动时的标记，主要用于在Activity界面回调时获取当前操作状态，既然如此，我们就可以根据onSwipeStart()和onSwipeEnd()这两个回调方法来解决上述的第三个问题(需要考虑到下拉、上拉和左滑事件的冲突)

    // 操作ListView左滑时的手势操作，这里用于处理上下左右滑动冲突：开始滑动时则禁止下拉刷新和上拉加载手势操作，结束滑动后恢复上下拉操作
        swipeMenuListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
                refreshlistview.setPullRefreshEnabled(false);
            }

            @Override
            public void onSwipeEnd(int position) {
                refreshlistview.setPullRefreshEnabled(true);
            }
        });

refreshlistview.setPullRefreshEnabled(false);方法便是我在PullToRefreshBase当中自定义的是否支持下拉刷新操作事件，我们可以根据onSwipeStart()和onSwipeEnd()方法来进行设置。 
这样我们就完美的解决了以上三点注意事项，从而实现ListView左滑删除也是一件很easy的事情。 
下面就看来上述注意事项d，这个需要在事件分发机制上下点功夫：因为当我们左滑出itemA的删除按钮，再次去滑动itemB时，不能让它也出现，得要先关闭掉itemA的删除状态，这样才是合理的操作，所以在方法中来处理拦截事件：

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //在拦截处处理，在滑动设置了点击事件的地方也能swip，点击时又不能影响原来的点击事件
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                boolean handled = super.onInterceptTouchEvent(ev);
                mTouchState = TOUCH_STATE_NONE;
                mTouchPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
                View view = getChildAt(mTouchPosition - getFirstVisiblePosition());

                //只在空的时候赋值 以免每次触摸都赋值，会有多个open状态
                if (view instanceof SwipeMenuLayout) {
                    //如果有打开了 就拦截.
                    if (mTouchView != null && mTouchView.isOpen() && !inRangeOfView(mTouchView.getMenuView(), ev)) {
                        return true;
                    }
                    mTouchView = (SwipeMenuLayout) view;
                    mTouchView.setSwipeDirection(mDirection);
                }
                //如果摸在另外个view
                if (mTouchView != null && mTouchView.isOpen() && view != mTouchView) {
                    handled = true;
                }

                if (mTouchView != null) {
                    mTouchView.onSwipe(ev);
                }
                return handled;
            case MotionEvent.ACTION_MOVE:
                float dy = Math.abs((ev.getY() - mDownY));
                float dx = Math.abs((ev.getX() - mDownX));
                if (Math.abs(dy) > MAX_Y || Math.abs(dx) > MAX_X) {
                    //每次拦截的down都把触摸状态设置成了TOUCH_STATE_NONE 只有返回true才会走onTouchEvent 所以写在这里就够了
                    if (mTouchState == TOUCH_STATE_NONE) {
                        if (Math.abs(dy) > MAX_Y) {
                            mTouchState = TOUCH_STATE_Y;
                        } else if (dx > MAX_X) {
                            mTouchState = TOUCH_STATE_X;
                            if (mOnSwipeListener != null) {
                                mOnSwipeListener.onSwipeStart(mTouchPosition);
                            }
                        }
                    }
                    return true;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }

OK，以上便是SwipeMenuListView类中的所有事件处理代码，下面就可以Activity中来引用我们所定义的SwipeMenuListView，从而实现ListView下拉刷新，上拉加载，左滑删除效果。

        // 创建左滑弹出的item
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // 创建Item
                SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                // 设置item的背景颜色
                openItem.setBackground(new ColorDrawable(Color.RED));
                // 设置item的宽度
                openItem.setWidth(Utils.dip2px(SwipeListViewActivity.this,90));
                // 设置item标题
                openItem.setTitle("删除");
                // 设置item字号
                openItem.setTitleSize(18);
                // 设置item字体颜色
                openItem.setTitleColor(Color.WHITE);
                // 添加到ListView的Item布局当中
                menu.addMenuItem(openItem);

            }
        };
        // set creator
        swipeMenuListView.setMenuCreator(creator);

        // 操作删除按钮的点击事件
        swipeMenuListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {

                Toast.makeText(SwipeListViewActivity.this,"删除"+pullData.get(position),Toast.LENGTH_LONG).show();

                return false;
            }
        });


        // 操作ListView左滑时的手势操作，这里用于处理上下左右滑动冲突：开始滑动时则禁止下拉刷新和上拉加载手势操作，结束滑动后恢复上下拉操作
        swipeMenuListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
                refreshlistview.setPullRefreshEnabled(false);
            }

            @Override
            public void onSwipeEnd(int position) {
                refreshlistview.setPullRefreshEnabled(true);
            }
        });
    }

四、常用的一些属性介绍：

pull-to-refresh在xml中还能定义一些属性：

ptrRefreshableViewBackground 设置整个mPullRefreshListView的背景色

ptrHeaderBackground 设置下拉Header或者上拉Footer的背景色

ptrHeaderTextColor 用于设置Header与Footer中文本的颜色

ptrHeaderSubTextColor 用于设置Header与Footer中上次刷新时间的颜色

ptrShowIndicator如果为true会在mPullRefreshListView中出现icon，右上角和右下角，挺有意思的。

ptrHeaderTextAppearance ， ptrSubHeaderTextAppearance分别设置拉Header或者上拉Footer中字体的类型颜色等等

ptrRotateDrawableWhilePulling当动画设置为rotate时，下拉是是否旋转。

总结：其实实现ListView刷新并不困难，可能以前我们经常会看到有这样的组件存在：XListView，这个组件应该在初学Android的时候，很多人都见过，这就是很多人自己定义编写的ListView下拉刷新，要实现功能也是没问题的，可是个人一直觉得效果体验程度太差。在项目中一直使用的是PullToRefresh下拉刷新。 
好了，今天的分享就到这里了，写的不足的地方和不懂的地方大家可以留言共同探讨学习交流！ 
分享自己的IT资源库QQ群：459756676 主要是帮助IT行业初学者分享视频学习资料，只要你是it爱好者都可进入共同学习！
