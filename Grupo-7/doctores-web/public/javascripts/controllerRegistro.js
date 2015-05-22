angular.module('doctoresLogin',[])
	.controller('registroController',['$http',function($http){
		this.registro = {};
		this.login = {};
		var self = this;
		this.crearCuenta = function(){
			$http.post('/registro', self.registro).
				  success(function(data, status, headers, config) {
				    // this callback will be called asynchronously
				    // when the response is available
				    console.log(data);
				  }).
				  error(function(data, status, headers, config) {
				    // called asynchronously if an error occurs
				    // or server returns response with an error status.
				  });
		}
		
	}]);