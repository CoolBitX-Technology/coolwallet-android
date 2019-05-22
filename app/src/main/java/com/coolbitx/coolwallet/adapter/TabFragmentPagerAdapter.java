package com.coolbitx.coolwallet.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;

import com.coolbitx.coolwallet.ui.Fragment.BaseFragment;

import java.util.LinkedList;
/**
 * Created by ShihYi on 2015/12/15.
 */
public class TabFragmentPagerAdapter extends FragmentStatePagerAdapter {

    LinkedList<BaseFragment> fragments = null;
    private FragmentManager fm = null;
    private  FragmentTransaction ft = null;
    public TabFragmentPagerAdapter(FragmentManager fm, LinkedList<BaseFragment> fragments) {
        super(fm);
        this.fm = fm;
        ft = fm.beginTransaction();
        if (fragments == null) {
            this.fragments = new LinkedList<BaseFragment>();
        }else{
            this.fragments = fragments;
        }
    }

    @Override
    public BaseFragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragments.get(position).getTitle();
    }

    /***
     * 將fragment全部移除
     * @param fragments -- fragment list
     */
    public void clearFragment(LinkedList<BaseFragment> fragments) {
        if(ft == null){
            ft = fm.beginTransaction();
        }
        for (int i = 0; i < fragments.size(); i++) {
            ft.remove(fragments.get(i));
        }
        ft.commit();
        //ft.commitAllowingStateLoss();

    }
}
