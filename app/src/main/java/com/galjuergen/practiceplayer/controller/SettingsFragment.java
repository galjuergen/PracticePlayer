package com.galjuergen.practiceplayer.controller;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.galjuergen.practiceplayer.R;


public class SettingsFragment extends PreferenceFragment
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.pref_general);
  }
}