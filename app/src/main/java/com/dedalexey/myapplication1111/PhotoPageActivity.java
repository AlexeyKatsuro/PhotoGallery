package com.dedalexey.myapplication1111;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

/**
 * Created by Alexey on 21.02.2018.
 */

public class PhotoPageActivity extends SingleFragmentActivity {

    PhotoPageFragment mPhotoPageFragment;

    public static Intent newIntent(Context context, Uri photoPageUri){
        Intent intent = new Intent(context,PhotoPageActivity.class);
        intent.setData(photoPageUri);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return mPhotoPageFragment=PhotoPageFragment.newInstance(getIntent().getData());
    }

    @Override
    public void onBackPressed() {
        if(!mPhotoPageFragment.canWebViewGoBack()) {
            super.onBackPressed();
        }
    }
}
