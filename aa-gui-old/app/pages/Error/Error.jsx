import styles from './_Error.scss';

import React from 'react';

import PubSub from 'pubsub-js'

import i18n from 'i18next';

export default class Error extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {error: {}};
    PubSub.subscribe('ERROR_RECEIVED', (msg, data) => {
      let promise = data.error.response.json();
      Promise.resolve(promise).then((json) => {
        this.setState({error: json})
      });
    });
  }

  renderErrorDetails() {
    let error = this.state.error;
    return (<div className={styles.mod_container}>
      <p>{i18n.t('error.exception_environment', {profiles: error.profiles})}</p>
      <table className={styles.table}>
        <thead>
        <tr>
          <th>{i18n.t('error.key')}</th>
          <th>{i18n.t('error.value')}</th>
        </tr>
        </thead>
        <tbody>
        {Object.keys(error).map((key) =>
          <tr key={key}>
            <td>{key}</td>
            <td>{error[key]}</td>
          </tr>)}
        </tbody>
      </table>
    </div>)
  }

  renderError() {
    return (<div className={styles.mod_error}>
      <p>{i18n.t('error.exception_occurred')}</p>
    </div>)
  }

  render() {
    return this.state.error.profiles ? this.renderErrorDetails() : this.renderError()
  }
}
