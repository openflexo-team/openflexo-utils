<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
                xmlns:o="urn:schemas-microsoft-com:office:office"
                xmlns:v="urn:schemas-microsoft-com:vml"
                xmlns:WX="http://schemas.microsoft.com/office/word/2003/auxHint"
                xmlns:aml="http://schemas.microsoft.com/aml/2001/core"
                xmlns:w10="urn:schemas-microsoft-com:office:word"
                xmlns:msxsl="urn:schemas-microsoft-com:xslt"
                xmlns:ext="http://www.xmllab.net/wordml2html/ext"
                xmlns:java="http://xml.apache.org/xalan/java"
                version="1.0"
                exclude-result-prefixes="java msxsl ext o v WX aml w10">

  <!-- 
  *  Copyright 2007, Plutext Pty Ltd.
  *
  *  This file is part of plutext-client-word2007.

  plutext-client-word2007 is free software: you can redistribute it and/or
  modify it under the terms of version 3 of the GNU General Public License
  as published by the Free Software Foundation.

  plutext-client-word2007 is distributed in the hope that it will be
  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with plutext-client-word2007.  If not, see
  <http://www.gnu.org/licenses/>.

  -->

  <xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />


  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!--

       <w:body>
               <w:p w:rsidR="00265854" w:rsidRDefault="00320AE9">
                       <w:r>
                               <w:t>We need to be able to handle things which a user has deleted,
like</w:t>
                       </w:r>
                       <w:del w:id="0" w:author="Jason Harrop" w:date="2008-05-26T14:16:00Z">
                               <w:r w:rsidDel="00320AE9">
                                       <w:delText xml:space="preserve"> this</w:delText>
                               </w:r>
                       </w:del>
                       <w:r>
                               <w:t>.</w:t>
                       </w:r>
               </w:p>
               <w:p w:rsidR="00320AE9" w:rsidRDefault="00320AE9">
                       <w:r>
                               <w:t>And stuff which has subsequently been inserted, like this</w:t>
                       </w:r>
                       <w:ins w:id="1" w:author="Jason Harrop" w:date="2008-05-26T14:16:00Z">
                               <w:r>
                                       <w:t xml:space="preserve"> stuff here</w:t>
                               </w:r>
                       </w:ins>
                       <w:r>
                               <w:t>.</w:t>
                       </w:r>
               </w:p>
               <w:sectPr w:rsidR="00320AE9" w:rsidSect="00265854">
                       <w:pgSz w:w="12240" w:h="15840" />
                       <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440"
w:header="708" w:footer="708" w:gutter="0" />
                       <w:cols w:space="708" />
                       <w:docGrid w:linePitch="360" />
               </w:sectPr>
       </w:body>


-->

  <xsl:template match="w:ins" />

  <xsl:template match="w:del" >
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="@w:rsidDel" />

  <xsl:template match="w:delText" >
    <w:t>
      <xsl:apply-templates  select="@*|node()"/>
    </w:t>    
  </xsl:template>


</xsl:stylesheet>
