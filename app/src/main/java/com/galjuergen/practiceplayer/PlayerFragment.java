package com.galjuergen.practiceplayer;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by juergen on 14/10/14.
 */
public class PlayerFragment extends Fragment
{
    protected boolean mPaused = false;

    protected MediaPlayer mPlayer = new MediaPlayer();

    protected String currentSong;

    protected PlayList mPlayList = new PlayList();
    protected Timer mFadeTimer;

    private final static int INT_VOLUME_MAX = 100;
    private final static int INT_VOLUME_MIN = 0;
    private final static float FLOAT_VOLUME_MAX = 1;
    private final static float FLOAT_VOLUME_MIN = 0;
    private int mVolume;

    private int mPrefCropTime;
    private int mPrefPauseTime;
    private int mPrefFadeOutTime;

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    static interface TaskCallbacks {
        void onPreExecute();
        void onMetadataUpdate(String... meta);
        void onProgressUpdate(long totalDuration, long currentDuration);
        void onCancelled();
        void onPostExecute();
    }

    private PlayerActivity mActivity;
    private PlayerBackgroundTask mTask;

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (PlayerActivity)activity;
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        // Create and execute the background task.
        mTask = new PlayerBackgroundTask();
        mTask.execute();


        loadPrefs();
        //loadPlaylist();
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    public void preparePlayer()
    {
        mActivity.onProgressUpdate(1, 0); // dummy

        try {
            currentSong = mPlayList.getNextSong();

            if(null == currentSong)
            {
                mPlayList.reset();
                mPlayer.reset();
            }
            else
            {
                mPlayer.reset();
                mPlayer.setDataSource(currentSong);
                mPlayer.prepare();
            }

            updateMetadata();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateMetadata() {
        String title, artist, genre;

        if (null == currentSong) {
            title = null;
            artist = null;
            genre = null;
        } else {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(currentSong);

            byte[] art = mmr.getEmbeddedPicture();

            if (art != null) {
                //mAlbumImg.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
            } else {
                //mAlbumImg.setImageResource(null);
                // TODO
            }

            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
        }

        mActivity.onMetadataUpdate(title, artist, genre);
    }

    public void playPrevSong() {
        playSong(mPlayList.getPrevSong());
    }

    public void playNextSong() {
        playSong(mPlayList.getNextSong());
    }

    /**
     * Function to play next song
     * */
    public void  playSong(String song) {
        System.out.println("Playing song '" + song + "'");

        mPaused = false;

        // Play song
        try {
            currentSong = song;

            if(null == currentSong)
            {
                preparePlayer();
            }
            else
            {
                mPlayer.reset();
                mPlayer.setDataSource(currentSong);
                mPlayer.setVolume(FLOAT_VOLUME_MAX, FLOAT_VOLUME_MAX);
                mPlayer.prepare();
                mPlayer.start();

                updateMetadata();
            }

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playPause()
    {
        if (!mPaused) {
            if (mPlayer.isPlaying()) mPlayer.pause();
            else mPlayer.start();
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
                mVolume--;

                float fVolume = (float)mVolume/100.0f;
                mPlayer.setVolume(fVolume, fVolume);

                if (mVolume == INT_VOLUME_MIN)
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
        int delay = 1000 * sec/INT_VOLUME_MAX;
        if (delay == 0) delay = 1;

        mFadeTimer.schedule(timerTask, delay, delay);
    }

    public void loadPrefs() {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

        String pref_crop_time_str    = mySharedPreferences.getString("pref_croptime",   "90");
        String pref_pause_time_str   = mySharedPreferences.getString("pref_pausetime",  "30");
        String pref_fadeout_time_str = mySharedPreferences.getString("pref_fadeouttime", "5");

        mPrefCropTime    = Integer.parseInt(pref_crop_time_str);
        mPrefPauseTime   = Integer.parseInt(pref_pause_time_str);
        mPrefFadeOutTime = Integer.parseInt(pref_fadeout_time_str);
    }

    public void loadPlaylist(String filename)
    {
        mPlayList.readFromFile(filename);
        preparePlayer();
    }

    /**
     * A dummy task that performs some (dumb) background work and
     * proxies progress updates and results back to the Activity.
     *
     * Note that we need to check if the callbacks are null in each
     * method in case they are invoked after the Activity's and
     * Fragment's onDestroy() method have been called.
     */
    private class PlayerBackgroundTask extends AsyncTask<Void, Long, Void> {

        @Override
        protected void onPreExecute() {
            if (mActivity != null) {
                mActivity.onPreExecute();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected Void doInBackground(Void... ignore) {
            while (!isCancelled())
            {
                SystemClock.sleep(100);
                long    totalDuration = mPrefCropTime * 1000;
                long    currentDuration;
                boolean nextSong = false;

                if(mPlayer.isPlaying())
                {
                    //totalDuration   = mPlayer.getDuration();
                    currentDuration = mPlayer.getCurrentPosition();

                    if (currentDuration >= totalDuration) // TODO!!
                    {
                        // wait for 5 seconds
                        pause(mPrefFadeOutTime);

                        publishProgress((long)0, totalDuration, currentDuration);
                        SystemClock.sleep(mPrefPauseTime * 1000);
                        nextSong = true;
                    }
                }
                else
                {
                    //totalDuration   = 100;
                    currentDuration = 0;
                }

                publishProgress((long)(nextSong ? 1 : 0), totalDuration, currentDuration);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Long... progress) {
            boolean nextSong = (progress[0] == 1);

            if (mActivity != null) {
                mActivity.onProgressUpdate(progress[1], progress[2]);

                if(nextSong) {
                    playNextSong();
                }
            }
        }

        @Override
        protected void onCancelled() {
            preparePlayer();
            if (mActivity != null) {
                mActivity.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(Void ignore) {
            if (mActivity != null) {
                mActivity.onPostExecute();
            }
        }
    }
}