import { browserHistory } from 'react-router';

var checkStatus = (response) => {
  if (response.status >= 200 && response.status < 300) {
    return response
  } else {
    var error = new Error(response.statusText);
    error.response = response;
    throw error;
  }
};

var doFetch = (url, callback) => {
  fetch(url, {
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    credentials: 'same-origin'
  })
    .then(checkStatus)
    .then(res => res.json())
    .then(json => callback(json))
    .catch(ex => browserHistory.replace('/error'));
};

export default {

  getAggregations(callback) {
    return doFetch('/aa/api/aggregations', callback);
  },

  getAuthorities(callback) {
    return doFetch('/aa/api/authorities', callback);
  }


};
