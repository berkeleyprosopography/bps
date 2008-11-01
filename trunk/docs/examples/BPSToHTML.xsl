<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

<!-- Output as an HTML file. -->
<xsl:output method="html" indent="yes"/>
<xsl:template name="root" match="/">
<html>
	<head>
		<title><xsl:value-of select="/teiCorpus/teiHeader/fileDesc/titleStmt/title/text()" /></title>
		<style>
		<![CDATA[
			body { font-family: Ariel, Helvetica, sans; }
			p { background-color:white; font-size:0.8em;}
			span.fn { color:blue; }
			span.fnp { color:purple; }
			span.fna { color:red; }
			span.lineno { font-family:Courier New, Courier, fixed; background-color:white; }
			span.note { color:green; background-color:white;  }
			span.pers { background-color:#e4e4e4; }
		 ]]>
		</style>
	</head>
	<body>
   <xsl:apply-templates select="/teiCorpus/TEI" />
	</body>
</html>
</xsl:template>

<xsl:template match="TEI">
	<hl />
	<xsl:apply-templates select="teiHeader/fileDesc/sourceDesc" />
	<p><span class="note">#atf: lang <xsl:value-of select="text/@xml:lang" /></span>
	<xsl:apply-templates select="text/note" /></p>
	<xsl:apply-templates select="text/div" />
</xsl:template>

<xsl:template match="text/note[@type='atf']">
	<br /><span class="note">#atf: <xsl:value-of select="./text()" /></span>
</xsl:template>

<xsl:template match="text/note[@type='inline']">
	<br /><span class="note"># <xsl:value-of select="./text()" /></span>
</xsl:template>

<xsl:template match="sourceDesc">
	<h2><xsl:apply-templates /></h2>
</xsl:template>

<xsl:template match="sourceDesc/name">
	<xsl:value-of select="./@type" /><xsl:apply-templates />
</xsl:template>

<xsl:template match="div">
	<h3><xsl:value-of select="./@type" /></h3>
	<p><xsl:apply-templates /></p>
</xsl:template>

<xsl:template match="div[@type='column']">
	<p>@Column <xsl:value-of select="./@n" /><xsl:apply-templates /></p>
</xsl:template>

<xsl:template match="lb">
	<xsl:if test="./@n&gt;1"><br /></xsl:if>
	<span class="lineno"><xsl:value-of select="./@n" /><xsl:text> </xsl:text></span>
</xsl:template>

<xsl:template match="note">
	<br /><span class="note"># <xsl:value-of select="./text()" /></span>
</xsl:template>

<xsl:template match="note[@type='question']">
</xsl:template>

<xsl:template match="div[@type='seal']">
	<br /><span class="seal">$ seal = <xsl:apply-templates /></span>
</xsl:template>

<xsl:template match="rs">
	<xsl:value-of select="./@type" /> <xsl:value-of select="./text()" />
</xsl:template>

<xsl:template match="persName">
  [<span class="pers"><xsl:apply-templates /></span>]
</xsl:template>

<xsl:template match="forename">
	<xsl:choose>
		<xsl:when test="./@type='patronym'">
			<span class="fnp"><xsl:value-of select="./text()" /></span>
		</xsl:when>
		<xsl:otherwise>
			<span class="fn"><xsl:value-of select="./text()" /></span>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="addname">
	<span class="fna"><xsl:value-of select="./text()" /></span>
</xsl:template>
	
</xsl:stylesheet>


