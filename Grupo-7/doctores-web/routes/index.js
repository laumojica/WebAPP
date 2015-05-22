var express = require('express');
var router = express.Router();
var https = require('https');

function ruta(passport){
	router.get('/', function(req, res, next) {
	  res.render('index', { title: 'Express' });
	});

	router.get('/registro',function(req,res,next){
		res.render('registro');
	});

	router.post('/registro',function(req,res,next){
		var headersPost = {
			"Accept": "application/json",
			"Content-Type": "application/json",
			'Authorization': 'Basic ' + new Buffer('4VMUI11066CHG4RE26PO6TTCU' + ':' + '/mMiyvA/44fbgV4IsYkI1HWj4WT54c+y5pw5ny6sg/g').toString('base64')
		}

		var postOptions = {
			host : 'api.stormpath.com',
			path : '/v1/applications/4EqqQbydZM7Zvn0eePATwG/accounts',
			headers : headersPost,
			method: 'POST'
		}

		var reqPost = https.request(postOptions,function(res){
			var data = '';
			res.on('data',function(d){
				data += d;
			});
			res.on('end',function(){
				data = JSON.parse(data);
				if(data.status === undefined){
					retosponder(false);
				}else{
					retosponder(true);
				}
			});
		});

		reqPost.write(JSON.stringify(req.body));
		reqPost.end();

		function retosponder(exito){
			res.setHeader('Content-Type','application/json');
			res.end(JSON.stringify({registrado:exito}));
		}
	});

	return router;
}

module.exports = ruta;
