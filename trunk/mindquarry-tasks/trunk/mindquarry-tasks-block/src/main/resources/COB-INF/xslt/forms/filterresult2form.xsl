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
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="taskID" select="''" />
	<xsl:param name="teamspaceID" select="''" />

	<xsl:template match="@*|node()"/>

	<xsl:template match="task">
		<item>
			<resultLink>
				../<xsl:value-of select="$taskID" />
			</resultLink>
			<xsl:apply-templates />
		</item>
	</xsl:template>

	<xsl:template match="title">
		<resultTitle>
			<xsl:value-of select="normalize-space(.)" />
		</resultTitle>
	</xsl:template>

	<xsl:template match="status">
		<resultStatus>
			<xsl:value-of select="normalize-space(.)" />
		</resultStatus>
	</xsl:template>

	<xsl:template match="summary">
		<resultSummary>
			<xsl:value-of select="normalize-space(.)" />
		</resultSummary>
	</xsl:template>
</xsl:stylesheet>
