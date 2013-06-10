var http    = require('http'),
    io      = require('socket.io'),
    fs      = require('fs'),
    util    = require('util'),
    sys     = require("sys"),
    spawn   = require('child_process').spawn,



http = http.createServer(handler);
http.listen(8080);
io = io.listen(http);

function handler(req, res) {
    fs.readFile(__dirname+'/test2.html',
    function(err, data){
    if (err) {
      res.writeHead(500);
      return res.end('Error loading index.html');
    }
        req.setEncoding(encoding="utf8");
        res.writeHead(200);
        res.end(data);
    });
}

io.sockets.on('connection',function(socket){ 
    // this part is juse a test
    socket.emit('msg',{hi:'Happy new year.'});
    socket.on('msg',function(data){
        console.log('Get a msg from client ...'+data.msg);
        socket.emit('user message',data);
    });
	
	//the code below is used to subscribe the data from winter and send the data to web page by websocket
    mosq = spawn('mosquitto_sub',['-h','winter.ceit.uq.edu.au','-t','test']);

    mosq.stdout.on('data', function (data) {            
        console.log('get data from mosquitto:'+data.toString());
	    var value=data.toString();
	    socket.emit('user message',{msg:value});
     });
});
     
      