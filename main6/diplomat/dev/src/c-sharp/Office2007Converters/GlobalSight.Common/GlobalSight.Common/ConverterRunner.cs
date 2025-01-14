using System;
using System.Threading;
using System.IO;

namespace GlobalSight.Common
{
	/// <summary>
	/// A ConverterRunner will watch a given directory for appropriate files
	/// and then invoke its associated Converter to convert the appropriate
	/// documents that appear in the watched directory.
	/// 
	/// The conversion is run in a separate thread.
	/// </summary>
	public class ConverterRunner
	{
		private Logger m_log = null;
		private Thread m_watchThread = null;
		private String m_watchDirName = null;
		private bool m_keepWatching = true;
		private Converter m_converter = null;
		private DirectoryInfo m_watchDirInfo = null;
        private Thread m_testThread = null;


		/// <summary>
		/// Creates a ConverterRunner capable of watching a
		/// directory and invoking a converter for appropriate files
		/// that appear in that directory.
		/// </summary>
		/// <param name="p_converter"></param>
		/// <param name="p_watchDirectory"></param>
		public ConverterRunner(Converter p_converter, string p_watchDirectory, Logger p_log)
		{
			m_watchDirName = p_watchDirectory;
			m_watchDirInfo = new DirectoryInfo(m_watchDirName);
			m_converter = p_converter;
			m_watchThread = new Thread(new ThreadStart(Watch));

            m_testThread = new Thread(new ThreadStart(WatchTest));
            m_log = p_log;
		}

		/// <summary>
		/// Starts the ConverterRunner up to watch the watch directory.
		/// </summary>
		public void Start()
		{
			m_keepWatching = true;
			m_watchThread.Start();
            m_testThread.Start();
		}

		/// <summary>
		/// Stops the ConverterRunner.
		/// </summary>
		public void Stop()
		{
			m_keepWatching = false;
		}

		/// <summary>
		/// Watches the watch directory and invokes the converter to convert any files
		/// with the correct file extension.
		/// </summary>
		private void Watch()
		{
			while (m_keepWatching)
			{
				try 
				{
					ScanDirectory();
				}
				catch (Exception e)
				{
					Logger.LogError(m_log, "Failed to scan directory",e);
				}
				Thread.Sleep(2000);
			}
		}

        private void WatchTest()
        {
            while (m_keepWatching)
            {
                try
                {
                    ScanDirectoryTest();
                }
                catch (Exception e)
                {
                    Logger.LogError(m_log, "Failed to scan directory", e);
                }
                Thread.Sleep(1000);
            }
        }
	
		/// <summary>
		/// Scans the watch directory for appropriate files and then invokes the converter.
		/// On each scan, it will grab the first file in each language directory and process it.
		/// NOTE: This assumes that files to process are being queued up in the adapter, because
		/// this could potentially lead to starvation as some document in the directory may
		/// never get processed.
		/// </summary>
		private void ScanDirectory()
		{
			foreach (DirectoryInfo langDir in m_watchDirInfo.GetDirectories())
			{
				FileInfo[] files = langDir.GetFiles(m_converter.GetFileExtensionToWatch());
				if (files.Length > 0)
				{
					FileInfo file = files[0];
					string srcLanguage = langDir.Name.Substring(0,2);
					m_converter.Convert(file.FullName, srcLanguage);
				}
			}
		}

        private void ScanDirectoryTest()
        {
            foreach (FileInfo f in m_watchDirInfo.GetFiles(m_converter.GetTestFileToWatch()))
            { 
                f.Delete();
            }
        }
	}
}
