/**
 * Copyright 2016 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * */
package com.globalsight.everest.servlet.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import jodd.util.HtmlDecoder;
import jodd.util.HtmlEncoder;
import jodd.util.StringUtil;
import jodd.util.URLCoder;
import jodd.util.URLDecoder;

import org.apache.log4j.Logger;

/**
 * @author VincentYan
 *
 */
public class ServletUtil extends jodd.servlet.ServletUtil
{
    public static Logger logger = Logger.getLogger(ServletUtil.class);
    private static List<Pattern> patterns = null;

    /**
     * Get parameter value from request with specified name
     * 
     * @param req
     *            Request
     * @param name
     *            Parameter name
     * @return String Parameter value
     */
    public static String getValue(HttpServletRequest req, String name)
    {
        return getValue(req, name, "");
    }

    /**
     * Get parameter value from request with specified name, if value is null or
     * empty, return default value
     * 
     * @param req
     *            Request
     * @param name
     *            Parameter name
     * @param defaultValue
     *            Default value if value is null or empty
     * @return String Parameter value
     */
    public static String getValue(HttpServletRequest req, String name, String defaultValue)
    {
        String result = defaultValue;

        if (req == null || StringUtil.isBlank(name))
            return defaultValue;

        name = name.trim();
        result = (String) value(req, name);
        result = stripXss(result);
        if (StringUtil.isBlank(result) || "null".equalsIgnoreCase(result))
            return defaultValue;

        return result;
    }

    /**
     * Get parameter value with specified name, if it is null, empty or not
     * integer value, return default value
     * 
     * @param req
     *            Request
     * @param name
     *            Parameter name
     * @param defaultValue
     *            Default value
     * @return int Parameter value
     */
    public static int getIntValue(HttpServletRequest req, String name, int defaultValue)
    {
        int result = defaultValue;
        String str = getValue(req, name, "-1");
        try
        {
            result = Integer.parseInt(str);
        }
        catch (Exception e)
        {
            logger.error("Cannot parse string " + str + " to integer.", e);
        }

        return result;
    }

    public static String[] getValues(HttpServletRequest req, String name)
    {
        if (StringUtil.isBlank(name))
            return null;
        String[] values = (String[]) value(req, name);
        if (values == null || values.length == 0)
            return null;
        String[] result = new String[values.length];
        for (int i = 0, len = result.length; i < len; i++)
        {
            result[i] = stripXss(values[i]);
        }
        return result;
    }

    /**
     * Check if the attributes or parameters in request have invaild characters
     * 
     * @param request
     *            HttpServletRequest
     * @return true -- Not including invaild characters false -- Including
     *         invaild characters
     */
    public static boolean checkAllValues(HttpServletRequest request)
    {
        String name, value;
        Enumeration enumeration = request.getAttributeNames();
        while (enumeration.hasMoreElements())
        {
            name = (String) enumeration.nextElement();
            value = (String) request.getAttribute(name);
            if (containXSS(value))
                return false;
        }
        enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements())
        {
            name = (String) enumeration.nextElement();
            value = (String) request.getParameter(name);
            if (containXSS(value))
                return false;
        }

        return true;
    }

    /**
     * Check if the attributes or parameters in URL have invaild characters
     * Invaild characters contains,
     * 
     * @param url
     *            String Query string in URL
     * @return true -- Not including invaild characters false -- Including
     *         invaild characters
     */
    public static boolean checkUrl(String url)
    {
        if (StringUtil.isBlank(url))
            return true;
        if (containXSS(url) || containXSS(URLDecoder.decode(url)))
            return false;

        return true;
    }

    private static List<Object[]> getXssPatternList()
    {
        List<Object[]> ret = new ArrayList<Object[]>();
        ret.add(new Object[]
        { "<(no)?script[^>]*>.*?</(no)?script>", Pattern.CASE_INSENSITIVE });
        ret.add(new Object[]
        { "eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL });
        ret.add(new Object[]
        { "expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL });
        ret.add(new Object[]
        { "(javascript:|vbscript:|view-source:).*", Pattern.CASE_INSENSITIVE });
        // ret.add(new Object[]
        // { "<(\"[^\"]*\"|\'[^\']*\'|[^\'\">]).*>",
        // Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL });
        ret.add(new Object[]
        {
                "(window\\.location|window\\.|\\.location|document\\.cookie|document\\.|alert\\(.*?\\)|window\\.open\\().*",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL });
        ret.add(new Object[]
        {
                "<+\\s*.*\\s*(oncontrolselect|oncopy|oncut|ondataavailable|ondatasetchanged|ondatasetcomplete|ondblclick|ondeactivate|ondrag|ondragend|ondragenter|ondragleave|ondragover|ondragstart|ondrop|onerror=|onerroupdate|onfilterchange|onfinish|onfocus|onfocusin|onfocusout|onhelp|onkeydown|onkeypress|onkeyup|onlayoutcomplete|onload|onlosecapture|onmousedown|onmouseenter|onmouseleave|onmousemove|onmousout|onmouseover|onmouseup|onmousewheel|onmove|onmoveend|onmovestart|onabort|onactivate|onafterprint|onafterupdate|onbefore|onbeforeactivate|onbeforecopy|onbeforecut|onbeforedeactivate|onbeforeeditocus|onbeforepaste|onbeforeprint|onbeforeunload|onbeforeupdate|onblur|onbounce|oncellchange|onchange|onclick|oncontextmenu|onpaste|onpropertychange|onreadystatechange|onreset|onresize|onresizend|onresizestart|onrowenter|onrowexit|onrowsdelete|onrowsinserted|onscroll|onselect|onselectionchange|onselectstart|onstart|onstop|onsubmit|onunload)+\\s*=+",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL });
        return ret;
    }

    private static List<Pattern> getPatterns()
    {
        if (patterns == null)
        {
            List<Pattern> list = new ArrayList<Pattern>();
            String regex = null;
            Integer flag = null;
            int arrLength = 0;
            for (Object[] arr : getXssPatternList())
            {
                arrLength = arr.length;
                for (int i = 0; i < arrLength; i++)
                {
                    regex = (String) arr[0];
                    flag = (Integer) arr[1];
                    list.add(Pattern.compile(regex, flag));
                }
            }
            patterns = list;
        }
        return patterns;
    }

    /**
     * Replace invaild characters got from parameter or attribute
     * 
     * @param value
     * @return
     */
    public static String stripXss(String value)
    {
        return stripXss(value, false);
    }

    public static String stripXss(String value, boolean htmlEncoding)
    {
        if (StringUtil.isNotBlank(value))
        {
            Matcher matcher = null;
            for (Pattern pattern : getPatterns())
            {
                matcher = pattern.matcher(value);
                if (matcher.find())
                {
                    value = matcher.replaceAll("");
                }
            }

            if (htmlEncoding)
                value = HtmlEncoder.text(value);
        }

        return value;

    }

    /**
     * Check if string contains invaild characters
     * 
     * @param str
     *            String string
     * @return true -- Including invaild characters false -- Not including
     *         invaild characters
     */
    public static boolean containXSS(String str)
    {
        if (StringUtil.isBlank(str))
            return false;
        Matcher matcher = null;
        for (Pattern pattern : getPatterns())
        {
            matcher = pattern.matcher(str);
            if (matcher.find())
            {
                return true;
            }
        }
        return false;
    }

    public static String encodeHtml(String str)
    {
        return HtmlEncoder.text(str);
    }

    public static String decodeHtml(String str)
    {
        return HtmlDecoder.decode(str);
    }

    public static String encodeUrl(String str)
    {
        return URLCoder.encodeHttpUrl(str);
    }

    public static String decodeUrl(String str)
    {
        return URLDecoder.decode(str);
    }
}