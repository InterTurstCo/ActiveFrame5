;(function () {
	if(window.cryptoToolAsync)
		return;

	var cryptoToolAsync = {
		
		checkForPlugInAsync: function (plugin_loaded, plugin_loaded_error) {
			cadesplugin.async_spawn(function*(args) {
				try{
					var oAbout = yield cadesplugin.CreateObjectAsync("CAdESCOM.About");
					var version = yield oAbout.Version;
					args[0]();
				}catch(ex){
					args[1](ex);
				}					
			}, plugin_loaded, plugin_loaded_error);
		},

		getCertificatesAsync: function(callback){
			cadesplugin.async_spawn(function*(args) {
				try{
					var oStore = yield cadesplugin.CreateObjectAsync("CAPICOM.store");
					yield oStore.Open(cadesplugin.CAPICOM_CURRENT_USER_STORE, cadesplugin.CAPICOM_MY_STORE, cadesplugin.CAPICOM_STORE_OPEN_MAXIMUM_ALLOWED);
			
					//Получаем сертификаты
					cryptoTool.oCertificates = yield oStore.Certificates;
					
					// Из них не рассматриваются сертификаты, в которых отсутствует закрытый ключ.
					cryptoTool.oCertificates = yield cryptoTool.oCertificates.Find(cadesplugin.CAPICOM_CERTIFICATE_FIND_EXTENDED_PROPERTY, cryptoTool.CAPICOM_PROPID_KEY_PROV_INFO);
					
					// Из них выбираются только сертификаты, действительные в настоящее время.
					cryptoTool.oCertificates = yield cryptoTool.oCertificates.Find(cadesplugin.CAPICOM_CERTIFICATE_FIND_TIME_VALID);
					
					var count = yield cryptoTool.oCertificates.Count;
					if (count == 0) {
						args[0](null, new Error("Actual valid certificates not found"));
					}
					yield oStore.Close();                
				
					var result = [];
					for (var i=0; i<count; i++){
						var certificate = yield cryptoTool.oCertificates.Item(i+1);
						var subject = yield certificate.SubjectName;
						var toDate = yield certificate.ValidToDate;
						var certInfo = subject + " действителен до " + toDate; 
						result.push(certInfo);
					}
					args[0](result);

				}catch(ex){
					args[0](null, ex.message);
				}					
			}, callback);
		},

		signAsync: function(certNo, base64Content, callcack){
			cadesplugin.async_spawn(function*(args) {
				try {
					cryptoTool.oCertificate = yield cryptoTool.oCertificates.Item(args[0]);
				
					var oSigner = yield cadesplugin.CreateObjectAsync("CAdESCOM.CPSigner");
					yield oSigner.propset_Certificate(cryptoTool.oCertificate);
					yield oSigner.propset_TSAAddress(cryptoTool.tsAddress);
		
					var oSignedData = yield cadesplugin.CreateObjectAsync("CAdESCOM.CadesSignedData");
					if (cryptoTool.hashOnServer){
						// Создаем объект CAdESCOM.HashedData
						var oHashedData = yield cadesplugin.CreateObjectAsync("CAdESCOM.HashedData");

						// Инициализируем объект заранее вычисленным хэш-значением
						// Алгоритм хэширования нужно указать до того, как будет передано хэш-значение
						yield oHashedData.propset_Algorithm(cadesplugin.CADESCOM_HASH_ALGORITHM_CP_GOST_3411);
						yield oHashedData.SetHashValue(args[1]);

						var sSignedMessage = yield oSignedData.SignHash(oHashedData, oSigner, cadesplugin.CADESCOM_CADES_X_LONG_TYPE_1);
					}else{
						yield oSignedData.propset_ContentEncoding(cadesplugin.CADESCOM_BASE64_TO_BINARY);
						yield oSignedData.propset_Content(args[1]);
						var sSignedMessage = yield oSignedData.SignCades(oSigner, cadesplugin.CADESCOM_CADES_X_LONG_TYPE_1, true);
					}
					args[2](sSignedMessage, null);
				} catch (err) {
					args[2](null, "Failed to create signature. Error: " + err.message);
				}
			}, certNo, base64Content, callcack);			
		},
		
	};
	
	window.cryptoToolAsync = cryptoToolAsync;
}());

window.cryptoTool.async_resolve();