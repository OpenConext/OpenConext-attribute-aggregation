import createBrowserHistory from 'history/lib/createBrowserHistory'

const history = createBrowserHistory();

import Utils from './Utils'

class API {

  constructor() {
    this.csrfToken = undefined;
  }

  checkStatus = (response) => {
    if (response.status >= 200 && response.status < 300) {
      this.csrfToken = response.headers.get('X-CSRF-TOKEN');
      return response
    } else {
      var error = new Error(response.statusText);
      error.response = response;
      throw error;
    }
  };

  doFetch = (url, callback, method = 'get', form) => {
    var options = {
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN' : this.csrfToken
      },
      method: method,
      credentials: 'same-origin'
      };
      if (form && (method === 'post' || method === 'put')) {
        options.body = JSON.stringify(form);
      }
      fetch(url, options)
      .then(this.checkStatus)
      .then(res => res.json())
      .then(json => callback(json))
      .catch(ex => history.replace('/#/error'));
  };

  getAggregations(callback) {
    return this.doFetch('/aa/api/internal/aggregations', callback);
  }

  getAggregation(id, callback) {
    return this.doFetch('/aa/api/internal/aggregation/' + id, callback);
  }

  getUser(callback) {
    return this.doFetch('/aa/api/internal/users/me', callback);
  }

  getAuthorityConfiguration(callback) {
    return this.doFetch('/aa/api/internal/authorityConfiguration', callback);
  }

  getServiceProviders(callback) {
    return this.doFetch('/aa/api/internal/serviceProviders', callback);
  }

  deleteAggregation(id, callback) {
    return this.doFetch('/aa/api/internal/aggregation/' + id, callback, 'delete');
  }

  saveAggregation(aggregation, callback) {
    let method = Utils.isEmpty(aggregation.id) ? 'post' : 'put';
    return this.doFetch('/aa/api/internal/aggregation', callback, method, aggregation);
  }
}

export default new API()
