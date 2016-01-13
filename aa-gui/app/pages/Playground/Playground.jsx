import styles from './_Playground.scss';

import React from 'react'
import update from 'react-addons-update'
import i18n from 'i18next'
import _ from 'lodash'
import Select from 'react-select'

import API from '../../util/API'
import Utils from '../../util/Utils'

export default class Playground extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {
      authorities: [{requiredInputAttributes: [], attributes: []}],
      serviceProviders: [],
      aggregations: [],
      play: {aggregation: {}, serviceProvider: {}}
    };

    API.getServiceProviders((json) => this.setState({serviceProviders: json}));
    API.getAggregations((json) => {
      var aggregations = json.sort((a, b) => a.name.localeCompare(b.name));
      this.setState({aggregations: aggregations});
    });
    API.getAuthorityConfiguration((json) => this.setState({authorities: json.authorities}));
  }

  updatePlayState(newPartialState) {
    var newState = update(this.state.play, {$merge: newPartialState});
    this.setState({play: newState})
  }


  renderAggregations() {
    let handleOnChange = (val) => this.updatePlayState({aggregation: val || {}})

    let aggregations = this.state.aggregations;
    return (
      <div className={styles.success}>
        <label htmlFor='aggregations'>{i18n.t('playground.aggregations')}</label>
        <em className={styles.info}>{i18n.t('playground.aggregationsInfo')}</em>
        <Select
          name='aggregations'
          labelKey='name'
          valueKey='name'
          value={this.state.play.aggregation.name}
          delimiter=','
          options={this.state.aggregations}
          onChange={handleOnChange}
          multi={false}
          placeholder='Select one Aggregation'
        />
      </div>
    );
  }

  renderServiceProviders() {
    let handleOnChange = (val) => this.updatePlayState({serviceProvider: val || []})

    let play = this.state.play;
    let entityId = play.aggregation.serviceProviders ? play.aggregation.serviceProviders[0].entityId
      : play.serviceProvider ? play.serviceProvider.entityId : undefined;
    return (
      <div className={Utils.isEmpty(play.serviceProvider) ? styles.failure : styles.success}>
        <label htmlFor='serviceProvider'>{i18n.t('playground.serviceProvider')}</label>
        <Select
          name='serviceProvider'
          labelKey='name'
          valueKey='entityId'
          value={entityId}
          options={this.state.serviceProviders}
          onChange={handleOnChange}
          multi={false}
          placeholder='Select one ServiceProvider'
        />
      </div>
    );
  }

  renderInput() {
    return (
      <div>
        <section className={styles.header_title}>{i18n.t('playground.title')}</section>
        {this.renderAggregations()}
        {this.renderServiceProviders()}
      </div>)
  }

  renderRight() {
    return this.state.play.result ? this.renderPlay() : this.renderAbout()
  }

  renderPlay() {
    return (
      <div>
        <p>Todo</p>
      </div>)
  }

  renderAbout() {
    return (
      <div className={styles.about}>
        <p className={styles.header}>{i18n.t('playground.aboutTitle')}</p>
        <p>Lorom Ipsum</p>
      </div>
    )
  }

  render() {
    return (
      <div className={styles.mod_container}>
        <div className={styles.mod_left_playground}>
          {this.renderInput()}
        </div>
        <div className={styles.mod_right_playground}>
          {this.renderRight()}
        </div>

      </div>
    );
  }
}
