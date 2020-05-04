package com.james.motion.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.blankj.utilcode.util.SPUtils;
import com.flyco.tablayout.SlidingTabLayout;
import com.james.motion.MyApplication;
import com.james.motion.R;
import com.james.motion.commmon.utils.Conn;
import com.james.motion.commmon.utils.MySp;
import com.james.motion.commmon.utils.Utils;
import com.james.motion.db.DataManager;
import com.james.motion.db.RealmHelper;
import com.james.motion.ui.BaseActivity;
import com.james.motion.ui.fragment.FastLoginFragment;
import com.james.motion.ui.fragment.PsdLoginFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;


public class LoginActivity extends BaseActivity {

    @BindView(R.id.slidingTabLayout)
    SlidingTabLayout slidingTabLayout;
    @BindView(R.id.vp)
    ViewPager vp;
    @BindView(R.id.btLogin)
    Button btLogin;
    @BindView(R.id.btReg)
    Button btReg;
    @BindView(R.id.wcLogin)
    ImageButton wcLogin;
    @BindView(R.id.qqLogin)
    ImageButton qqLogin;

    /**
     * 上次点击返回键的时间
     */
    private long lastBackPressed;

    //上次点击返回键的时间
    public static final int QUIT_INTERVAL = 2500;

    private final String[] mTitles = {"普通登录"};

    private ArrayList<Fragment> mFragments = new ArrayList<>();

    private boolean isPsd = true;//是否是密码登录

    private PsdLoginFragment psdLoginFragment = new PsdLoginFragment();
    private FastLoginFragment fastLoginFragment = new FastLoginFragment();

    private DataManager dataManager = null;

    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void initData(Bundle savedInstanceState) {

        dataManager = new DataManager(new RealmHelper());

        MyPagerAdapter mAdapter = new MyPagerAdapter(getSupportFragmentManager());
        vp.setAdapter(mAdapter);

        mFragments.add(psdLoginFragment);
      //  mFragments.add(fastLoginFragment);

        slidingTabLayout.setViewPager(vp, mTitles, this, mFragments);

        isPsd = true;
    }

    @Override
    public void initListener() {
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                isPsd = i == 0;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @OnClick({R.id.container, R.id.btLogin, R.id.btReg})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.container:
                hideSoftKeyBoard();
                break;
            case R.id.btLogin:

                    psdLoginFragment.checkAccount(this::login);

                break;
            case R.id.btReg:
                startActivity(new Intent(LoginActivity.this, RegistActivity.class));
                break;

            default:
                break;
        }
    }

    /**
     * 登录
     */
    public void login(String account, String psd) {
        btLogin.setEnabled(false);
        showLoadingView();
        new Handler().postDelayed(() -> {
            dismissLoadingView();
            btLogin.setEnabled(true);
            if (isPsd) {
                if (dataManager.checkAccount(account, psd))
                    loginSuccess(account, psd);
                else
                    showToast("账号或密码错误!");
            } else {
                if (dataManager.checkAccount(account))
                    loginSuccess(account, "");
                else
                    showToast("账号不存在!");
            }
        }, Conn.Delayed);
    }

    private void loginSuccess(String account, String psd) {
        SPUtils.getInstance().put(MySp.ISLOGIN, true);

        SPUtils.getInstance().put(MySp.USERID, account.substring(8));

        SPUtils.getInstance().put(MySp.PHONE, account);
        SPUtils.getInstance().put(MySp.PASSWORD, psd);

        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        Utils.showToast(LoginActivity.this, "恭喜您,登录成功...");

        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) { // 表示按返回键 时的操作
                long backPressed = System.currentTimeMillis();
                if (backPressed - lastBackPressed > QUIT_INTERVAL) {
                    lastBackPressed = backPressed;
                    showToast("再按一次退出");
                } else {
                    moveTaskToBack(false);
                    MyApplication.closeApp(this);
                    finish();
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }

    @Override
    protected void onDestroy() {
        if (null != dataManager)
            dataManager.closeRealm();
        super.onDestroy();
    }
}
