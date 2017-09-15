;(function () {
	if(window.cryptoTool)
		return;	
	
	var cryptoTool = {
		CAPICOM_PROPID_KEY_PROV_INFO: 2,
		
		//Флаг загруженности плагина
		pluginInstalled: false,
		certificateNo: -1,
		
		//Флаг загруженности библиотеки для асинхронных вызовов
		async_code_included: false,
		async_promise: null,
		async_resolve: null,
		hashOnServer: false,
		certificateNumbers: {},
		certificateIds: {},
		
		
		checkInstall: function(){
			return this.pluginInstalled;
		},

		setCertificate: function(cerNo){
			this.certificateNo = cerNo;
		},
		
		findCertificateById: function(certificateId){
			var result = this.certificateNumbers[certificateId];
			if (result != null){
				return result;
			}
			return -1;
		},

		getCertificate: function(){
			return this.certificateNo;
		},

		getCertificateId: function(certNo){
			return this.certificateIds[certNo];
		},
		
		checkForPlugIn: function (plugin_loaded, plugin_loaded_error) {
			cadesplugin.set_log_level(cadesplugin.LOG_LEVEL_DEBUG);
			var canAsync = !!cadesplugin.CreateObjectAsync;
			if(canAsync){
				this.includeAsyncCode().then(function(){
					cryptoToolAsync.checkForPlugInAsync(plugin_loaded, plugin_loaded_error);					
				});				
			}else{
				checkForPlugInNPAPI(plugin_loaded, plugin_loaded_error);
			}
		},

		includeAsyncCode: function()
		{
			if(!this.async_code_included){
				this.async_promise = new Promise(function(resolve, reject){
					window.cryptoTool.async_resolve = resolve;
				});

				var fileref = document.createElement('script');
				fileref.setAttribute("type", "text/javascript");
				fileref.setAttribute("src", "js/crypto-tool-async.js");
				document.getElementsByTagName("head")[0].appendChild(fileref);
				this.async_code_included = true;
			}
			return this.async_promise;			
		},
		
		checkForPlugInNPAPI: function (plugin_loaded, plugin_loaded_error) {
			try{
				var oAbout = cadesplugin.CreateObject("CAdESCOM.About");
				var version = oAbout.Version;
				plugin_loaded();
			}catch(ex){
				plugin_loaded_error(ex);
			}
		},

		init: function (tsAddress, hashOnServer, collback) {
			this.tsAddress = tsAddress;
			this.hashOnServer = hashOnServer;

			if (cryptoTool.pluginInstalled){
				return collback(true);
			}else{
				try{
					cadesplugin.set_log_level(cadesplugin.LOG_LEVEL_DEBUG);
					var canPromise = !!window.Promise;
					if(canPromise) {						
						cadesplugin.then(function () {								
								cryptoTool.checkForPlugIn(function(){
									cryptoTool.pluginInstalled=true;
									collback(true);
									}, function(err){
										cryptoTool.pluginInstalled=false;
										collback(false, err)
										});								
							},
							function(error) {
								cryptoTool.pluginInstalled=false;
								collback(false, error);
							}
					   );
					} else {
						window.addEventListener("message", function (event){
							if (event.data == "cadesplugin_loaded") {
								var oAbout = cadesplugin.CreateObject("CAdESCOM.About");
								var version = oAbout.Version;
								if (!cryptoTool.pluginInstalled){
									cryptoTool.pluginInstalled=true;
									collback(true);
								}
							} else if(event.data == "cadesplugin_load_error") {
								cryptoTool.pluginInstalled=false;
								collback(false);
							}
						},
						false);
						window.postMessage("cadesplugin_echo_request", "*");
					}
				}catch(err){
					this.pluginInstalled = false;
				}     
			}			
		},
		
		getCertificates: function (callback) {
			var canAsync = !!cadesplugin.CreateObjectAsync;
			if(canAsync){
				cryptoToolAsync.getCertificatesAsync(callback);
			}else{
				var oStore = cadesplugin.CreateObject("CAPICOM.store");
				oStore.Open(cadesplugin.CAPICOM_CURRENT_USER_STORE, cadesplugin.CAPICOM_MY_STORE, cadesplugin.CAPICOM_STORE_OPEN_MAXIMUM_ALLOWED);
		
				//Получаем сертификаты
				this.oCertificates = oStore.Certificates;
				
				// Из них не рассматриваются сертификаты, в которых отсутствует закрытый ключ.
				this.oCertificates = this.oCertificates.Find(cadesplugin.CAPICOM_CERTIFICATE_FIND_EXTENDED_PROPERTY, this.CAPICOM_PROPID_KEY_PROV_INFO);
				
				// Из них выбираются только сертификаты, действительные в настоящее время.
				this.oCertificates = this.oCertificates.Find(cadesplugin.CAPICOM_CERTIFICATE_FIND_TIME_VALID);
				
				if (this.oCertificates.Count == 0) {
					throw "Actual valid certificates not found";
				}
				oStore.Close();                
			
				var result = [];
				for (var i=0; i<this.oCertificates.Count; i++){
					result.push(this.oCertificates.Item(i+1).SubjectName + " действителен до " + this.oCertificates.Item(i+1).ValidToDate);
					this.certificateNumbers[this.oCertificates.Item(i+1).SerialNumber]=i+1;
					this.certificateIds[i+1]=this.oCertificates.Item(i+1).SerialNumber;
				}
				callback(result);
			}            
		},

		sign: function(certNo, base64Content, callcack){
			var canAsync = !!cadesplugin.CreateObjectAsync;
			if(canAsync){
				cryptoToolAsync.signAsync(certNo, base64Content, callcack);
			}else{
				this.oCertificate = this.oCertificates(certNo);
				var oSigner = cadesplugin.CreateObject("CAdESCOM.CPSigner");
				oSigner.Certificate = this.oCertificate;
				oSigner.TSAAddress = this.tsAddress;
				//oSigner.KeyPin = "111111";
		
				try {
					var oSignedData = cadesplugin.CreateObject("CAdESCOM.CadesSignedData");
					if (this.hashOnServer){
						// Создаем объект CAdESCOM.HashedData
						var oHashedData = cadesplugin.CreateObject("CAdESCOM.HashedData");

						// Инициализируем объект заранее вычисленным хэш-значением
						// Алгоритм хэширования нужно указать до того, как будет передано хэш-значение
						oHashedData.Algorithm = cadesplugin.CADESCOM_HASH_ALGORITHM_CP_GOST_3411_2012_256;
						oHashedData.SetHashValue(base64Content);

						var sSignedMessage = oSignedData.SignHash(oHashedData, oSigner, cadesplugin.CADESCOM_CADES_X_LONG_TYPE_1);
					}else{
						oSignedData.ContentEncoding = cadesplugin.CADESCOM_BASE64_TO_BINARY;
						oSignedData.Content = base64Content;
						var sSignedMessage = oSignedData.SignCades(oSigner, cadesplugin.CADESCOM_CADES_X_LONG_TYPE_1, true);
					}
					callcack(sSignedMessage, null);
				} catch (err) {
					callcack(null, "Failed to create signature. Error: " + err.message);
				}
			}
		}
	};   
	
	window.cryptoTool = cryptoTool;
}());	
