<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:collection="http://apache.org/cocoon/collection/1.0"
		xmlns:xlink="http://www.w3.org/1999/xlink"
		exclude-result-prefixes="collection">

    <xsl:param name="basePath" />
    <xsl:param name="teamspace" />

    <xsl:template match="/collection:collection">
    	<tasks xml:base="{$basePath}{$teamspace}/" xlink:href="{$teamspace}">
    		<xsl:apply-templates />
    	</tasks>
    </xsl:template>

    <xsl:template match="collection:resource">
		<task xlink:href="{substring-before(@name, '.xml')}" id="{substring-before(@name, '.xml')}"/>
    </xsl:template>

</xsl:stylesheet>