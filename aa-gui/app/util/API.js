import createBrowserHistory from 'history/lib/createBrowserHistory'

const history = createBrowserHistory();

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

  doFetch = (url, callback, method = 'get') => {
    fetch(url, {
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN' : this.csrfToken
      },
      method: method,
      credentials: 'same-origin'
    })
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

}

export default new API()
