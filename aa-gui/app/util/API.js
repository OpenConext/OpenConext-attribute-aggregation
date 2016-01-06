import history from 'history';

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
    .then(json => callback(json));
    //todo 'react-router' is in flux on hwo to navigate outside components
    //.catch(ex => history.replace('/error'));
};

export default {

  getAggregations(callback) {
    return doFetch('/aa/api/internal/aggregations', callback);
  },

  getAggregation(id, callback) {
    return doFetch('/aa/api/internal/aggregation/' + id, callback);
  },

  getAuthorityConfiguration(callback) {
    return doFetch('/aa/api/internal/authorityConfiguration', callback);
  },

  getServiceProviders(callback) {
    return doFetch('/aa/api/internal/serviceProviders', callback);
  }

};
