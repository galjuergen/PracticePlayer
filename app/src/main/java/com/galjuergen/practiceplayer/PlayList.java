package com.galjuergen.practiceplayer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by juergen on 12/10/14.
 */
public class PlayList {
    protected LinkedList<String> mSongList;
    protected int mSongIdx = -1;

    public PlayList() { }

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

    public void readFromFile(String filePath)
    {
        try
        {
            mSongList = new LinkedList<String>();
            mSongIdx  = -1;

            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;

            while ((line = reader.readLine()) != null)
            {
                if(!line.startsWith("#"))
                {
                    mSongList.add(line);
                }
            }
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public String getPrevSong()
    {
        if(mSongIdx <= 0) mSongIdx = 1;

        if((mSongList != null) && !mSongList.isEmpty() && (mSongIdx <= mSongList.size()))
        {
            mSongIdx--;
            String song = mSongList.get(mSongIdx);

            return song;
        }

        return null;
    }

    public String getNextSong()
    {
        if((mSongList != null) && !mSongList.isEmpty() && (mSongIdx < mSongList.size() - 1))
        {
            mSongIdx++;
            String song = mSongList.get(mSongIdx);
            return song;
        }

        return null;
    }
}
