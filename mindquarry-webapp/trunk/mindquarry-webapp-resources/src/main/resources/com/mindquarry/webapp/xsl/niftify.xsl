<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:us="http://www.mindquarry.com/ns/schema/webapp">
	
	<xsl:template match="xhtml:div[@class='nifty']|div[@class='nifty']">
		<div class="nifty">
			<b class="rtop">
				<b class="r1"><xsl:comment>t</xsl:comment></b>
				<b class="rleft"><xsl:comment>tr</xsl:comment></b>
				<b class="rright"><xsl:comment>tl</xsl:comment></b>
			</b>
			<div class="content">
				<xsl:apply-templates />
			</div>
			<b class="rbottom">
				<b class="r1"><xsl:comment>b</xsl:comment></b>
				<b class="rleft"><xsl:comment>bl</xsl:comment></b>
				<b class="rright"><xsl:comment>br</xsl:comment></b>
			</b>
		</div>
	</xsl:template>

</xsl:stylesheet>