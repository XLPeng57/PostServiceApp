var mongodb = require('mongodb');
var ObjectID = mongodb.ObjectID;
var crypto = require('crypto');
var express = require('express');
var bodyParser = require('body-parser');
const { response } = require('express');

//express
var app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended:true}));

//mongodb
var MongoClient = mongodb.MongoClient;

//conneciton

const url = 'mongodb+srv://xlpeng:0507@cluster0.wbeif.mongodb.net/wminfo?retryWrites=true&w=majority'

MongoClient.connect(url,{useNewUrlParser:true},function(err,client){
    if (err)
        console.log('Unable to connect to the mongoDB server. Error', err);
    else{
        console.log('No Error');

        

        //register 
        app.post('/register',(request,response, next)=> {
            var post_data = request.body;
            var plaint_password = post_data.password;
            var hash_data = saltHashPassword(plaint_password);
            var password = hash_data.passwordHash;
            var salt = hash_data.salt;
            var name = post_data.name;
            var email = post_data.email;
            var csu = post_data.csu;

            var insertJson = {
                'email': email,
                'password': password,
                'salt': salt,
                'name': name,
                'csu': csu

            };

            var db = client.db('wminfo')

            db.collection('user')
                .find({'email':email}).count(function(err,number){
                    if(number !=0){
                        response.json('Email already exists');
                        console.log('Email already exists');

                    }else{
                        //insert 
                        db.collection('user')
                            .insertOne(insertJson,function(error,res){
                                response.json('Registration success');
                                console.log('Registration success');
                            })
                    }
                })

        });

        app.post('/login',(request,response, next)=> {
            var post_data = request.body;
            
            var email = post_data.email;
            var userPassword = post_data.password;

            var db = client.db('wminfo')

            db.collection('user')
                .find({'email':email}).count(function(err,number){
                    if(number == 0){
                        response.json('Email not exists');
                        console.log('Email not exists');

                    }else{
                        //insert 
                        db.collection('user')
                            .findOne({'email':email},function(err,user){
                                var salt = user.salt;
                                var hashed_password = checkHashPassword(userPassword,salt).passwordHash;
                                var encrypted_password = user.password;
                                if (hashed_password == encrypted_password){
                                    response.json('Login Success!');
                                    console.log('Login Success!');
                                }else{
                                    response.json('Wrong password.');
                                    console.log('Wrong password.');
                                }
                            })
                    }
                })

        });


        //db postoffice
        app.post('/scan_in',(request,response, next)=> {
            var office_data = request.body;
            var name = office_data.name;
            var email = office_data.email;
            var csu = office_data.csu;
            var package_id = office_data.package_id;
            var package_location = office_data.package_location;
            var package_size = office_data.package_size;
            var schedule = office_data.schedule;
            var complete =office_data.complete;

            var insertJson = {
                'email': email,
                'name': name,
                'csu': csu,
                'package_id': package_id,
                'package_location': package_location,
                'package_size': package_size,
                'schedule': schedule,
                'complete': complete,
        
            };

            var db1 = client.db('postoffice')

            db1.collection('package')
                .find({'package_id':package_id}).count(function(err,number){
                    if(number !=0){
                        response.json('Package already exists');
                        console.log('Package already exists');

                    }else{
                        //insert 
                        db1.collection('package')
                            .insertOne(insertJson,function(error,res){
                                response.json('Successfully added');
                                console.log('Successfully added');
                            })
                    }
                })

        });

        app.get('/alluser',(req,res) => {
            var db = client.db('postoffice');

            db.collection('package').find().toArray()
            .then(results => {
                res.json(results)
                console.log(results)
            })
            .catch(error => console.error(error))
        })

        app.get('/confirmation',(req,res) => {

            var get_data = req.query;
            var req_email = get_data.email;
            var req_schedule = get_data.schedule;

            var db = client.db('postoffice');

            db.collection('package').find({email:req_email, schedule:req_schedule}).toArray()
            .then(results => {
                res.json(results)
                console.log(results)
            })
            .catch(error => console.error(error))
        })

        app.get('/userinfo',(req,res) => {

            var get_data = req.query;
            var req_email = get_data.email;
        
            var db = client.db('wminfo');

            db.collection('user').find({email:req_email}).toArray()
            .then(results => {
                res.json(results)
                console.log(results)
            })
            .catch(error => console.error(error))
        })

        app.put('/schedule',(request,response, next)=> {
            var post_data = request.body;
            var find_email = post_data.email;
            var find_package_id = post_data.package_id;
    
            var db = client.db('postoffice')
            db.collection('package').findOneAndUpdate({email:find_email, package_id:find_package_id},
                {$set: {schedule:'yes'}});
            
            response.json("Scheduled");
            console.log("Scheduled");


        });

    
        app.listen(3000,()=> {
            console.log('Connected to MongoDB Server, webservice running on port 3000')
        })
    }

})