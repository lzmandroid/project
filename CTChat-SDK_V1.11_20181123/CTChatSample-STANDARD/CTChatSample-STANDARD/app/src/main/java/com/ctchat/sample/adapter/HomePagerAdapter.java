package com.ctchat.sample.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.ctchat.sample.entity.TabBean;

import java.util.List;

public class HomePagerAdapter extends FragmentPagerAdapter {

    private List<TabBean> list;

    public HomePagerAdapter(FragmentManager fm, List<TabBean> list) {
        super(fm);
        this.list = list;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position).getFragment();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return list.get(position).getTitle();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
                super.destroyItem(container, position, object);
    }

    public List<TabBean> getData() {
        return list;
    }

    public void setData(List<TabBean> list) {
        this.list = list;
    }
}