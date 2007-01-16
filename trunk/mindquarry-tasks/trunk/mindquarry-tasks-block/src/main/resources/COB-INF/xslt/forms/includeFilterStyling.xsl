<?xml version="1.0"?>
<!--
	Copyright (C) 2006-2007 Mindquarry GmbH, All Rights Reserved
	
	The contents of this file are subject to the Mozilla Public License
	Version 1.1 (the "License"); you may not use this file except in
	compliance with the License. You may obtain a copy of the License at
	http://www.mozilla.org/MPL/
	
	Software distributed under the License is distributed on an "AS IS"
	basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
	License for the specific language governing rights and limitations
	under the License.
-->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:bu="http://apache.org/cocoon/browser-update/1.0"
	xmlns:xhtml="http://www.w3.org/1999/xhtml">

	<xsl:import href="block:/xslt/contextpath.xsl" />
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
	
	<!-- do not remove the prefix for the cocoon AJAX Browser Update namespace -->
	<xsl:template match="bu:*">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="*">
		<xsl:element name="{local-name(.)}">
			<xsl:apply-templates select="@*|node()" />
		</xsl:element>
	</xsl:template>
		
	<xsl:template match="xhtml:head|head">
		<head>
			<xsl:apply-templates />
			
			<link rel="stylesheet"
				href="{$pathToBlock}css/filter.css" type="text/css" />
		</head>
	</xsl:template>
	
	<xsl:template match="img[@class='task_status']">
		<img class="task_status">
			<xsl:attribute name="src">
				<xsl:choose>
					<xsl:when test="../following-sibling::node()/span='new'">
						<xsl:value-of select="$pathToBlock" />images/status/new.png</xsl:when>
					<xsl:when test="../following-sibling::node()/span='running'">
						<xsl:value-of select="$pathToBlock" />images/status/running.png</xsl:when>
					<xsl:when test="../following-sibling::node()/span='paused'">
						<xsl:value-of select="$pathToBlock" />images/status/paused.png</xsl:when>
					<xsl:when test="../following-sibling::node()/span='done'">
						<xsl:value-of select="$pathToBlock" />images/status/done.png</xsl:when>
				</xsl:choose>
			</xsl:attribute>
		</img>
	</xsl:template>
</xsl:stylesheet>
