package clockworktt.gaby.com;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by user on 1/23/2018.
 */

class SectionPagerAdapter extends FragmentPagerAdapter{


    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                InvitedFragment invitedFragment = new InvitedFragment();
                return invitedFragment;
            case 1:
                ArrivedFragment arrivedFragment = new ArrivedFragment();
                return arrivedFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position){

        switch (position){
            case 0:
                return "Invited";
            case 1:
                return "Arrived";
            default:
                return null;
        }

    }

}
