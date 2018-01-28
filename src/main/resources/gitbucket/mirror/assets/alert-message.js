let alertMessageKey = 'alertMessage';

function displayAlertMessage() {

  let jsonMessage = sessionStorage.getItem(alertMessageKey);

  if (jsonMessage != null) {

    let message = JSON.parse(jsonMessage);

    sessionStorage.removeItem(alertMessageKey);

    $('#alert-container').append(
      '<div class="alert alert-' + message.type + ' fade in">' +
        '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>' +
        message.content +
      '</div>'
    );

  }

}

function setAlertMessage(content, type) {

  let message = { content: content, type: type };

  sessionStorage.setItem(alertMessageKey, JSON.stringify(message));
}