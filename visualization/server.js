var compression = require('compression');
var express = require('express');
var app = express();


app.use(compression());
app.use(express.static(__dirname + '/public', {maxAge: 3600000}));

var port = process.env.PORT || 3000;

app.listen(port);

console.log('Listening on port', port);