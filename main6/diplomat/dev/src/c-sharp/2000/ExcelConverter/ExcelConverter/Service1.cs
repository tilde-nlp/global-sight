using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.ServiceProcess;
using System.IO;
using GlobalSight.Common;

namespace ExcelConverter
{
	public class Service1 : System.ServiceProcess.ServiceBase
	{
		/// <summary> 
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;
				
		private Logger m_log = null;
		private String m_watchDirName = null;
		private bool m_alreadyStarted = false;

		//maintain a separate ConverterRunner for import and export
		private ConverterRunner m_importConverterRunner = null;
		private ConverterRunner m_exportConverterRunner = null;
		private ExcelConverterImpl m_importConverter = null;
		private ExcelConverterImpl m_exportConverter = null;
				
		public Service1()
		{
			// This call is required by the Windows.Forms Component Designer.
			InitializeComponent();

			// TODO: Add any initialization after the InitComponent call
		}

		// The main entry point for the process
		static void Main()
		{
			System.ServiceProcess.ServiceBase[] ServicesToRun;
	
			// More than one user Service may run within the same process. To add
			// another service to this process, change the following line to
			// create a second service object. For example,
			//
			//   ServicesToRun = New System.ServiceProcess.ServiceBase[] {new Service1(), new MySecondUserService()};
			//
			ServicesToRun = new System.ServiceProcess.ServiceBase[] { new Service1() };

			System.ServiceProcess.ServiceBase.Run(ServicesToRun);
		}

		/// <summary> 
		/// Required method for Designer support - do not modify 
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			// 
			// Service1
			// 
			this.ServiceName = "GlobalSight Converter - Excel";

		}

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			if( disposing )
			{
				if (components != null) 
				{
					components.Dispose();
				}
			}
			base.Dispose( disposing );
		}

		/// <summary>
		/// Set things in motion so your service can do its work.
		/// </summary>
		protected override void OnStart(string[] args)
		{
			if (m_alreadyStarted == true)
				return;
			else
				m_alreadyStarted = true;
			try 
			{
				m_watchDirName = RegistryUtil.GetGlobalSightRegistryValue("MsOfficeConvDir") + @"\excel";
				DirectoryInfo watchDir = new DirectoryInfo(m_watchDirName);
				watchDir.Create();
				Logger.Initialize(m_watchDirName + @"\excelConverter.log");
				m_log = Logger.GetLogger();
				m_log.Log("GlobalSight Excel Converter starting up.");
				m_log.Log("Creating and starting threads to watch directory " + m_watchDirName);
				
				m_importConverter = new ExcelConverterImpl(ExcelConverterImpl.ConversionType.IMPORT);
				m_importConverterRunner = new ConverterRunner(m_importConverter, m_watchDirName);
				m_exportConverter = new ExcelConverterImpl(ExcelConverterImpl.ConversionType.EXPORT);
				m_exportConverterRunner = new ConverterRunner(m_exportConverter, m_watchDirName);
				
				m_importConverterRunner.Start();
				m_exportConverterRunner.Start();
			}
			catch (Exception e)
			{
				string msg = "ExcelConverter failed to initialize because of: " + e.Message + "\r\n" + e.StackTrace;
				EventLog.WriteEntry(msg,EventLogEntryType.Error);
				Logger.LogWithoutException(msg);
				throw e;
			}
		}
 
		/// <summary>
		/// Stop this service.
		/// </summary>
		protected override void OnStop()
		{
			// TODO: Add code here to perform any tear-down necessary to stop your service.
			m_log.Log("ExcelConverter shutting down.");
			m_importConverterRunner.Stop();
			m_exportConverterRunner.Stop();
			m_alreadyStarted = false;
			m_importConverter.QuitExcel();
			m_exportConverter.QuitExcel();
		}
	}
}
