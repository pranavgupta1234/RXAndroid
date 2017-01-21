package pranav.apps.amazing.rxandroid;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Pranav Gupta on 1/21/2017.
 */

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new MainFragment(), this.toString())
                    .commit();
        }
    }
}
