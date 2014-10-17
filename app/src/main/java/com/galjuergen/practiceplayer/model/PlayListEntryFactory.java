package com.galjuergen.practiceplayer.model;

import java.io.File;

/**
 * Created by juergen on 17/10/14.
 */
public class PlayListEntryFactory
{
  public static PlayListEntryInterface createPlayListEntry(String uri)
    throws IllegalArgumentException
  {
    File f = new File(uri);
    return createPlayListEntry(f);
  }

  public static PlayListEntryInterface createPlayListEntry(File f)
    throws IllegalArgumentException
  {
    if (f.isFile())
    {
      return new PlayListEntryFile(f);
    } else if (f.isDirectory())
    {
      return new PlayListEntryFolder(f);
    }

    // not possible to reach this code in reality!
    throw new IllegalArgumentException("'" + f.getAbsolutePath() + "' is neither a file nor a directory!");
  }
}
