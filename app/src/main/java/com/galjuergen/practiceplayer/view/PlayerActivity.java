package com.galjuergen.practiceplayer.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.galjuergen.practiceplayer.R;
import com.galjuergen.practiceplayer.controller.PlayerFragment;
import com.galjuergen.practiceplayer.controller.SettingsFragment;
import com.galjuergen.practiceplayer.controller.Utilities;

import java.io.File;


public class PlayerActivity extends Activity implements PlayerFragment.TaskCallbacks
{

  private static final String TAG_TASK_FRAGMENT = "player_fragment";
  protected Utilities utils = new Utilities();
  protected ImageView mAlbumImg;
  protected TextView mTitleLbl;
  protected TextView mArtistLbl;
  protected TextView mGenreLbl;
  protected TextView mDurationLbl;
  protected ProgressBar mDurationProgress;
  protected TextView mPercentageLbl;
  protected Button mPrevBtn;
  protected Button mPlayPauseBtn;
  protected Button mNextBtn;
  protected ImageButton mRepeatIBtn;
  protected ImageButton mShuffleIBtn;
  protected Button mLoadPlayListBtn;
  private PlayerFragment mPlayerFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);

    FragmentManager fm = getFragmentManager();
    mPlayerFragment = (PlayerFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

    // If the Fragment is non-null, then it is currently being
    // retained across a configuration change.
    if (mPlayerFragment == null)
    {
      mPlayerFragment = new PlayerFragment();
      fm.beginTransaction().add(mPlayerFragment, TAG_TASK_FRAGMENT).commit();
    }

    mAlbumImg = (ImageView) findViewById(R.id.album_img);
    mTitleLbl = (TextView) findViewById(R.id.title_lbl);
    mArtistLbl = (TextView) findViewById(R.id.artist_lbl);
    mGenreLbl = (TextView) findViewById(R.id.genre_lbl);

    mDurationLbl = (TextView) findViewById(R.id.duration_lbl);
    mDurationProgress = (ProgressBar) findViewById(R.id.duration_progress);
    mPercentageLbl = (TextView) findViewById(R.id.percentage_lbl);

    mPrevBtn = (Button) findViewById(R.id.prev_btn);
    mPlayPauseBtn = (Button) findViewById(R.id.play_pause_btn);
    mNextBtn = (Button) findViewById(R.id.next_btn);
    mRepeatIBtn = (ImageButton) findViewById(R.id.repeat_ibtn);
    mShuffleIBtn = (ImageButton) findViewById(R.id.shuffle_ibtn);

    mDurationProgress.setProgress(0);
    mDurationProgress.setMax(100);

    mPrevBtn.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View v)
      {
        mPlayerFragment.playPrevSong();
      }
    });

    mPlayPauseBtn.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View v)
      {
        boolean isPlaying = mPlayerFragment.playPause();

        if (isPlaying)
        {
          mPlayPauseBtn.setText(R.string.pause);
        } else
        {
          mPlayPauseBtn.setText(R.string.play);
        }
      }
    });

    mNextBtn.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View v)
      {
        mPlayerFragment.playNextSong();
      }
    });

    mRepeatIBtn.setOnClickListener(new View.OnClickListener()
    {
      private boolean mIsChecked = mPlayerFragment.isRepeat();

      @Override
      public void onClick(View v)
      {
        ImageButton ib = (ImageButton) v;
        mIsChecked = !mIsChecked;

        mPlayerFragment.setRepeat(mIsChecked);

        if (mIsChecked) ib.setColorFilter(Color.argb(0xff, 0x33, 0xb5, 0xe5));
        else ib.setColorFilter(Color.argb(255, 255, 255, 255));
      }
    });

    mShuffleIBtn.setOnClickListener(new View.OnClickListener()
    {
      private boolean mIsChecked = mPlayerFragment.isShuffle();

      @Override
      public void onClick(View v)
      {
        ImageButton ib = (ImageButton) v;
        mIsChecked = !mIsChecked;

        mPlayerFragment.setShuffle(mIsChecked);

        if (mIsChecked) ib.setColorFilter(Color.argb(0xff, 0x33, 0xb5, 0xe5));
        else ib.setColorFilter(Color.argb(255, 255, 255, 255));
      }
    });
  }

  @Override
  protected void onStart()
  {
    super.onStart();
  }

  @Override
  public void onPreExecute()
  {
  }

  @Override
  public void onPostExecute()
  {
  }

  @Override
  public void onCancelled()
  {
  }

  @Override
  public void onProgressUpdate(long totalDuration, long currentDuration)
  {
    int progress = utils.getProgressPercentage(currentDuration, totalDuration);

    mDurationLbl.setText("" + utils.milliSecondsToTimer(currentDuration));
    mPercentageLbl.setText("" + progress + "%");
    mDurationProgress.setProgress(progress);
  }

  public void onMetadataUpdate(String... meta)
  {
    String title = meta[0];
    String artist = meta[1];
    String genre = meta[2];

    if (null != title && "" != title) mTitleLbl.setText(title);
    else mTitleLbl.setText(R.string.title);

    if (null != artist && "" != artist) mArtistLbl.setText(artist);
    else mArtistLbl.setText(R.string.artist);

    if (null != genre && "" != genre) mGenreLbl.setText(genre);
    else mGenreLbl.setText(R.string.genre);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.player, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    switch(id)
    {
      case R.id.action_settings:
        mPlayerFragment.pause();

        Intent intent = new Intent();
        intent.setClass(PlayerActivity.this, SettingsActivity.class);
        startActivityForResult(intent, 0);

        return true;

      case R.id.action_load_playlist:
        loadPlaylist();
        return true;

      default:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    //super.onActivityResult(requestCode, resultCode, data);

    Context context = getApplicationContext();
    int duration = Toast.LENGTH_SHORT;
    Toast toast = Toast.makeText(context, R.string.prefs_updated, duration);
    toast.show();

    mPlayerFragment.loadPrefs();
  }

  public void loadPlaylist()
  {
    //Create FileOpenDialog and register a callback
    SimpleFileDialog fileOpenDialog = new SimpleFileDialog(
        PlayerActivity.this,
        "FileOpen..",
        new SimpleFileDialog.SimpleFileDialogListener()
        {
          @Override
          public void onChosenDir(String filename)
          {
            // The code in this function will be executed when the dialog OK button is pushed

            // load playlist
            mPlayerFragment.loadPlaylist(filename);

            // reset play/pause button
            mPlayPauseBtn.setText(R.string.play);

            // store latest directory
            File f = new File(filename);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PlayerActivity.this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("pref_last_dir", f.getParent());
            editor.commit();

            // inform user by a toast
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, R.string.playlist_loaded, duration);
            toast.show();
          }
        }
    );
    //You can change the default filename using the public variable "Default_File_Name"
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PlayerActivity.this);
    fileOpenDialog.default_file_name = prefs.getString("pref_last_dir", "/");
    fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
  }

  private void showUserSettings()
  {
    Intent intent = new Intent();
    intent.setClass(this, SettingsFragment.class);
    startActivityForResult(intent, 0);
  }
}
