package com.galjuergen.practiceplayer.controller;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.galjuergen.practiceplayer.model.PlayList;
import com.galjuergen.practiceplayer.view.PlayerActivity;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by juergen on 14/10/14.
 */
public class PlayerFragment extends Fragment
{
  private final static int INT_VOLUME_MAX = 100;
  private final static int INT_VOLUME_MIN = 0;
  private final static float FLOAT_VOLUME_MAX = 1;
  private final static float FLOAT_VOLUME_MIN = 0;
  protected MediaPlayer mPlayer;
  protected String currentSong;

  protected boolean mInitialized;
  protected boolean mPaused;
  protected boolean mRepeat;
  protected boolean mShuffle;

  protected PlayList mPlayList;
  protected Timer mFadeTimer;
  protected Timer mWaitTimer;
  private int mVolume;

  private int mPrefCropTime;
  private int mPrefPauseTime;
  private int mPrefFadeOutTime;
  private PlayerActivity mActivity;
  private PlayerBackgroundTask mTask;

  /**
   * Hold a reference to the parent Activity so we can report the
   * task's current progress and results. The Android framework
   * will pass us a reference to the newly created Activity after
   * each configuration change.
   */
  @Override
  public void onAttach(Activity activity)
  {
    super.onAttach(activity);
    mActivity = (PlayerActivity) activity;

    // Create and execute the background task.
    mTask = new PlayerBackgroundTask();
    mTask.execute();
  }

  /**
   * This method will only be called once when the retained
   * Fragment is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    // Retain this fragment across configuration changes.
    setRetainInstance(true);

    mPlayer   = new MediaPlayer();
    mPlayList = new PlayList();

    mInitialized = true;
    mPaused  = false;
    mRepeat  = false;
    mShuffle = false;

    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        mp.stop();
        waitFor(mPrefPauseTime);
      }


    });

    loadPrefs();
    //loadPlaylist();
  }

  /**
   * Set the callback to null so we don't accidentally leak the
   * Activity instance.
   */
  @Override
  public void onDetach()
  {
    super.onDetach();
    mActivity = null;
    mTask.cancel(true);
  }

  public void preparePlayer()
  {
    if(mActivity != null)
    {
      mActivity.onProgressUpdate(1, 0); // dummy
    }

    if(mPaused)
    {
      if (null != mWaitTimer)
      {
        mWaitTimer.cancel();
        mWaitTimer.purge();
      }

      if (null != mFadeTimer)
      {
        mFadeTimer.cancel();
        mFadeTimer.purge();
      }
    }

    try
    {
      currentSong = mPlayList.getNextSong();

      if (null == currentSong)
      {
        mPlayList.reset();
        mPlayer.reset();

        //if(null != mActivity)
        //{
        //  mActivity.onProgressUpdate(100L, 0L);
        //}
      }
      else
      {
        mPlayer.reset();
        mPlayer.setDataSource(currentSong);
        mPlayer.prepare();
      }

      mPaused = true;
      updateMetadata();

    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    catch (IllegalStateException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public void updateMetadata()
  {
    String title, artist, genre;

    if (null == currentSong)
    {
      title = null;
      artist = null;
      genre = null;
    } else
    {
      MediaMetadataRetriever mmr = new MediaMetadataRetriever();
      mmr.setDataSource(currentSong);

      byte[] art = mmr.getEmbeddedPicture();

      if (art != null)
      {
        //mAlbumImg.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
      } else
      {
        //mAlbumImg.setImageResource(null);
        // TODO
      }

      title  = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
      artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
      genre  = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
    }

    if(null != mActivity)
    {
      mActivity.onMetadataUpdate(title, artist, genre);
    }
  }

  public void playPrevSong()
  {
    String song = mPlayList.getPrevSong();

    if(null == song)
    {
      if(mShuffle)     playSong(mPlayList.getRandomSong());
      else if(mRepeat) playSong(mPlayList.getLastSong());
      else             preparePlayer();
    }
    else
    {
      playSong(song);
    }
  }

  public void playNextSong()
  {
    String song = mPlayList.getNextSong();

    if(null == song)
    {
      if(mShuffle)     playSong(mPlayList.getRandomSong());
      else if(mRepeat) playSong(mPlayList.getFirstSong());
      else             preparePlayer();
    }
    else
    {
      playSong(song);
    }
  }

  /**
   * Function to play next song
   */
  public boolean playSong(String song)
  {
    System.out.println("Playing song '" + song + "'");

    if(mPaused && null != mWaitTimer)
    {
      mWaitTimer.cancel();
      mWaitTimer.purge();
    }

    //mPaused = false;

    // Play song
    try
    {
      currentSong = song;

      if (null == currentSong)
      {
        preparePlayer();
        return false;
      } else
      {
        mPlayer.reset();
        mPlayer.setDataSource(currentSong);
        mPlayer.setVolume(FLOAT_VOLUME_MAX, FLOAT_VOLUME_MAX);
        mPlayer.prepare();
        mPlayer.start();

        mInitialized = false;
        mPaused = false;
        updateMetadata();
        return true;
      }

    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    catch (IllegalStateException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    return false;
  }

  public boolean playPause()
  {
    if (mPlayList.getSongCnt() == 0)
    {
      return false;
    }

    if (mPlayer.isPlaying())
    {
      mPlayer.pause();
      return false;
    }
    else
    {
      if(!mPaused)
      {
        mPlayer.start();
      }
      else if(mInitialized)
      {
        playSong(mPlayList.getFirstSong());
      }

      return true;
    }
  }

  public void pause()
  {
    mPaused = true;
    mPlayer.pause();
  }

  public void pause(int fadeOutS)
  {
    mPaused = true;
    fadeOut(fadeOutS);

  }

  public void fadeOut(int sec)
  {
    mVolume = INT_VOLUME_MAX;
    mFadeTimer = new Timer(true);
    TimerTask timerTask = new TimerTask()
    {
      @Override
      public void run()
      {
        if(!mPaused)
        {
          mFadeTimer.cancel();
          mFadeTimer.purge();
          mPlayer.setVolume(FLOAT_VOLUME_MAX, FLOAT_VOLUME_MAX);
          return;
        }

        mVolume--;

        System.out.println("fadeout volume " + mVolume);

        float fVolume = (float) mVolume / 100.0f;
        mPlayer.setVolume(fVolume, fVolume);

        if (mVolume <= INT_VOLUME_MIN)
        {
          //Pause music
          if (mPlayer.isPlaying()) mPlayer.pause();
          mPlayer.setVolume(FLOAT_VOLUME_MAX, FLOAT_VOLUME_MAX);

          mFadeTimer.cancel();
          mFadeTimer.purge();
        }
      }
    };

    // calculate delay, cannot be zero, set to 1 if zero
    int delay = 1000 * sec / INT_VOLUME_MAX;
    if (delay == 0) delay = 1;

    mFadeTimer.schedule(timerTask, delay, delay);
  }

  public void waitFor(int sec)
  {
    mWaitTimer = new Timer(true);
    TimerTask timerTask = new TimerTask()
    {
      @Override
      public void run()
      {
        if(mActivity != null)
        {
          mActivity.runOnUiThread(new Runnable()
          {
            @Override
            public void run()
            {
              playNextSong();
            }
          });
        }
      }
    };

    // calculate delay, cannot be zero, set to 1 if zero
    int delay = 1000 * sec;
    if (delay == 0) delay = 1;

    mWaitTimer.schedule(timerTask, delay);
  }

  public boolean isRepeat()
  {
    return mRepeat;
  }

  public void setRepeat(boolean repeat)
  {
    mRepeat = repeat;
  }

  public void toggleRepeat()
  {
    mRepeat = !mRepeat;
  }

  public boolean isShuffle()
  {
    return mShuffle;
  }

  public void setShuffle(boolean shuffle)
  {
    mShuffle = shuffle;
  }

  public void toggleShuffle()
  {
    mShuffle = !this.mShuffle;
  }

  public void loadPrefs()
  {
    SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

    String pref_crop_time_str    = mySharedPreferences.getString("pref_croptime"   , "90");
    String pref_pause_time_str   = mySharedPreferences.getString("pref_pausetime"  , "30");
    String pref_fadeout_time_str = mySharedPreferences.getString("pref_fadeouttime", "5" );

    mPrefCropTime    = Integer.parseInt(pref_crop_time_str);
    mPrefPauseTime   = Integer.parseInt(pref_pause_time_str);
    mPrefFadeOutTime = Integer.parseInt(pref_fadeout_time_str);
  }

  public void loadPlaylist(String filename)
  {
    mPlayList.readFromFile(filename);
    mInitialized = true;
    preparePlayer();
  }

  /**
   * Callback interface through which the fragment will report the
   * task's progress and results back to the Activity.
   */
  public static interface TaskCallbacks
  {
    void onPreExecute();

    void onMetadataUpdate(String... meta);

    void onProgressUpdate(long totalDuration, long currentDuration);

    void onCancelled();

    void onPostExecute();
  }

  /**
   * A dummy task that performs some (dumb) background work and
   * proxies progress updates and results back to the Activity.
   * <p/>
   * Note that we need to check if the callbacks are null in each
   * method in case they are invoked after the Activity's and
   * Fragment's onDestroy() method have been called.
   */
  private class PlayerBackgroundTask extends AsyncTask<Void, Long, Void>
  {
    protected int mPauseCnt;

    @Override
    protected void onPreExecute()
    {
      if (mActivity != null)
      {
        mActivity.onPreExecute();
      }
    }

    /**
     * Note that we do NOT call the callback object's methods
     * directly from the background thread, as this could result
     * in a race condition.
     */
    @Override
    protected Void doInBackground(Void... ignore)
    {
      while (!isCancelled())
      {
        SystemClock.sleep(100);
        long totalPlayDuration = mPrefCropTime * 1000;
        long totalDuration     = totalPlayDuration + mPrefFadeOutTime * 1000;
        long currentDuration;
        //boolean wait = false;

        if (mPlayer.isPlaying())
        {
          mPauseCnt = 0;
          currentDuration = mPlayer.getCurrentPosition();

          if (currentDuration >= totalPlayDuration) // TODO!!
          {
            if(!mPaused)
            {
              // wait for x seconds
              pause(mPrefFadeOutTime);
              waitFor(mPrefPauseTime);
            }

            //publishProgress(0L, totalDuration, currentDuration);
            //SystemClock.sleep(mPrefPauseTime * 1000);
            //nextSong = true;
           }

          publishProgress(totalDuration, currentDuration);
        }
      }
      return null;
    }

    @Override
    protected void onProgressUpdate(Long... progress)
    {
      //boolean wait = (progress[0] == 1);

      if (mActivity != null)
      {
        mActivity.onProgressUpdate(progress[0], progress[1]);

        //if (wait)
        //{
        //  waitFor(mPrefPauseTime);
        //  playNextSong();
        //}
      }
    }

    @Override
    protected void onCancelled()
    {
      if (mActivity != null)
      {
        preparePlayer();
        mActivity.onCancelled();
      }
    }

    @Override
    protected void onPostExecute(Void ignore)
    {
      if (mActivity != null)
      {
        mActivity.onPostExecute();
      }
    }
  }
}