<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" indent="yes" omit-xml-declaration="yes" />
    <xsl:template match="/failsafe-summary">
        Jakarta Batch TCK completed running <xsl:value-of select="completed"/> tests.
        Number of Tests Passed      = <xsl:value-of select="completed - failures - errors"/>
        Number of Tests with Errors = <xsl:value-of select="errors"/>
        Number of Tests Failed      = <xsl:value-of select="failures"/>
        Number of Tests Skipped     = <xsl:value-of select="skipped"/>
    </xsl:template>
</xsl:stylesheet>

