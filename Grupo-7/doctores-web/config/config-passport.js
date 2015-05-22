var https = require('https');
var localStrategy = require('passport-local').Strategy;


function config(passport){
	passport.serializeUser(function(user,done){
		done(null,true);
	});

	passport.deserializeUser(function(tipo,done){
		if(tipo)
			done(null,tipo);
		else
			done({},null);
	});

	passport.use('local-signup',new localStrategy({
		usernameField : 'username',
		passwordField : 'password',
		passReqToCallback : true
	},
	function(req,email,password,done){
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
					done(null,false);
				}else{
					done(null,{
						username : data.username
					});
				}
			});
		});

		reqPost.write(JSON.stringify(req.body));
		reqPost.end();
	}));
}

module.exports = config;