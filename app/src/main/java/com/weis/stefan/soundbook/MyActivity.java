package com.weis.stefan.soundbook;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Environment;
import java.io.File;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

import java.io.IOException;


public class MyActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    public String m_chosenDir = "";
    public String[] files;
    public int i = 0;
    public int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Open folder chooser on startup to select the folder where the audiobooks are located

        boolean m_newFolderEnabled = true;
        final Intent listIntent = new Intent(this, ListActivity.class);

            // Create DirectoryChooserDialog and register a callback
            DirectoryChooserDialog directoryChooserDialog =
                    new DirectoryChooserDialog(MyActivity.this,
                            new DirectoryChooserDialog.ChosenDirectoryListener()
                            {
                                @Override
                                public void onChosenDir(String chosenDir)
                                {
                                    m_chosenDir = chosenDir;
                                    Toast.makeText(
                                            MyActivity.this, "Chosen directory: " +
                                                    chosenDir, Toast.LENGTH_LONG).show();

                                    // write absolute path to file to string "dirPath"

                                    String dirPath = m_chosenDir + "/";

                                    File dir = new File(dirPath);
                                    files = dir.list();
                                    if (files.length == 0) {
                                        files[0] = "";
                                    }
                                    else {
                                        for (int i = 0; i < files.length; i++) {
                                            files[i] = dirPath + files[i];
                                        }
                                    }
                                }
                            });

            // Toggle new folder button enabling
            directoryChooserDialog.setNewFolderEnabled(m_newFolderEnabled);
            // Load directory chooser dialog for initial 'm_chosenDir' directory.
            // The registered callback will be called upon final directory selection.
            directoryChooserDialog.chooseDirectory(m_chosenDir);
            m_newFolderEnabled = ! m_newFolderEnabled;

        final MediaPlayer player = new MediaPlayer();

        // Pause music if pause button was clicked

        Button pauseButton = (Button) findViewById(R.id.pause);
        pauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (player.isPlaying()) {
                    position = player.getCurrentPosition();
                    player.pause();
                }
            }
        });

        // Open ListActivity when List-Botton is clicked

        Button listbutton = (Button) findViewById(R.id.listbutton);
        listbutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(listIntent);

            }
        });

        // Start or continue playing music if play button was clicked

        Button playButton = (Button) findViewById(R.id.pl);
        playButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                // Play first file in array

                try
                {
                    String filePath = files[i];

                    if (position == 0)
                        player.setDataSource(filePath);
                }

                catch (IOException e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (position == 0)
                        player.prepare();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                player.seekTo(position);
                player.start();


                // Play next file when previous media file ended

                player.setOnCompletionListener(new OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {

                        Log.i("Completion Listener","Song Complete");

                        mp.stop();
                        mp.reset();
                        i++;

                        if (i <= files.length) {
                            try {
                                mp.setDataSource(files[i]);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            try {
                                mp.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            mp.start();
                        }

                    }
                });

            }
        });

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.my, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_my, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MyActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
