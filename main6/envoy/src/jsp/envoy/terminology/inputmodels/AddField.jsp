<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
%>
<html>
<head>
<title><%=bundle.getString("lb_term_input_model_add_field_constraints") %></title>
<!-- <META HTTP-EQUIV="EXPIRES" CONTENT="0"> -->
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/management/objects_js.jsp"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script LANGUAGE="JavaScript" src="/globalsight/envoy/terminology/management/FireFox.js"></script>
<style>
/* TO BE REMOVED AND REPLACED BY SetStylesheet.js */
BODY,
LABEL,
#idDescription {
    font-family: Verdana, Helvetica, sans-serif;
    font-size: 10pt;
}
#idDescription {
    height: 5em;
    width: 100%;
    overflow: auto;
    padding: 1px 3px 1px 1px;
    background-color: lightblue;
}
</style>
<SCRIPT language="Javascript">
var o= window.opener;
var g_args = o.addFieldParams;

var g_currentFields;
var g_currentAttributes;

function setup(level, definedFields)
{
  g_currentFields = new Array().concat(definedFields);

  if (g_currentFields.length == 0)
  {
    if (level == 'concept')
    {
      g_currentFields = g_currentFields.concat(g_conceptFields);
    }
    else if (level == 'language')
    {
      g_currentFields = g_currentFields.concat(g_languageFields);
    }
    else if (level == 'term')
    {
      g_currentFields = g_currentFields.concat(g_termFields);
    }
    else if (level == 'field')
    {
      g_currentFields = g_currentFields.concat(g_fieldFields);
    }
    else if (level == 'source')
    {
      g_currentFields = g_currentFields.concat(g_sourceFields);
    }
  }

  // override field and source level fields
  if (level == 'field')
  {
    g_currentFields = g_fieldFields;
  }
  else if (level == 'source')
  {
    g_currentFields = g_sourceFields;
  }

  fillSelect(idType, g_currentFields);
  setValues(0);
}

function clearSelect(select)
{
    var options = select.options;
    for (var i = options.length; i >= 1; --i)
    {
        options.remove(i-1);
    }
}

function fillSelect(select, values)
{
    clearSelect(select);

    var options = select.options;

    for (var i = 0; i < values.length; i++)
    {
        var value = values[i];

        var option = document.createElement('OPTION');
        option.text = value.getDisplayName() + ' ';
        option.value = i;
        options.add(option);
    }

    setDescription(0);
}

function fillAttributes(select, values)
{
    g_currentAttributes = values;

    clearSelect(select);

    var options = select.options;

    for (var i = 0; i < values.length; i++)
    {
        var value = values[i];

        var option = document.createElement('OPTION');
        option.text = Trim(value) + '\u00a0';
        option.value = i;
        options.add(option);
    }
}

function fillAttributesWithDummy(select)
{
    clearSelect(select);

    var options = select.options;
    var option = document.createElement('OPTION');
    option.text = "text field ";
    options.add(option);
}

function setDescription(index)
{
    var field = g_currentFields[index];

    idDescription.innerText = field.getDescription();
}

function setValues(index)
{
  var field = g_currentFields[index];

/*
  if (field.isAttribute())
  {
    fillAttributes(idAttrValue, field.values.split(","));

    if (g_currentFields.length == 1)
    {
      idAttrValue.focus();
    }

    idAttrValue.disabled = false;
    idAttrValueLabel.disabled = false;
    idValue.disabled = true;
    idValueLabel.disabled = true;
  }
  else
  {
    fillAttributesWithDummy(idAttrValue);

    if (g_currentFields.length == 1)
    {
      idValue.focus();
    }

    idAttrValue.disabled = true;
    idAttrValueLabel.disabled = true;
    idValue.disabled = false;
    idValueLabel.disabled = false;
  }
*/
}

function changeType()
{
  var index = idType.options[idType.selectedIndex].value;

  setValues(index);
  setDescription(index);
}

function doClose(ok)
{
  if (ok == true)
  {
    var index = idType.options[idType.selectedIndex].value;
    var field = g_currentFields[index];
    var type = field.type;
    var value;

/*
    if (field.isAttribute())
    {
      index = idAttrValue.options[idAttrValue.selectedIndex].value;
      value = g_currentAttributes[index];
    }
    else
    {
      value = Trim(idValue.value);
    }
*/
    if (idRequired.checked)
    {
      value = "required";
    }
    else
    {
      value = "optional";
    }

    if (idMultiple.checked)
    {
      value += ", multiple";
    }

    g_args.setType(type);
    g_args.setValue(value);

    if (o.isToCurrent)
    {
    	o.AddFieldToCurrentDialog(g_args);
    }
    else
    {
    	o.AddFieldAfterCurrentDialog(g_args);
    }
  }

  window.close();
}

function doKeypress()
{
  var key = event.keyCode;

  if (key == 27) // Escape
  {
    doClose(false);
  }
  else if (key == 13) // Return
  {
    doClose(true);
  }
}

function doLoad()
{
  var level = g_args.getLevel();
  idLevel.innerText = Trim(level);

  var definedFields = g_args.getDefinedFields();
  setup(level, definedFields);
}
</script>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</head>

<body onload="doLoad()" onkeypress="doKeypress()">

<DIV ID="contentLayer"
  STYLE="POSITION: ABSOLUTE; TOP: 10px; LEFT: 10px;">
<SPAN ID="idHeading" CLASS="mainHeading">
<%=bundle.getString("lb_term_input_model_field_constraints_at") %> <span id="idLevel"></span> &nbsp;<%=bundle.getString("lb_term_input_model_field_level") %></SPAN>
<BR><BR>
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS="standardText">
  <COL WIDTH="25%">
  <COL WIDTH="75%">
  <TBODY>
  <TR>
    <TD>
      <LABEL FOR="idType"><%=bundle.getString("lb_the_field") %></LABEL>
    </TD>
    <TD>
      <select id="idType" TABINDEX="1" onchange="changeType()"></select>
    </TD>
  </TR>
<!--
  <TR>
    <TD valign="top">
      <LABEL id="idAttrValueLabel" FOR="idAttrValue">Attribute Value:</LABEL>
    </TD>
    <TD valign="top">
      <SELECT id="idAttrValue" TABINDEX="1"></SELECT>
    </TD>
  </TR>
  <TR>
    <TD valign="top">
      <LABEL id="idValueLabel" FOR="idValue">Text Value:</LABEL>
    </TD>
    <TD valign="top">
      <TEXTAREA id="idValue" TABINDEX="1" rows="5" cols="27"></TEXTAREA>
    </TD>
  </TR>
  <TR><TD>&nbsp;</TD><TD>&nbsp;</TD></TR>
-->
  <TR>
    <TD valign="top"><LABEL><%=bundle.getString("lb_term_input_model_explanation") %>:</LABEL></TD>
    <TD valign="top">
      <P id="idDescription" style="width:200"></P>
    </TD>
  </TR>
  <TR>
    <TD valign="top" rowspan=2><%=bundle.getString("lb_term_input_model_is") %>:</TD>
    <TD>
      <INPUT TYPE="radio" name="m" id="idRequired" TABINDEX="2" checked>
      <LABEL FOR="idRequired"><%=bundle.getString("lb_editor_required") %></LABEL>
      <INPUT TYPE="radio" name="m" id="idOptional" TABINDEX="3">
      <LABEL FOR="idOptional"><%=bundle.getString("lb_editor_optional") %></LABEL>
    </TD>
  </TR>
  <TR>
    <TD colspan=2>
      <INPUT TYPE="checkbox" id="idMultiple" TABINDEX="4">
      <LABEL FOR="idMultiple"><%=bundle.getString("lb_term_input_model_occur_multi_times") %></LABEL>
    </TD>
  </TR>
  </TBODY>
</TABLE>

<BR>

<DIV ALIGN="CENTER">
<INPUT id="idOk" TYPE=BUTTON VALUE=" <%=bundle.getString("lb_ok") %> " TABINDEX="5" onclick="doClose(true);">
<INPUT id="idCancel" TYPE=BUTTON VALUE="<%=bundle.getString("lb_cancel") %>" TABINDEX="6" onclick="doClose(false);">
</DIV>

</DIV>
</body>
</html>
