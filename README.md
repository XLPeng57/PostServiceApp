
# I-Pickup

### Preparation to run the app
1. Install: MongoDB, NodeJS, Android Studio
2. Pull the code and connect to new_dev branch
   Terminal commands:
    1. `git clone https://code.wm.edu/CS/425/pickup.git`
    2. `git init`
    3. `cd pickup/Code`
    4. `git checkout new_dev`
    5. `git branch` see if you are under the right branch
    6. `git push origin new_dev:new_dev`
3. See if you can connect to the database:
    1. type in terminal: `mongo "mongodb+srv://cluster0.wbeif.mongodb.net/wminfo" --username xlpeng
       ` or `mongo` if you haven't connected to the database before
    2. enter the password *0507*

### Regular steps to run the application
1. run `mongo` in the terminal
2. under the js_wm_mongodb directory where you can see index.js, run `node index.js`
3. open PostServiceApp using Android Studio and run the emulator
4. test accounts: 
   * test1@email.wm.edu, password: 1218
   * test2@email.wm.edu, password: 1693



