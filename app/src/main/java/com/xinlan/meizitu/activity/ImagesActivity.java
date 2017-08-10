package com.xinlan.meizitu.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.xinlan.meizitu.config.Constant;
import com.xinlan.meizitu.R;
import com.xinlan.meizitu.data.Node;
import com.xinlan.meizitu.data.Resource;
import com.xinlan.meizitu.data.Trans;
import com.xinlan.meizitu.fragment.ImageFragment;
import com.xinlan.meizitu.task.ImageNodeTask;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ImagesActivity extends BaseActivity {
    public static void start(Activity context, int pos) {
        Intent it = new Intent(context, ImagesActivity.class);
        it.putExtra(Constant.INTENT_PARAM_POS, pos);
        context.startActivity(it);
    }

    private int mPos;
    private Node mNode;
    private ImageNodeTask mImagesTask;

    private ViewPager mGallery;
    private ImagesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        initUI();
        initAction();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Trans bean) {
        if (bean == null)
            return;

        switch (bean.cmd) {
            case Constant.CMD_NODE_LIST_GET:
                mNode = Resource.getInstance().findImageNode(mPos);
                if(mNode.getChildList() == null){
                    Toast.makeText(this,R.string.not_get_data,Toast.LENGTH_SHORT).show();
                    return;
                }
                refreshImageList();
                break;
        }//end switch
    }

    private void initUI() {
        mGallery = (ViewPager)findViewById(R.id.gallery);
    }

    private void refreshImageList(){
        if(mNode.getChildList() == null){
            return;
        }

        mAdapter = new ImagesAdapter(this.getSupportFragmentManager());
        mGallery.setAdapter(mAdapter);
    }

    private void initAction() {
        mPos = getIntent().getIntExtra(Constant.INTENT_PARAM_POS, -1);
        if (mPos < 0)
            return;

        mNode = Resource.getInstance().findImageNode(mPos);
        if (mNode == null)
            return;

        if (mNode.getChildList() == null) {//load data
            mImagesTask = new ImageNodeTask(mNode);
            mImagesTask.execute(mNode.getLink());
        }else{
            refreshImageList();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImagesTask != null) {
            mImagesTask.cancel(true);
        }
    }

    private final class ImagesAdapter extends FragmentStatePagerAdapter{
        public ImagesAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Node n = mNode.getChildList().get(position);
            return ImageFragment.newInstance(n.getImage(),n.getRefer());
        }

        @Override
        public int getCount() {
            return mNode.getChildList().size();
        }
    }
}//end class
