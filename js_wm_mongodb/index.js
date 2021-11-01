var mongodb = require('mongodb');
var ObjectID = mongodb.ObjectID;
var crypto = require('crypto');
var express = require('express');
var bodyParser = require('body-parser');
const { response } = require('express');

//PASSWORD UTILS

//CREATE Fuction to random salt
var genRandomString = function(length){
    return crypto.randomBytes(Math.ceil(length/2))
    .toString('hex')
    .slice(0,length);
};

var sha512 = function(password,salt){
    var hash = crypto.createHmac('sha512',salt);
    hash.update(password);
    var value = hash.digest('hex');
    return{
        salt:salt,
        passwordHash:value
    };
}

function saltHashPassword(userPassword){
    var salt = genRandomString(16);
    var passwordData = sha512(userPassword,salt);
    return passwordData;
}

function checkHashPassword(userPassword,salt){
    var passwordData = sha512(userPassword,salt);
    return passwordData;
}

//express
var app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended:true}));

//mongodb
var MongoClient = mongodb.MongoClient;

//conneciton
var url = 'mongodb://localhost:27017'

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

            var insertJson = {
                'email': email,
                'name': name,
                'csu': csu,
                'package_id': package_id,
                'package_location': package_location,
                'package_size': package_size,
                'schedule': 'no',
                'complete': 'no',
        
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

        //post request to create a schedule
        app.post('/create_schedule',(request,response, next)=> {

            var data = request.body;
            var id = data.id;
            var date = data.date;
            var time = data.time;
            var email = data.email;
            var packageIDs = data.packageIDs;
            var walkIn = (data.walkIn==='true');

            var insertJson = {
                'id': id,
                'date': date,
                'time': time,
                'email':email,
                'packageIDs': packageIDs,
                'walkIn':walkIn,
                'fulfilled': false
            };

            var db1=client.db('postoffice')

            db1.collection('schedule').insertOne(insertJson,function(error,res){
                response.json('Schedule is created successfully');
                console.log('Schedule is created successfully');
            })
        });
        //one instance of post office database for all get methods
        var postoffice = client.db('postoffice');
        //get a user's arrived yet not picked up packages by email
        app.get('/getPackagesByEmail',(req,res) => {
            var get_data=req.query;
            var req_email=get_data.email;
            postoffice.collection('package').find({email:req_email}).toArray()
            .then(results => {
                res.json(results)
            })
            .catch(error => console.error(error))
        })

        app.get('/confirmation',(req,res) => {
            var get_data = req.query;
            var req_email = get_data.email;
            var req_schedule = get_data.schedule;


            postoffice.collection('package').find({email:req_email, schedule:req_schedule}).toArray()
            .then(results => {
                res.json(results)
                //console.log(results)
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

        app.get('/getUserByCSU',(req,res) => {
            var get_data=req.query;
            var req_csu=get_data.csu;

            var db = client.db('wminfo');
            db.collection('user').find({csu:req_csu}).toArray()
            .then(results => {
                res.json(results)
            })
            .catch(error => console.error(error))
        })

        //get all walk-in, unfulfilled schedules in the database
        app.get('/getAllSchedules',(req,res) => {

            postoffice.collection('schedule').find({fulfilled:false, walkIn:true}).toArray()
            .then(results => {
                res.json(results)
                //console.log(results)
            })
            .catch(error => console.error(error))
        })
        //count the number of walk-in schedule requests by given date and time
        app.get('/countScheduleByTime', (req, res)=>{
            var get_data = req.query;
            var req_date = get_data.date;
            var req_time = get_data.time;

            postoffice.collection('schedule').countDocuments({date:req_date,time:req_time,walkIn:true})
            .then(results => {
                 res.json(results)
             }).catch(error => console.error(error))
        })
        //count the number of locker pickup requests by given date
        app.get('/countLockerByDate', (req, res)=>{
            var get_data = req.query;
            var req_date = get_data.date;

            postoffice.collection('schedule').countDocuments({date:req_date,walkIn:false})
            .then(results => {
                 res.json(results)
             }).catch(error => console.error(error))
        })


        app.put('/schedule',(request,response, next)=> {
            var post_data = request.body;
            var find_email = post_data.email;
            var find_package_id = post_data.package_id;

            postoffice.collection('package').findOneAndUpdate({email:find_email, package_id:find_package_id},
                {$set: {schedule:'yes'}});
            console.log('Scheduled');


        });
        //fulfill a request by scheduleID
        app.put('/fulfillRequestByID',(req, res, next) => {
            var put_data = req.body;
            var req_id=put_data.id;
            postoffice.collection('schedule').findOneAndUpdate({id:req_id},{$set:{fulfilled:true}});
            res.send('fulfilled a request');

        })
        //update package status: 'complete' = yes
        app.put('/completePackageByID',(req, res, next) => {
            var put_data = req.body;
            var id=put_data.id;
            postoffice.collection('package').findOneAndUpdate({package_id:id},{$set:{complete:'yes'}});
            res.send('package with ID is completed');
        })

        app.get('/getPackageWithId',(req,res) => {
            var get_data = req.query;
            var req_packId = get_data.package_id;

            var db = client.db('postoffice');

            db.collection('package').find({package_id:req_packId}).toArray()
            .then(results => {
                res.json(results)
                console.log(results)
            })
            .catch(error => console.error(error))
        })

    
        app.listen(3000,()=> {
            console.log('Connected to MongoDB Server, webservice running on port 3000')
        })
    }

})
