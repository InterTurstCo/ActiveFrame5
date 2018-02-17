<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/">
        <html>
            <head>
                <title>Информация о персонах</title>
                <link type="text/css" rel="stylesheet" media="all" href="remote/report/resource/test-xml-to-html/test-report.css"/>                
            </head>
            <body>
                <h1>Информация о персонах</h1>
                <table border="1">
                    <tr class="table-header">
                        <th>Id</th>
                        <th>Login</th>
                        <th>Имя</th>
                        <th>Фамилия</th>
                        <th>Email</th>
                    </tr>
                    <xsl:for-each select="persons/person">
                        <tr class="table-row">
                            <td>
                                <xsl:value-of select="@id" />
                            </td>
                            <td>
                                <xsl:value-of select="@login" />
                            </td>
                            <td>
                                <xsl:value-of select="@firstname" />
                            </td>
                            <td>
                                <xsl:value-of select="@lastname" />
                            </td>
                            <td>
                                <xsl:value-of select="@email" />
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>