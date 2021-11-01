const mongoose = require("mongoose");

const schema = mongoose.Schema({
    _id:String,
    email:String,
    password:String,
    salt:String,
    name:String,
    csu:String
});

module.exports = mongoose.model("User", schema);