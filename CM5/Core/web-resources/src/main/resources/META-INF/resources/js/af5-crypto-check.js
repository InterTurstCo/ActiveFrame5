;(function () {
    if(window.af5CryptoTool)
        return;

    var canPromise = !!window.Promise;

    var af5CryptoTool = {

        af5_crypto_code_included: false,
        af5_crypto_settings_checked: false,
        /*af5_crypto_promise: null,*/
        /*af5_resolve: null,*/

        af5CheckForSettingsAndInclude: function() {
            if (canPromise) {
                var promise = this.af5CheckForSettings();
                if (promise != null) {
                    promise.then(function (param) {
                        if (param == "1") {
                            this.af5CryptoTool.af5IncludeCryptoCode();
                        }
                    });
                }
            }
        },

        af5CheckForSettings: function () {
            var promise = null;
            if(!this.af5_crypto_settings_checked){
                this.af5_crypto_settings_checked = true;
                promise = new Promise(function(succeed, fail) {
                    var request = new XMLHttpRequest();
                    var url = "remote/service/af5-check-crypto-settings";
                    request.open("GET", url, true);
                    request.addEventListener("load", function() {
                        if (request.status < 400)
                            succeed(request.response);
                        else
                            fail(new Error("Request failed: " + request.statusText));
                    });
                    request.addEventListener("error", function() {
                        fail(new Error("Network error"));
                    });
                    request.send();
                });
            }
            return promise;
        },

        af5IncludeCryptoCode: function() {
            if(!this.af5_crypto_code_included){
                var fileref = document.createElement('script');
                fileref.setAttribute("type", "text/javascript");
                fileref.setAttribute("src", "js/cadesplugin_api.js");
                document.getElementsByTagName("head")[0].appendChild(fileref);

                fileref = document.createElement('script');
                fileref.setAttribute("type", "text/javascript");
                fileref.setAttribute("src", "js/crypto-tool.js");
                document.getElementsByTagName("head")[0].appendChild(fileref);

                this.af5_crypto_code_included = true;
            }
        },
    };
    window.af5CryptoTool = af5CryptoTool;

    af5CryptoTool.af5CheckForSettingsAndInclude();

}());
