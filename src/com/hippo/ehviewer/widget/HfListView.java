/*
 * Copyright (C) 2014 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer.widget;

import com.hippo.ehviewer.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HfListView extends SuperSwipeRefreshLayout
        implements AbsListView.OnScrollListener {
    
    private final static int FOOTER_REFRESHING = 0;
    private final static int FOOTER_SUCCESS = 1;
    private final static int FOOTER_FAIL = 2;
    
    private Context mContext;
    private ListView mListView;
    
    private View mFooter;
    private TextView mFooterTipTextView;
    private ProgressBar mFooterProgressBar;
    private int footerState = FOOTER_SUCCESS;
    private OnFooterRefreshListener mFooterRefreshListener;
    
    // Footer String to show
    private String mFooterRefreshStr;
    private String mFooterSuccessStr;
    private String mFooterFailStr;
    
    private boolean mIsEnabledHeader = true;
    private boolean mIsEnabledFooter = true;
    
    public HfListView(Context context) {
        super(context);
        mContext = context;
        init();
    }
    
    public HfListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }
    
    private void init() {
        mListView = new ListView(mContext);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mListView, lp);
        
        mListView.setOnScrollListener(this);
        mFooter = (LinearLayout)LayoutInflater.from(mContext)
                .inflate(R.layout.pull_list_view_footer, null);
        mFooterTipTextView = (TextView)mFooter.findViewById(R.id.footer_tip_text);
        mFooterProgressBar = (ProgressBar)mFooter.findViewById(R.id.footer_progressBar);
        mFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRefreshing() && footerState == FOOTER_FAIL)
                    footerRefresh();
            }
        });
        changeFooterViewByState();
        mListView.addFooterView(mFooter);
    }
    
    public ListView getListView() {
        return mListView;
    }
    
    public void setOnFooterRefreshListener(OnFooterRefreshListener l) {
        mFooterRefreshListener = l;
    }
    
    private void footerRefresh() {
        if (mFooterRefreshListener.onFooterRefresh()) {
            footerState = FOOTER_REFRESHING;
            changeFooterViewByState();
            
            // Disable header refresh
            if (mIsEnabledHeader)
                setEnabled(false);
        }
    }
    
    /**
     * Set footer UI String
     * 
     * @param refreshStr
     * @param successStr
     * @param failStr
     */
    public void setFooterString(String refreshStr, String successStr, String failStr) {
        mFooterRefreshStr = refreshStr;
        mFooterSuccessStr = successStr;
        mFooterFailStr = failStr;
        
        switch (footerState) {
        case FOOTER_REFRESHING:
            mFooterTipTextView.setText(mFooterRefreshStr);
            break;
        case FOOTER_SUCCESS:
            mFooterTipTextView.setText(mFooterSuccessStr);
            break;
        case FOOTER_FAIL:
            mFooterTipTextView.setText(mFooterFailStr);
            break;
        }
    }
    
    /**
     * Refresh footer UI
     */
    private void changeFooterViewByState() {
        switch (footerState) {
        case FOOTER_REFRESHING:
            mFooterTipTextView.setText(mFooterRefreshStr);
            mFooterProgressBar.setVisibility(View.VISIBLE);
            break;
        case FOOTER_SUCCESS:
            mFooterTipTextView.setText(mFooterSuccessStr);
            mFooterProgressBar.setVisibility(View.GONE);
            break;
        case FOOTER_FAIL:
            mFooterTipTextView.setText(mFooterFailStr);
            mFooterProgressBar.setVisibility(View.GONE);
            break;
        }
    }
    
    public void setHeaderRefreshComplete() {
        super.setRefreshing(false);
    }
    
    public void setFooterRefreshComplete(boolean isSuccess) {
        if (isSuccess)
            footerState = FOOTER_SUCCESS;
        else
            footerState = FOOTER_FAIL;
        changeFooterViewByState();
        
        // enable header refresh
        if (mIsEnabledHeader)
            setEnabled(true);
    }
    
    public void setAnyRefreshComplete() {
        setAnyRefreshComplete(true);
    }
    
    public void setAnyRefreshComplete(boolean isSuccess) {
        setHeaderRefreshComplete();
        setFooterRefreshComplete(isSuccess);
    }
    
    /**
     * @return True if actionbar is refreshing
     */
    public boolean isHeaderRefreshing() {
        return super.isRefreshing();
    }
    
    /**
     * @return True if footer is refreshing
     */
    public boolean isFooterRefreshing() {
        return footerState == FOOTER_REFRESHING;
    }
    
    /**
     * @return True if actionbar or footer is refreshing
     */
    public boolean isRefreshing() {
        return (mIsEnabledHeader ? isHeaderRefreshing() : false)
                | (mIsEnabledFooter ? isFooterRefreshing() : false);
    }
    
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (mIsEnabledFooter
                && firstVisibleItem + visibleItemCount == totalItemCount
                && !isRefreshing() && totalItemCount > 1
                && footerState != FOOTER_FAIL)
            footerRefresh();
    }
    
    public void setEnabledHeader(boolean enabled) {
        mIsEnabledHeader = enabled;
        setEnabled(enabled);
    }
    
    public void setEnabledFooter(boolean enabled) {
        if (mIsEnabledFooter = enabled)
            mListView.addFooterView(mFooter);
        else
            mListView.removeFooterView(mFooter);
    }
    
    public interface OnFooterRefreshListener {
        /**
         * @return True if this refresh action is vaild
         */
        boolean onFooterRefresh();
    }
}
