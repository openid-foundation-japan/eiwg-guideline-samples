// First, parse the query string
var params = {}, postBody = location.hash.substring(1),
  regex = /([^&=]+)=([^&]*)/g, m;
while (m = regex.exec(postBody)) {
  params[decodeURIComponent(m[1])] = decodeURIComponent(m[2]);
}

// And send the token over to the server
var req = new XMLHttpRequest();
// using POST so query isn't logged
req.open('POST', '//' + window.location.host +
  '/catch_response', true);
req.setRequestHeader('Content-Type',
  'application/x-www-form-urlencoded');

//for Rails CSRF Protection
var token = document.querySelector("meta[name='csrf-token']").attributes['content'].value;
req.setRequestHeader('X-CSRF-Token', token);

req.onreadystatechange = function (e) {
  if (req.readyState == 4) {
    if (req.status == 200) {
      // If the response from the POST is 200 OK, perform a redirect
      window.location = '//'
        + window.location.host + '/after_login'
    }
    // if the OAuth response is invalid, generate an error message
    else if (req.status == 400) {
      alert('There was an error processing the token')
    } else {
      alert('Something other than 200 was returned')
    }
  }
};
req.send(postBody);
