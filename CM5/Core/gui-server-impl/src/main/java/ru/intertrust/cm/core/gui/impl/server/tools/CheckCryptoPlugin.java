package ru.intertrust.cm.core.gui.impl.server.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import ru.intertrust.cm.core.business.api.crypto.CryptoService;
import ru.intertrust.cm.core.business.api.dto.crypto.SignerInfo;
import ru.intertrust.cm.core.business.api.dto.crypto.VerifyResult;

@WebServlet(name = "CheckCryptoPlugin", urlPatterns = { "/remote/service/check-crypto-plugin" }, asyncSupported = true)
public class CheckCryptoPlugin extends HttpServlet {

    @Autowired
    private CryptoService cryptoService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();

        out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        out.print("<html>");
        out.print("<head><title>");
        out.print("Проверка криптоплагина");
        out.print("</title>");
        out.print("<meta charset='utf-8'>");

        out.print("<script language=\"javascript\" src=\"" + req.getContextPath() + "/js/cadesplugin_api.js\"></script>\r\n");
        out.print("<script language=\"javascript\" src=\"" + req.getContextPath() + "/js/crypto-tool.js\"></script>\r\n");
        out.print("<script>\r\n" +
                "    function onClickSign(){\r\n" +
                "        var install = window.cryptoTool.checkInstall();\r\n" +
                "        if (install){\r\n" +
                "            try{\r\n" +
                "                var certificatesSelect = document.getElementById(\"cer\");\r\n" +
                "                var bodyInput = document.getElementById(\"body\");\r\n" +
                "                var resultDiv = document.getElementById(\"signature\");\r\n" +
                "                var cerIndex = parseInt(certificatesSelect.value);\r\n" +
                "                var signData = toBase64(bodyInput.value);\r\n" +
                "                window.cryptoTool.sign(cerIndex+1, signData, function(signature, error){\r\n" +
                "                    if (signature == null && error != null){\r\n" +
                "                        resultDiv.innerHTML = error.message;\r\n" +
                "                    }else{\r\n" +
                "                        resultDiv.innerHTML = '<pre>' + signature + '</pre>';\r\n" +
                "                        var checkDiv = document.getElementById(\"check\");\r\n" +
                "                        checkDiv.innerHTML = 'Wait check result' + '<img src=\"" + req.getContextPath() + "/images/loading.gif" + "\"/>';\r\n" +
                "                        request(signData, signature);" +
                "                    }\r\n" +
                "                });\r\n" +
                "            } catch (err) {\r\n" +
                "                resultDiv.innerHTML= err.message;\r\n" +
                "            }\r\n" +
                "        }else{\r\n" +
                "            resultDiv.innerHTML = \"Cryptopro plugin not installed\";\r\n" +
                "        }\r\n" +
                "    }\r\n" +
                "    \r\n" +
                "    function toBase64(param){\r\n" +
                "        // Create Base64 Object\r\n" +
                "        var Base64={_keyStr:\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=\",encode:function(e){var t=\"\";var n,r,i,s,o,u,a;var f=0;e=Base64._utf8_encode(e);while(f<e.length){n=e.charCodeAt(f++);r=e.charCodeAt(f++);i=e.charCodeAt(f++);s=n>>2;o=(n&3)<<4|r>>4;u=(r&15)<<2|i>>6;a=i&63;if(isNaN(r)){u=a=64}else if(isNaN(i)){a=64}t=t+this._keyStr.charAt(s)+this._keyStr.charAt(o)+this._keyStr.charAt(u)+this._keyStr.charAt(a)}return t},decode:function(e){var t=\"\";var n,r,i;var s,o,u,a;var f=0;e=e.replace(/[^A-Za-z0-9+/=]/g,\"\");while(f<e.length){s=this._keyStr.indexOf(e.charAt(f++));o=this._keyStr.indexOf(e.charAt(f++));u=this._keyStr.indexOf(e.charAt(f++));a=this._keyStr.indexOf(e.charAt(f++));n=s<<2|o>>4;r=(o&15)<<4|u>>2;i=(u&3)<<6|a;t=t+String.fromCharCode(n);if(u!=64){t=t+String.fromCharCode(r)}if(a!=64){t=t+String.fromCharCode(i)}}t=Base64._utf8_decode(t);return t},_utf8_encode:function(e){e=e.replace(/rn/g,\"n\");var t=\"\";for(var n=0;n<e.length;n++){var r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r)}else if(r>127&&r<2048){t+=String.fromCharCode(r>>6|192);t+=String.fromCharCode(r&63|128)}else{t+=String.fromCharCode(r>>12|224);t+=String.fromCharCode(r>>6&63|128);t+=String.fromCharCode(r&63|128)}}return t},_utf8_decode:function(e){var t=\"\";var n=0;var r=c1=c2=0;while(n<e.length){r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r);n++}else if(r>191&&r<224){c2=e.charCodeAt(n+1);t+=String.fromCharCode((r&31)<<6|c2&63);n+=2}else{c2=e.charCodeAt(n+1);c3=e.charCodeAt(n+2);t+=String.fromCharCode((r&15)<<12|(c2&63)<<6|c3&63);n+=3}}return t}}\r\n"
                +
                "\r\n" +
                "        // Encode the String\r\n" +
                "        return Base64.encode(param);\r\n" +
                "        \r\n" +
                "    }\r\n" +
                "function request(signData, signature) {\r\n" +
                "  const xhr = new XMLHttpRequest();\r\n" +
                "  xhr.timeout = 20000;\r\n" +
                "  var checkDiv = document.getElementById(\"check\");\r\n" +
                "  xhr.onreadystatechange = function(e) {\r\n" +
                "    if (xhr.readyState === 4) {\r\n" +
                "      if (xhr.status === 200) {\r\n" +
                "         // Код обработки успешного завершения запроса\r\n" +
                "         checkDiv.innerHTML = xhr.responseText;\r\n" +
                "      } else {\r\n" +
                "         // Обрабатываем ответ с сообщением об ошибке\r\n" +
                "         checkDiv.innerHTML = \"Request error\" + xhr.status;\r\n" +
                "      }\r\n" +
                "    }\r\n" +
                "  }\r\n" +
                "  xhr.ontimeout = function () {\r\n" +
                "    // Ожидание ответа заняло слишком много времени, тут будет код, который обрабатывает подобную ситуацию\r\n" +
                "    checkDiv.innerHTML = \"Request timeout\";\r\n" +
                "  }\r\n" +
                "  xhr.open('post', '', true)\r\n" +
                "  xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');\r\n" +
                "  xhr.send('data=' + encodeURIComponent(signData) + '&signature=' + encodeURIComponent(signature));\r\n" +
                "}\r\n" +
                "</script>\r\n");
        out.print("</head>");

        out.print("<body>\r\n" +
                "    <div id=\"info\">Check plugin</div>\r\n" +
                "    <table>\r\n" +
                "    \r\n" +
                "    <tr><td><select id=\"cer\"/></td></tr>\r\n" +
                "    <tr><td><input type=\"text\" id=\"body\" value=\"Test string\"/></td></tr>\r\n" +
                "    <tr><td><input type=\"button\" id=\"sign\" value=\"Sign\" onclick=\"onClickSign()\"/></td></tr>\r\n" +
                "    <table>\r\n" +
                "    <hr style=\"width: 100%; color: rgb(151, 7, 11);\">\r\n" +
                "    <div>Signature:</div>\r\n" +
                "    <div id=\"signature\">Result</div>\r\n" +
                "    <hr style=\"width: 100%; color: rgb(151, 7, 11);\">\r\n" +
                "    <div>Check signature:</div>\r\n" +
                "    <div id=\"check\">Result</div>\r\n" +
                "    <script lang=\"javascript\">\r\n" +
                "        function onInitCryptoTool(isInit, error){\r\n" +
                "            var infoDiv = document.getElementById(\"info\");\r\n" +
                "            try{\r\n" +
                "                if (isInit){\r\n" +
                "                    var install = window.cryptoTool.checkInstall();\r\n" +
                "                    infoDiv.innerHTML = \"Check plugin = \" + install;\r\n" +
                "                    if (install){\r\n" +
                "                        var certificatesSelect = document.getElementById(\"cer\");\r\n" +
                "                        window.cryptoTool.getCertificates(function (certificates, error){\r\n" +
                "                            if (certificates == null && error != null){\r\n" +
                "                                infoDiv.innerHTML = error;\r\n" +
                "                            }else{\r\n" +
                "                                for(var certificate in certificates) {\r\n" +
                "                                    var option = document.createElement(\"option\");\r\n" +
                "                                    option.value = certificate;\r\n" +
                "                                    option.text = certificates[certificate];\r\n" +
                "                                    certificatesSelect.add(option);\r\n" +
                "                                }       \r\n" +
                "                            }\r\n" +
                "                        });\r\n" +
                "                    }\r\n" +
                "                }else{\r\n" +
                "                    infoDiv.innerHTML = error;\r\n" +
                "                }\r\n" +
                "            } catch (err) {\r\n" +
                "                infoDiv.innerHTML = err.message;\r\n" +
                "            }\r\n" +
                "        }   \r\n" +
                "\r\n" +
                "        try{\r\n" +
                "            window.cryptoTool.init(\"http://testca.cryptopro.ru/tsp/\", false, \"CAdES-BES\", \"GOST_3411_2012_256\", \""
                + req.getContextPath() + "\", onInitCryptoTool);\r\n" +
                "            //window.cryptoTool.init(\"http://cryptopro.ru/tsp/\", false);    \r\n" +
                "            //window.cryptoTool.init(\"http://testca.cryptopro.ru/tsp/tsp.srf\", false);\r\n" +
                "        } catch (err) {\r\n" +
                "            infoDiv.innerHTML = err;\r\n" +
                "        }\r\n" +
                "    </script>\r\n" +
                "</body>");

        out.print("</html>");

    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        try {
            if (req.getParameter("data") != null) {
                String data = req.getParameter("data");
                String signature = req.getParameter("signature");

                byte[] dataArr = Base64.decodeBase64(data);
                byte[] signatureArr = Base64.decodeBase64(signature);
                
                VerifyResult result = cryptoService.verify(new ByteArrayInputStream(dataArr), signatureArr);
                for (SignerInfo signerInfo : result.getSignerInfos()) {
                    if (signerInfo.isValid()) {
                        out.print(signerInfo.getName() + " OK");
                    }else {
                        out.print("ERROR: " + signerInfo.getError());
                    }
                }
            } else {
                out.print("ERROR: No data for verify");
            }
        } catch (Exception ex) {
            out.print("ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
