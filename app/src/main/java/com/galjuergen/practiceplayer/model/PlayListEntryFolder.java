package com.galjuergen.practiceplayer.model;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by juergen on 16/10/14.
 */
public class PlayListEntryFolder implements PlayListEntryInterface
{
  protected File mDir = null;
  protected LinkedList<PlayListEntryInterface> mChildEntries = new LinkedList<PlayListEntryInterface>();

  PlayListEntryFolder(String uri)
  {
    setURI(uri);
  }

  PlayListEntryFolder(File f)
  {
    setFile(f);
  }

  @Override
  public void setURI(String uri)
      throws IllegalArgumentException
  {
    setFile(new File(uri));
  }

  @Override
  public void setFile(File f)
      throws IllegalArgumentException
  {
    mDir = f;
    mChildEntries.clear();

    if (mDir.isDirectory())
    {
      for (File child : mDir.listFiles())
      {
        PlayListEntryInterface childEntry = PlayListEntryFactory.createPlayListEntry(child);
        mChildEntries.add(childEntry);
      }
    } else
    {
      throw new IllegalArgumentException("'" + mDir + "' is not a directory!");
    }
  }

  @Override
  public String getEntry()
  {
    int max = mChildEntries.size() - 1;
    double rand = Math.random();
    int idx = (int) (Math.round(rand * max));

    return mChildEntries.get(idx).getEntry();
  }
}
