package com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

public class LocProfileReportGenerator
{
    private final int COL_LOCPROFILE_NAME = 0;
    private final int COL_WFTEMPLATE_NAME = 1;
    private ResourceBundle m_bundle;
    private CellStyle headerStyle = null;
    private CellStyle contentStyle = null;
    private String userId = null;
    private Locale m_uiLocale;
    
    public LocProfileReportGenerator()
    {
    }
    
    public LocProfileReportGenerator(HttpServletRequest p_request)
    {
        userId = (String) p_request.getSession(false).getAttribute(WebAppConstants.USER_NAME);
        m_uiLocale = Locale.US;
        m_bundle = SystemResourceBundle.getInstance().getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
    }

    // GBS-4312 generate localizationProfile report
    public File[] generateOneReport(HttpServletRequest p_request, HttpServletResponse p_response)
    {
        try
        {
            String[] l10nProfileIDs = (p_request.getParameterValues("radioBtn"));
            Workbook p_workbook = new SXSSFWorkbook();
            Sheet sheet = p_workbook.createSheet(m_bundle.getString("lb_workflows"));
            addSegmentHeader(p_workbook, sheet);
            writeSegmentInfo(p_workbook, sheet, l10nProfileIDs);
            if (p_workbook != null)
            {
                File file = ReportHelper.getXLSReportFile(getReportType(), null);
                FileOutputStream out = new FileOutputStream(file);
                p_workbook.write(out);
                out.close();
                ((SXSSFWorkbook) p_workbook).dispose();
                List<File> workBooks = new ArrayList<File>();
                workBooks.add(file);
                return ReportHelper.moveReports(workBooks, userId);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private String getReportType()
    {
        return ReportConstants.LOCPROFILE_WFTEMPLATE_REPORT;
    }

    private void writeSegmentInfo(Workbook p_workBook, Sheet p_sheet, String[] l10nProfileIDs)
    {
        try
        {
            int row = 1;
            for (String l10nProfileID : l10nProfileIDs)
            {
                L10nProfile l10nProfile = ServerProxy.getProjectHandler().getL10nProfile(
                        Long.parseLong(l10nProfileID));
                Collection wfTemplateInfos = l10nProfile.getWorkflowTemplateInfos();
                for (Object object : wfTemplateInfos)
                {
                    WorkflowTemplateInfo workflowTemplateInfo = (WorkflowTemplateInfo) object;
                    Row currentRow = getRow(p_sheet, row);
                    CellStyle srcStyle = getContentStyle(p_workBook);
                    Cell cell_A = getCell(currentRow, COL_LOCPROFILE_NAME);
                    cell_A.setCellValue(l10nProfile.getName());
                    cell_A.setCellStyle(srcStyle);

                    // Target segment
                    CellStyle trgStyle = getContentStyle(p_workBook);
                    Cell cell_B = getCell(currentRow, COL_WFTEMPLATE_NAME);
                    cell_B.setCellValue(workflowTemplateInfo.getName());
                    cell_B.setCellStyle(trgStyle);
                    
                    row++;    
                }
                row++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private CellStyle getContentStyle(Workbook p_workBook)
    {
        if (contentStyle == null)
        {
            CellStyle style = p_workBook.createCellStyle();
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            Font font = p_workBook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);

            contentStyle = style;
        }

        return contentStyle;
    }

    private void addSegmentHeader(Workbook p_workBook, Sheet p_sheet)
    {
        Row headerRow = getRow(p_sheet, 0);

        Cell cell_A = getCell(headerRow, COL_LOCPROFILE_NAME);
        cell_A.setCellValue(m_bundle.getString("lb_loc_profiles"));
        cell_A.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(COL_LOCPROFILE_NAME, 40 * 256);

        Cell cell_B = getCell(headerRow, COL_WFTEMPLATE_NAME);
        cell_B.setCellValue(m_bundle.getString("lb_workflow_names"));
        cell_B.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(COL_WFTEMPLATE_NAME, 40 * 256);
    }

    private CellStyle getHeaderStyle(Workbook p_workBook)
    {
        if (headerStyle == null)
        {
            Font font = p_workBook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            font.setColor(IndexedColors.BLUE.getIndex());
            font.setUnderline(Font.U_NONE);
            font.setFontName("Times");
            font.setFontHeightInPoints((short) 11);

            CellStyle cs = p_workBook.createCellStyle();
            cs.setFont(font);
            cs.setWrapText(true);
            cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cs.setBorderTop(CellStyle.BORDER_THIN);
            cs.setBorderRight(CellStyle.BORDER_THIN);
            cs.setBorderBottom(CellStyle.BORDER_THIN);
            cs.setBorderLeft(CellStyle.BORDER_THIN);

            headerStyle = cs;
        }

        return headerStyle;
    }

    private Cell getCell(Row p_row, int index)
    {
        Cell cell = p_row.getCell(index);
        if (cell == null)
            cell = p_row.createCell(index);
        return cell;
    }

    private Row getRow(Sheet p_sheet, int p_row)
    {
        Row row = p_sheet.getRow(p_row);
        if (row == null)
            row = p_sheet.createRow(p_row);
        return row;
    }
}