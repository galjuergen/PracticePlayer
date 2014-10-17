package com.galjuergen.practiceplayer.model;

import java.io.File;

/**
 * Created by juergen on 16/10/14.
 */
public interface PlayListEntryInterface
{
  void setURI(String uri);

  void setFile(File f);

  String getEntry();
}
