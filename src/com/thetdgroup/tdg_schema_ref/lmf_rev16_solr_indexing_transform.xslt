<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:tdg="urn:com:thetdgroup:schema:lmf:rev16" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="urn:com:thetdgroup:schema:lmf:rev16 lmf_rev16.xsd" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output method="xml" omit-xml-declaration="yes" encoding="UTF-8" indent="yes"/>

 <xsl:template match="/">
  <add>
   <xsl:apply-templates select="tdg:LexicalResource"/>
  </add>
 </xsl:template>

 <xsl:template match="tdg:LexicalResource">
  <doc>
   <xsl:apply-templates/>
  </doc>
 </xsl:template>

 <xsl:template match="tdg:feat">
 
  <xsl:choose>
   <!-- Will associate the language with it's written form-->
   <xsl:when test="@tdg:att = 'writtenForm'">
    <field>
     <xsl:attribute name="name">
      <xsl:for-each select="../tdg:feat">
       <xsl:if test="@tdg:att='language'">
        <xsl:value-of select="@tdg:val"/>
       </xsl:if>
      </xsl:for-each>
     </xsl:attribute>
     <xsl:value-of select="@tdg:val"/>
    </field>
   </xsl:when>

   <!-- Will associate the language iso with a display name-->
   <xsl:when test="@tdg:att='language'">
    <xsl:if test="@tdg:val='ara'">
     <field name="language">Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='arq'">
     <field name="language">Algerian Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='aao'">
     <field name="language">Algerian Saharan Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='bbz'">
     <field name="language">Babalia Creole Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='abv'">
     <field name="language">Baharna Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='shu'">
     <field name="language">Chadian Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='acy'">
     <field name="language">Cypriot Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='adf'">
     <field name="language">Dhofari Arabi</field>
    </xsl:if>
    <xsl:if test="@tdg:val='avl'">
     <field name="language">Eastern Egyptian Bedawi Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='arz'">
     <field name="language">Egyptian Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='afb'">
     <field name="language">Gulf Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='ayh'">
     <field name="language">Hadrami Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='acw'">
     <field name="language">Hijazi Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='ayl'">
     <field name="language">Libyan Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='acm'">
     <field name="language">Mesopotamian Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='ary'">
     <field name="language">Moroccan Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='ars'">
     <field name="language">Najdi Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='apc'">
     <field name="language">North Levantine Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='ayp'">
     <field name="language">North Mesopotamian Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='acx'">
     <field name="language">Omani Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='aec'">
     <field name="language">Saidi Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='ayn'">
     <field name="language">Sanaani Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='ssh'">
     <field name="language">Shihhi Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='ajp'">
     <field name="language">South Levantine Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='arb'">
     <field name="language">Standard Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='apd'">
     <field name="language">Sudanese Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='pga'">
     <field name="language">Sudanese Creole Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='acq'">
     <field name="language">Ta'izzi-Adeni Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='abh'">
     <field name="language">Tajiki Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='aeb'">
     <field name="language">Tunisian Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='auz'">
     <field name="language">Uzbeki Arabic</field>
    </xsl:if>
    <xsl:if test="@tdg:val='chi'">
     <field name="language">Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='zho'">
     <field name="language">Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='gan'">
     <field name="language">Gan Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='hak'">
     <field name="language">Hakka Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='czh'">
     <field name="language">Huizhou Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='cjy'">
     <field name="language">Jinyu Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='lzh'">
     <field name="language">Literary Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='cmn'">
     <field name="language">Mandarin Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='mnp'">
     <field name="language">Min Bei Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='cdo'">
     <field name="language">Min Dong Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='nan'">
     <field name="language">Min Nan Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='czo'">
     <field name="language">Min Zhong Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='cpx'">
     <field name="language">Pu-Xian Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='wuu'">
     <field name="language">Wu Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='hsn'">
     <field name="language">Xiang Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='yue'">
     <field name="language">Yue Chinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='ace'">
     <field name="language">Achinese</field>
    </xsl:if>
    <xsl:if test="@tdg:val='eng'">
     <field name="language">English</field>
    </xsl:if>
    <xsl:if test="@tdg:val='fre'">
     <field name="language">French</field>
    </xsl:if>
    <xsl:if test="@tdg:val='deu'">
     <field name="language">German</field>
    </xsl:if>
    <xsl:if test="@tdg:val='ind'">
     <field name="language">Indonesian</field>
    </xsl:if>
    <xsl:if test="@tdg:val='per'">
     <field name="language">Persian</field>
    </xsl:if>
    <xsl:if test="@tdg:val='rus'">
     <field name="language">Russian</field>
    </xsl:if>
    <xsl:if test="@tdg:val='sin'">
     <field name="language">Sinhala</field>
    </xsl:if>
    <xsl:if test="@tdg:val='spa'">
     <field name="language">Spanish</field>
    </xsl:if>
    <xsl:if test="@tdg:val='tam'">
     <field name="language">Tamil</field>
    </xsl:if>
    <xsl:if test="@tdg:val='kor'">
     <field name="language">Korean</field>
    </xsl:if>
   </xsl:when>

   <!-- Process all other fields-->
   <xsl:otherwise>
    <field>
     <xsl:attribute name="name">
      <xsl:value-of select="@tdg:att"/>
     </xsl:attribute>
     <xsl:value-of select="@tdg:val"/>
    </field>
  </xsl:otherwise>
  </xsl:choose>
 </xsl:template>
</xsl:stylesheet>
