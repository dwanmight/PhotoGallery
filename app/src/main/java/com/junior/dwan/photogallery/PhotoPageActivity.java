package com.junior.dwan.photogallery;

import android.support.v4.app.Fragment;

/**
 * Created by Might on 09.01.2017.
 */

public class PhotoPageActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new PhotoPageFragment();
    }
}
