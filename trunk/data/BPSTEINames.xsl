<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:tei="http://www.tei-c.org/ns/1.0"
	xmlns:xl="http://www.w3.org/1999/xlink"
	exclude-result-prefixes="xl"
    version="2.0">

<!-- Output as an HTML file. -->
<xsl:output method="html" indent="yes"/>
<xsl:template match="/">
<html>
	<head>
		<title>BPS Names Summary for corpus
			<xsl:if test="$datesOnly != 0">
				<xsl:text> (Dates only)</xsl:text>
			</xsl:if>
		</title>
		<style>
			body { font-family: Ariel, Helvetica, sans;  font-size:0.8em;}
			.lineNum { font-style:italic; font-weight:normal; font-size:0.85em;}
			.milestone{ font-weight:normal; font-size:0.85em;}
			.date{ font-weight:bold; font-size:0.85em;}
			.label { font-weight:bold; font-size:1.2em;}
			.person { font-weight:bold; }
			.unknown { font-style:italic;; }
			.note { font-weight:normal; }
		</style>
	</head>
	<body>
		<h2>BPS Names Summary for corpus</h2>
		   <xsl:apply-templates select="/tei:teiCorpus/tei:TEI[tei:text[@type='transliteration']]" >
			   <xsl:with-param name="dates_only" select="0" />
		   </xsl:apply-templates>
	</body>
</html>
</xsl:template>

<xsl:template match="tei:TEI">
	<xsl:param name="dates_only" select="0" />
	<xsl:variable name="docid"><xsl:value-of select="./tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title/tei:name[@type='cdlicat:id_text']/text()" /></xsl:variable>
	<xsl:variable name="cdli_link"><xsl:value-of select="./tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title/tei:name[@type='cdlicat:id_text']/@xl:href" /></xsl:variable>
	<xsl:variable name="cdl_link"><xsl:value-of select="'http://cdl.museum.upenn.edu/hbtin/'" /><xsl:value-of select="$docid" /></xsl:variable>
	<xsl:variable name="weblink"><xsl:value-of select="$cdl_link" /></xsl:variable>
	<hr />
	<h4>
		<xsl:element name="a">
			<xsl:attribute name="href"><xsl:value-of select="$weblink" /></xsl:attribute>
			<xsl:text>Document: </xsl:text><xsl:value-of select="$docid" />
		</xsl:element>
	</h4>
	<p><span class="label">Date:</span>
		<xsl:apply-templates select="./tei:text//tei:div[@type='discourse' and @subtype='date']" />
	</p>
	<xsl:if test="$dates_only=0">
		<p><span class="label">Principles:</span><br />
			<xsl:apply-templates select="./tei:text/tei:body" />
		</p>
		<p><span class="label">Witnesses:</span><br />
			<xsl:apply-templates select="./tei:text/tei:back" />
		</p>
	</xsl:if>
</xsl:template>

<xsl:template match="tei:body">
	<xsl:apply-templates select=".//tei:persName|.//tei:lb|.//tei:milestone[@type='surface']" mode="principle"/>
</xsl:template>

<xsl:template match="tei:back">
	<xsl:apply-templates select=".//tei:persName|.//tei:lb|.//tei:milestone[@type='surface']" mode="witness" />
</xsl:template>

<xsl:template match="tei:persName" mode="principle">
	<span class="person">Principle: 
		<xsl:apply-templates select=".//tei:forename[(not(@type) or (@type!='patronymic'))]" mode="forename" />
		<xsl:apply-templates select=".//tei:forename[((@type='patronymic') and not(@subtype))]" mode="father" />
		<xsl:apply-templates select=".//tei:forename[((@type='patronymic') and @subtype)]" mode="ancestor" />
		<xsl:apply-templates select=".//tei:addName[@type='clan']" />
	</span><br />
</xsl:template>

<xsl:template match="tei:persName" mode="witness">
	<span class="person">Witness: 
		<xsl:apply-templates select=".//tei:forename[(not(@type) or (@type!='patronymic'))]" mode="forename" />
		<xsl:apply-templates select=".//tei:forename[((@type='patronymic') and not(@subtype))]" mode="father" />
		<xsl:apply-templates select=".//tei:forename[((@type='patronymic') and @subtype)]" mode="ancestor" />
		<xsl:apply-templates select=".//tei:addName[@type='clan']" />
	</span><br />
</xsl:template>

<xsl:template match="tei:persName" mode="date">
	<span class="person">Reign: 
		<xsl:apply-templates select=".//tei:forename[(not(@type) or (@type!='patronymic'))]" mode="forename" />
		<xsl:apply-templates select=".//tei:forename[((@type='patronymic') and not(@subtype))]" mode="father" />
		<xsl:apply-templates select=".//tei:forename[((@type='patronymic') and @subtype)]" mode="ancestor" />
		<xsl:apply-templates select=".//tei:addName[@type='clan']" />
	</span><br />
</xsl:template>

<xsl:template name="outputName">
	<xsl:param name="nameText" />
	<xsl:variable name="trimmedName"><xsl:value-of select="replace($nameText,'\[[0-9]*\]','')" /></xsl:variable>
	<xsl:variable name="mappedName"><xsl:value-of select="replace($trimmedName,'NENNI','NENNI(Unknown)')" /></xsl:variable>
	<xsl:value-of select="$mappedName" />	
</xsl:template>

<xsl:template match="tei:forename" mode="forename">
	<span><xsl:call-template name="outputName"><xsl:with-param name="nameText" select="@n" /></xsl:call-template></span>
</xsl:template>

<xsl:template match="tei:forename" mode="father">
	<span class="note"> (father:<span class="person"><xsl:call-template name="outputName"><xsl:with-param name="nameText" select="@n" /></xsl:call-template></span>)</span>
</xsl:template>

<xsl:template match="tei:forename" mode="ancestor">
	<span class="note"> (<xsl:value-of select="@subtype" />:<span class="person"><xsl:call-template name="outputName"><xsl:with-param name="nameText" select="@n" /></xsl:call-template></span>)</span>
</xsl:template>

<xsl:template match="tei:addName"  mode="#all">
	<span class="note"> (clan:<span class="person"><xsl:value-of select="@n" /></span>)</span>
</xsl:template>

<xsl:template match="tei:lb" mode="#all">
	<span class="lineNum">(Line: <xsl:value-of select="@n" />)<br /></span>
</xsl:template>

<xsl:template match="tei:milestone" mode="#all">
	<span class="milestone">(<xsl:value-of select="@n" />)<br /></span>
</xsl:template>

<xsl:template match="tei:div[@type='discourse' and @subtype='date']">
	<xsl:apply-templates select=".//tei:c[@type='num'][1]/preceding-sibling::tei:c[@type='sign' and @subtype='logo']"/>
	<xsl:apply-templates select=".//tei:c[@type='num']"/>
	<xsl:apply-templates select=".//tei:persName[1]" mode="date"/>
	<!-- If persName not there, we may find undemarcated names -->
	<xsl:apply-templates select=".//tei:c[@type='num'][last()]/following-sibling::tei:c[@type='determinative' and @subtype='semantic']/following-sibling::text()[1]" mode="reign"/>
</xsl:template>

<xsl:template match="tei:c[@type='sign' and @subtype='logo']">
	<span class="month"><xsl:value-of select="." /><xsl:text> </xsl:text></span>
</xsl:template>

<xsl:template match="tei:c[@type='num']">
	<span class="numeric"><xsl:value-of select="." /><xsl:text> </xsl:text></span>
</xsl:template>

<xsl:template match="text()" mode="reign">
	<span class="person">Reign: <span><xsl:value-of select="." /></span></span>
</xsl:template>

</xsl:stylesheet>


