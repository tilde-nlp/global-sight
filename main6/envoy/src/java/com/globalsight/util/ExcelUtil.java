/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.util;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class ExcelUtil
{
    private static Logger logger = Logger.getLogger(ExcelUtil.class);

    public static boolean isXls(String filename)
    {
        if (StringUtil.isEmpty(filename))
            return false;
        return filename.toLowerCase().endsWith(".xls");
    }

    public static boolean isXlsx(String filename)
    {
        if (StringUtil.isEmpty(filename))
            return false;
        return filename.toLowerCase().endsWith(".xlsx");
    }

    public static boolean isExcel(String filename)
    {
        if (StringUtil.isEmpty(filename))
            return false;
        return isXls(filename) || isXlsx(filename);
    }

    public static Workbook getWorkbook(File file)
    {
        return getWorkbook(file.getAbsolutePath());
    }

    public static Workbook getWorkbook(String filename)
    {
        Workbook workbook = null;
        if (StringUtil.isEmpty(filename) || !isExcel(filename))
            return null;
        File file = null;
        try
        {
            file = new File(filename);
            if (!file.exists() || file.isDirectory())
                return null;
            InputStream is = new FileInputStream(file);
            if (isXls(filename))
                workbook = new HSSFWorkbook(is);
            else
                workbook = new XSSFWorkbook(OPCPackage.open(file));
        }
        catch (Exception e)
        {
            logger.error("Cannot open Excel file correctly.", e);
        }

        return workbook;
    }

    public static Workbook getWorkbook(String filename, InputStream is)
    {
        Workbook workbook = null;
        if (StringUtil.isEmpty(filename) || !isExcel(filename) || is == null)
            return null;

        File file = null;
        try
        {
            file = new File(filename);
            if (!file.exists() || file.isDirectory())
                return null;
            if (isXls(filename))
                workbook = new HSSFWorkbook(is);
            else
                workbook = new XSSFWorkbook(OPCPackage.open(is));
        }
        catch (Exception e)
        {
            logger.error("Cannot open Excel file correctly.", e);
        }

        return workbook;
    }

    public static Sheet getSheet(Workbook workbook, String sheetName)
    {
        if (workbook == null || StringUtil.isEmpty(sheetName))
            return null;

        return workbook.getSheet(sheetName);
    }

    public static Sheet getSheet(Workbook workbook, int sheetNumber)
    {
        if (workbook == null || sheetNumber < 0)
            return null;

        Sheet sheet = workbook.getSheetAt(sheetNumber);
        if (sheet == null)
            sheet = workbook.createSheet();

        return sheet;
    }

    public static Sheet getDefaultSheet(Workbook workbook)
    {
        if (workbook == null)
            return null;

        return workbook.getSheetAt(0);
    }

    public static Row getRow(Sheet sheet, int index)
    {
        Row row = sheet.getRow(index);
        if (row == null)
            row = sheet.createRow(index);
        return row;
    }

    public static Cell getCell(Row row, int index)
    {
        Cell cell = row.getCell(index);
        if (cell == null)
            cell = row.createCell(index);
        return cell;
    }

    public static Cell getCell(Sheet sheet, int row, int col)
    {
        Row rowLine = getRow(sheet, row);
        return getCell(rowLine, col);
    }

    public static String getCellValue(Sheet sheet, int row, int col)
    {
        String value = "";
        if (sheet == null || row < 0 || col < 0)
            return "";

        Row rowData = sheet.getRow(row);
        if (rowData == null)
            return "";
        Cell cell = rowData.getCell(col);
        if (cell == null)
            return "";
        switch (cell.getCellType())
        {
            case Cell.CELL_TYPE_NUMERIC:
                value = String.valueOf((int) cell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                value = cell.getStringCellValue();
                break;

            default:
                value = cell.toString();
        }

        return value;
    }

    public static String getCellValue(Sheet sheet, int row, int col, boolean needDouble)
    {
        String value = "";
        if (sheet == null || row < 0 || col < 0)
            return "";

        Row rowData = sheet.getRow(row);
        if (rowData == null)
            return "";
        Cell cell = rowData.getCell(col);
        if (cell == null)
            return "";
        switch (cell.getCellType())
        {
            case Cell.CELL_TYPE_NUMERIC:
                value = String.valueOf(cell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                value = cell.getStringCellValue();
                break;

            default:
                value = cell.toString();
        }

        return value;
    }

    public static void createValidatorList(Sheet sheet, String[] values, int startRow,
            int endRow, int columnNumber)
    {
        if (sheet == null || values == null || values.length == 0)
            return;

        try
        {
            endRow = endRow < 0 ? startRow + values.length : endRow;

            DataValidationHelper dvHelper = sheet.getDataValidationHelper();
            DataValidationConstraint dvConstraint = dvHelper.createExplicitListConstraint(values);
            CellRangeAddressList addressList = new CellRangeAddressList(startRow, endRow,
                    columnNumber, columnNumber);
            DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
            validation.setSuppressDropDownArrow(true);
            validation.setShowErrorBox(true);
            sheet.addValidationData(validation);
        }
        catch (Exception e)
        {
            logger.error("Error when create hidden area for category failures.", e);
        }
    }

    public static void createValidatorList(Sheet sheet, List<String> values, int startRow,
            int endRow, int columnNumber)
    {
        if (sheet == null || values == null || values.size() == 0)
            return;

        String[] array = new String[values.size()];
        values.toArray(array);

        createValidatorList(sheet, array, startRow, endRow, columnNumber);
    }

    public static void createValidatorList(Workbook workbook, String validatorName, List<String>
            values, int startRow, int hiddenDataColumn)
    {
        if (workbook == null || values == null || values.size() == 0)
            return;

        String[] array = new String[values.size()];
        values.toArray(array);

        createValidatorList(workbook, validatorName, array, startRow, hiddenDataColumn);
    }

    public static void createValidatorList(Workbook workbook, String validatorName, String[]
            values, int startRow, int hiddenDataColumn)
    {
        if (workbook == null || values == null || values.length == 0)
            return;

        try
        {
            Sheet sheet = workbook.getSheetAt(0);
            Row row;
            Cell cell;
            int len = values.length;
            for (int i = 0; i < len; i++)
            {
                row = getRow(sheet, startRow + i);
                cell = getCell(row, hiddenDataColumn);
                cell.setCellValue(values[i]);
            }
            String hiddenColumn = "$" + indexToColumn(hiddenDataColumn) + "$";

            String formula = sheet.getSheetName() + "!" + hiddenColumn + (startRow + 1) + ": " +
                    hiddenColumn
                    + (startRow + len);
            Name name = workbook.createName();
            name.setRefersToFormula(formula);
            name.setNameName(validatorName);

            // Hide "AA" column
            sheet.setColumnHidden(hiddenDataColumn, true);
        }
        catch (Exception e)
        {
            logger.error("Error when create hidden area for category failures.", e);
        }
    }

    public static String indexToColumn(int index)
    {
        String column = "";
        do
        {
            if (column.length() > 0)
            {
                index--;
            }
            column = ((char) (index % 26 + (int) 'A')) + column;
            index = (int) ((index - index % 26) / 26);
        }
        while (index > 0);

        return column;
    }

    public static void addValidation(Sheet sheet, String validatorName, int startRow, int lastRow,
                                     int startColumn, int lastColumn)
    {
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        DataValidationConstraint dvConstraint = dvHelper
                .createFormulaListConstraint(validatorName);
        CellRangeAddressList addressList = new CellRangeAddressList(startRow, lastRow, startColumn,
                lastColumn);
        DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }
}
