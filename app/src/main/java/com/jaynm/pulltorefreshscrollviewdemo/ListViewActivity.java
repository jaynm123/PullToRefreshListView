package com.jaynm.pulltorefreshscrollviewdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jaynm.pulltorefreshscrollviewdemo.refresh.PullToRefreshBase;
import com.jaynm.pulltorefreshscrollviewdemo.refresh.PullToRefreshListView;

import java.util.LinkedList;
import java.util.List;

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

    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        handler = new Handler();
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
     *
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
                convertView = LayoutInflater.from(ListViewActivity.this).inflate(R.layout.listview_item, null);
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

    public void onBackClick(View view) {
        finish();
    }

}
