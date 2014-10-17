package com.galjuergen.practiceplayer.model;

import java.io.File;

/**
 * Created by juergen on 16/10/14.
 */
public class PlayListEntryFile implements PlayListEntryInterface
{
  protected File mFile = null;

  PlayListEntryFile(String uri)
  {
    setURI(uri);
  }

  PlayListEntryFile(File f)
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
    if (!f.isFile())
    {
      throw new IllegalArgumentException("'" + f.getAbsolutePath() + "' is not a file!");
    }
    mFile = f;
  }

  @Override
  public String getEntry()
  {
    if (null == mFile)
      return null;

    return mFile.getAbsolutePath();
  }
}
