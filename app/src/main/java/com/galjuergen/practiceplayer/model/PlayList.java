package com.galjuergen.practiceplayer.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by juergen on 12/10/14.
 */
public class PlayList
{
  protected LinkedList<PlayListEntryInterface> mSongList = new LinkedList<PlayListEntryInterface>();
  protected int mSongIdx = -1;

  public PlayList()
  {
  }

  public PlayList(String filePath)
  {
    readFromFile(filePath);
  }

  public void reset()
  {
    mSongIdx = -1;
  }

  public void clear()
  {
    mSongList.clear();
    reset();
  }

  public int getSongCnt()
  {
    return mSongList.size();
  }

  public void readFromFile(String filePath)
  {
    try
    {
      mSongIdx = -1;
      mSongList.clear();

      BufferedReader reader = new BufferedReader(new FileReader(filePath));
      String line;

      while ((line = reader.readLine()) != null)
      {
        if (!line.startsWith("#"))
        {
          mSongList.add(PlayListEntryFactory.createPlayListEntry(line));
        }
      }
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public String getPrevSong()
  {
    if (mSongIdx <= 0) mSongIdx = 1;

    if ((mSongList != null) && !mSongList.isEmpty() && (mSongIdx <= mSongList.size()))
    {
      mSongIdx--;
      PlayListEntryInterface entry = mSongList.get(mSongIdx);

      if (null != entry)
        return entry.getEntry();
    }

    return null;
  }

  public String getNextSong()
  {
    if ((mSongList != null) && !mSongList.isEmpty() && (mSongIdx < mSongList.size() - 1))
    {
      mSongIdx++;
      PlayListEntryInterface entry = mSongList.get(mSongIdx);

      if (null != entry)
        return entry.getEntry();
    }

    return null;
  }

  public String getFirstSong()
  {
    if ((mSongList != null) && !mSongList.isEmpty())
    {
      mSongIdx = 0;
      PlayListEntryInterface entry = mSongList.get(mSongIdx);

      if (null != entry)
        return entry.getEntry();
    }

    return null;
  }

  public String getLastSong()
  {
    if ((mSongList != null) && !mSongList.isEmpty())
    {
      mSongIdx = mSongList.size() - 1;
      PlayListEntryInterface entry = mSongList.get(mSongIdx);

      if (null != entry)
        return entry.getEntry();
    }

    return null;
  }

  public String getRandomSong()
  {
    if ((mSongList != null) && !mSongList.isEmpty())
    {
      int max = mSongList.size() - 1;
      mSongIdx = (int)Math.round(Math.random() * max);

      PlayListEntryInterface entry = mSongList.get(mSongIdx);

      if (null != entry)
        return entry.getEntry();
    }

    return null;
  }
}
