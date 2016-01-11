import styles from './_Aggregation.scss'

import React from 'react'
import i18n from 'i18next'
import moment from 'moment'

import API from '../../util/API'
import Utils from '../../util/Utils'

export default class Aggregation extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {aggregation: {}, serviceProviders: [], errors: {}};
    API.getServiceProviders((json) => this.setState({serviceProviders: json}));
    let id = props.params.id;
    if (id !== 'new') {
      API.getAggregation(id, (json) => this.setState({aggregation: json}));
    }
  }

  componentWillMount() {
    this._mounted = true;
  }

  componentWillUnmount() {
    this._mounted = true;
  }

  componentWillReceiveProps(nextProps) {
    if (this._mounted) {
      let id = nextProps.params.id;
      if (id === 'new') {
        this.setState({aggregation: {}});
      }
    }
  }

  handleSubmit = (e) => {
    Utils.stop(e);

  };

  handleCancel = (e) => {
    Utils.stop(e);
    this.props.history.replace('/aggregations');
  };

  renderName() {
    var aggregation = this.state.aggregation;
    let handleOnChange = (e) => this.setState({aggregation: {name: e.target.value}});

    var style = Utils.isEmpty(aggregation.name) ? styles.failure : styles.success;
    return (
      <div className={style}>
        <label htmlFor='name'>{i18n.t('aggregation.name')}</label>
        <input type="text" name="name" value={aggregation.name}
               onChange={handleOnChange}/>
      </div>
    );
  }

  renderServiceProviders() {
    var aggregation = this.state.aggregation;
    let handleOnChange = (e) => this.setState({aggregation: {serviceProviders: e.target.value}});

    var style = Utils.isEmpty(aggregation.serviceProviders) ? styles.failure : styles.success;
    return (
      <div className={style}>
        <label htmlFor='serviceProviders'>{i18n.t('aggregation.serviceProviders')}</label>
        <input type="text" name="serviceProviders" value={aggregation.name}
               onChange={handleOnChange}/>
      </div>
    );
  }

  renderAttributes() {
    var aggregation = this.state.aggregation;
    let handleOnChange = (e) => this.setState({aggregation: {attributes: e.target.value}});

    var style = Utils.isEmpty(aggregation.attributes) ? styles.failure : styles.success;
    return (
      <div className={style}>
        <label htmlFor='attributes'>{i18n.t('aggregation.attributes')}</label>
        <input type="text" name="attributes" value={aggregation.name}
               onChange={handleOnChange}/>
      </div>
    );
  }

  renderActions() {
    var aggregation = this.state.aggregation;
    let submitStyle = Utils.isEmpty(aggregation.name) ? styles.button_submit_disabled : styles.button_submit;
    return (
      <div className={styles.actions}>
        <a className={submitStyle} href="#"
           onClick={this.handleSubmit}>{i18n.t("aggregation.submit")}</a>
        <a className={styles.button_cancel} href="#" onClick={this.handleCancel}>{i18n.t("aggregation.cancel")}</a>
      </div>
    );
  }

  render() {
    var aggregation = this.state.aggregation;
    var title = aggregation.id ? i18n.t('aggregation.update', {name: aggregation.name}) : i18n.t("aggregation.create");
    var subtitle = aggregation.id ? i18n.t('aggregation.created', {
      name: aggregation.userDisplayName,
      date: moment(aggregation.created).format('LLLL')
    }) : '';

    return (
      <div className={styles.mod_container_aggregation}>
        <section className={styles.header}>{title}<em>{subtitle}</em></section>
        {this.renderName()}
        {this.renderServiceProviders()}
        {this.renderAttributes()}
        {this.renderActions()}
      </div>
    );
  }

}

