<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:egonp="http://schemas.gov.sk/form/36126624.PovolenieZdravotnictvo.sk/1.6">
    <!--<xsl:variable name="breakLine"><![CDATA[<br/>]]></xsl:variable>-->
    <xsl:variable name="output-namespace">http://www.w3.org/1999/xhtml</xsl:variable>
    <xsl:output method="html" doctype-system="http://www.w3.org/TR/html4/loose.dtd" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" indent="no" omit-xml-declaration="yes"/>
    <xsl:template match="/egonp:OutputDocument">
        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;</xsl:text>
        <html xsl:version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" xml:lang="sk">
            <head>
            </head>
            <body>
                <xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyzľščťžýáíéú'" />
                <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZĽŠČŤŽÝÁÍÉÚ'" />

                <div class="bodyRozhodnutie">
                    <xsl:for-each select="egonp:Rozhodnutie/egonp:UvodVyroku">
                        <div class="divBody">
                            <xsl:apply-templates/>
                        </div>
                    </xsl:for-each>
                </div>
            </body>
        </html>
    </xsl:template>
    <xsl:template name="DeliverTo">
        <xsl:text disable-output-escaping="yes">&lt;br/&gt;</xsl:text>
        <xsl:choose>
            <xsl:when test="egonp:CorporateBody/egonp:CorporateBodyFullName!=''">
                <xsl:value-of select="egonp:CorporateBody/egonp:CorporateBodyFullName"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="egonp:PhysicalPerson/egonp:PersonName/egonp:FormattedName"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>, </xsl:text>
        <xsl:for-each select="egonp:PhysicalAddress">
            <xsl:call-template name="singleLineAddress"/>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="udzsAddress">
        <xsl:if test="egonp:Unit!=''">
            <xsl:value-of select="egonp:Unit"/>
            <xsl:text disable-output-escaping="yes">&lt;br/&gt;</xsl:text>
        </xsl:if>
        <xsl:call-template name="firstLineAddress"/>
        <xsl:text disable-output-escaping="yes">&lt;br/&gt;</xsl:text>
        <xsl:call-template name="secondLineAddress"/>
        <xsl:text disable-output-escaping="yes">&lt;br/&gt;</xsl:text>
        <xsl:call-template name="thirdLineAddress"/>
    </xsl:template>

    <xsl:template name="singleLineAddress">
        <xsl:call-template name="firstLineAddress"/>
        <xsl:text>, </xsl:text>
        <xsl:call-template name="secondLineAddress"/>
    </xsl:template>
    <xsl:template name="firstLineAddress">
        <xsl:choose>
            <xsl:when test="egonp:StreetName!=''">
                <xsl:value-of select="egonp:StreetName"/>
                <xsl:text>&#160;</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="egonp:Municipality/egonp:Codelist/*!=''">
                        <xsl:value-of select="egonp:Municipality/egonp:Codelist/egonp:CodelistItem/egonp:ItemName"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="egonp:Municipality/egonp:NonCodelistData"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>&#160;</xsl:text>
        <xsl:value-of select="egonp:PropertyRegistrationNumber"/>
        <xsl:if test="egonp:BuildingNumber!=''">
            <xsl:text>/</xsl:text>
            <xsl:value-of select="egonp:BuildingNumber"/>
        </xsl:if>
    </xsl:template>
    <xsl:template name="secondLineAddress">
        <xsl:value-of select="egonp:DeliveryAddress/egonp:PostalCode"/>
        <xsl:text>&#160;</xsl:text>
        <xsl:choose>
            <xsl:when test="egonp:Municipality/egonp:Codelist/*!=''">
                <xsl:value-of select="egonp:Municipality/egonp:Codelist/egonp:CodelistItem/egonp:ItemName"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="egonp:Municipality/egonp:NonCodelistData"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="thirdLineAddress">
        <xsl:value-of select="egonp:Country/egonp:Codelist/egonp:CodelistItem/egonp:ItemName"/>
    </xsl:template>
    <!-- Formatted text thingy START -->
    <xsl:template match="egonp:h1|egonp:h2|egonp:h3|egonp:p|egonp:i|egonp:big|egonp:small|egonp:b|egonp:u|egonp:s|egonp:ul|egonp:ol|egonp:li|egonp:br|egonp:table|egonp:thead|egonp:th|egonp:td|egonp:tr|egonp:caption|egonp:a">
        <xsl:element name="{name()}" namespace="http://www.w3.org/1999/xhtml">
            <xsl:if test="@align != ''">
                <xsl:attribute name="align"><xsl:value-of select="@align"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@type != ''">
                <xsl:attribute name="type"><xsl:value-of select="@type"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@width != ''">
                <xsl:attribute name="width"><xsl:value-of select="@width"/>pt</xsl:attribute>
            </xsl:if>
            <xsl:if test="@colspan != ''">
                <xsl:attribute name="colspan"><xsl:value-of select="@colspan"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@rowspan != ''">
                <xsl:attribute name="rowspan"><xsl:value-of select="@rowspan"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@border != ''">
                <xsl:attribute name="border"><xsl:value-of select="@border"/></xsl:attribute>
                <xsl:if test="name()='table' or name()='td' or name()='th'">
                    <xsl:attribute name="class">table-border</xsl:attribute>
                </xsl:if>
            </xsl:if>
            <xsl:if test="@href != ''">
                <xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="egonp:editable">
        <span class="editable">
            <xsl:apply-templates/>
        </span>
    </xsl:template>
    <!--<xsl:template match="*" mode="copy">
        <xsl:element name="{name()}" namespace="http://www.w3.org/1999/xhtml">
            <xsl:apply-templates select="@*|node()" mode="copy"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="@*|text()|comment()" mode="copy">
        <xsl:copy/>
    </xsl:template>-->
    <!-- Formatted text thingy END -->
    <xsl:template name="formatDate">
        <xsl:choose>
            <xsl:when test="string-length(.) >= 10">
                <xsl:value-of select="number(substring(., 9, 2))"/>
                <xsl:text>.</xsl:text>
                <xsl:value-of select="number(substring(., 6, 2))"/>
                <xsl:text>.</xsl:text>
                <xsl:value-of select="substring(., 1, 4)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>{</xsl:text>
                <xsl:value-of select="."/>
                <xsl:text>}</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="formatRCSK">
        <xsl:value-of select="substring(., 1, 6)"/>
        <xsl:text>/</xsl:text>
        <xsl:value-of select="substring(., 7)"/>
    </xsl:template>

    <xsl:template name="formatDateLongSK">
        <xsl:choose>
            <xsl:when test="string-length(.) >= 10">
                <xsl:value-of select="number(substring(., 9, 2))"/>
                <xsl:text>. </xsl:text>

                <xsl:choose>
                    <xsl:when test="number(substring(., 6, 2)) = 1">
                        <xsl:text>januára</xsl:text>
                    </xsl:when>
                    <xsl:when test="number(substring(., 6, 2)) = 2">
                        <xsl:text>februára</xsl:text>
                    </xsl:when>
                    <xsl:when test="number(substring(., 6, 2)) = 3">
                        <xsl:text>marca</xsl:text>
                    </xsl:when>
                    <xsl:when test="number(substring(., 6, 2)) = 4">
                        <xsl:text>apríla</xsl:text>
                    </xsl:when>
                    <xsl:when test="number(substring(., 6, 2)) = 5">
                        <xsl:text>mája</xsl:text>
                    </xsl:when>
                    <xsl:when test="number(substring(., 6, 2)) = 6">
                        <xsl:text>júna</xsl:text>
                    </xsl:when>
                    <xsl:when test="number(substring(., 6, 2)) = 7">
                        <xsl:text>júla</xsl:text>
                    </xsl:when>
                    <xsl:when test="number(substring(., 6, 2)) = 8">
                        <xsl:text>augusta</xsl:text>
                    </xsl:when>
                    <xsl:when test="number(substring(., 6, 2)) = 9">
                        <xsl:text>septembra</xsl:text>
                    </xsl:when>
                    <xsl:when test="number(substring(., 6, 2)) = 10">
                        <xsl:text>októbra</xsl:text>
                    </xsl:when>
                    <xsl:when test="number(substring(., 6, 2)) = 11">
                        <xsl:text>novembra</xsl:text>
                    </xsl:when>
                    <xsl:when test="number(substring(., 6, 2)) = 12">
                        <xsl:text>decembra</xsl:text>
                    </xsl:when>
                </xsl:choose>
                <xsl:text>&#160;</xsl:text>
                <xsl:value-of select="substring(., 1, 4)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>{</xsl:text>
                <xsl:value-of select="."/>
                <xsl:text>}</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="makeUpper">
        <xsl:param name="text"/>
        <xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyzľščťžýáíéúóäôüöňď'"/>
        <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZĽŠČŤŽÝÁÍÉÚÓÄÔÜÖŇĎ'"/>
        <xsl:value-of select="translate($text, $smallcase, $uppercase)" />
    </xsl:template>
</xsl:stylesheet>
