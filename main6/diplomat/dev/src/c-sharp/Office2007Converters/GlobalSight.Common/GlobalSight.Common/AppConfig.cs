﻿using System;
using System.Collections.Generic;
using System.Text;
using System.Configuration;

namespace GlobalSight.Common
{
    public class AppConfig
    {
        public static string GetAppConfig(string strKey)
        {
            return ConfigurationManager.AppSettings[strKey];
        }

        public static void UpdateAppConfig(string newKey, string newValue)
        {
            bool isModified = false;
            foreach (string key in ConfigurationManager.AppSettings)
            {
                if (key == newKey)
                {
                    isModified = true;
                }
            }

            // Open App.Config of executable
            Configuration config =
                ConfigurationManager.OpenExeConfiguration(ConfigurationUserLevel.None);
            // You need to remove the old settings object before you can replace it
            if (isModified)
            {
                config.AppSettings.Settings.Remove(newKey);
            }
            // Add an Application Setting.
            config.AppSettings.Settings.Add(newKey, newValue);
            // Save the changes in App.config file.
            config.Save(ConfigurationSaveMode.Modified);
            // Force a reload of a changed section.
            ConfigurationManager.RefreshSection("appSettings");
        }

        public static bool AutoStartAll
        {
            set
            {
                try
                {
                    UpdateAppConfig("AutoStartAll", "" + value);
                }
                catch { }
            }
            get
            {
                try
                {
                    string v = GetAppConfig("AutoStartAll");
                    return "true".Equals(v, StringComparison.CurrentCultureIgnoreCase);
                }
                catch
                {
                    return false;
                }
            }
        }
    }
}
