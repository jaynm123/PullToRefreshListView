package com.jaynm.pulltorefreshscrollviewdemo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jaynm.pulltorefreshscrollviewdemo.refresh.PullToRefreshBase;
import com.jaynm.pulltorefreshscrollviewdemo.refresh.PullToRefreshMenuView;
import com.jaynm.pulltorefreshscrollviewdemo.refresh.SwipeMenu;
import com.jaynm.pulltorefreshscrollviewdemo.refresh.SwipeMenuCreator;
import com.jaynm.pulltorefreshscrollviewdemo.refresh.SwipeMenuItem;
import com.jaynm.pulltorefreshscrollviewdemo.refresh.SwipeMenuListView;
import com.jaynm.pulltorefreshscrollviewdemo.utils.Utils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by caobo on 2016/11/1 0001.
 * ListView上拉刷新、下拉加載更多+左滑刪除
 */

public class SwipeListViewActivity extends Activity implements PullToRefreshBase.OnRefreshListener<SwipeMenuListView> {

    private PullToRefreshMenuView refreshlistview;

    private SwipeMenuListView swipeMenuListView;

    private LinkedList<String> pullData;

    private ListAdapter adapter;

    //标记下拉index
    private int pullDownIndex = 0;
    //标记上拉index
    private int pullUpIndex = 0;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipelistview);
        pullData = new LinkedList<>();
        refreshlistview = (PullToRefreshMenuView) findViewById(R.id.refreshlistview);
        refreshlistview.setPullLoadEnabled(false);
        refreshlistview.setScrollLoadEnabled(true);
        refreshlistview.setOnRefreshListener(this);
        swipeMenuListView = refreshlistview.getRefreshableView();
        adapter = new ListAdapter(getData());
        swipeMenuListView.setAdapter(adapter);
        refreshlistview.onRefreshComplete();

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

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<SwipeMenuListView> refreshView) {
        onPullDown();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<SwipeMenuListView> refreshView) {
        onPullUp();
    }

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    pullData.addFirst("下拉刷新数据" + pullDownIndex);
                    pullDownIndex++;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshlistview.onRefreshComplete();
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 上拉加载添加数据到List集合
     */
    public void onPullUp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    pullData.addLast("上拉加载数据" + pullUpIndex);
                    pullUpIndex++;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshlistview.onRefreshComplete();
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public class ListAdapter extends BaseAdapter {
        private List<String> listData;

        public ListAdapter(List<String> listData) {
            this.listData = listData;
        }

        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(SwipeListViewActivity.this).inflate(R.layout.listview_item, null);
                viewHolder = new ViewHolder();
                viewHolder.textview = (TextView) convertView.findViewById(R.id.textview);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.textview.setText(listData.get(position));
            return convertView;
        }

        class ViewHolder {
            public TextView textview;
        }
    }

    public void onBackClick(View view){
        finish();
    }
}
