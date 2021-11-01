const genRandomString=require("./generateRamdomString");

const sha512=(password,salt)=>{
    const hash = crypto.createHmac('sha512',salt);
    hash.update(password);
    const value = hash.digest('hex');
    return{
        salt:salt,
        passwordHash:value
    };
}

const saltHashPassword=(userPassword)=>{
    const salt = genRandomString(16);
    const passwordData = sha512(userPassword,salt);
    return passwordData;
}

const checkHashPassword=(userPassword,salt)=>{
    const passwordData = sha512(userPassword,salt);
    return passwordData;
}

module.exports={
    sha512:sha512,
    saltHashPassword:saltHashPassword,
    checkHashPassword:checkHashPassword
};