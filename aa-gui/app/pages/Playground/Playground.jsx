import styles from './_Playground.scss';

import React from 'react'
import { Link } from 'react-router'
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
      result: undefined,
      play: {aggregation: {}, serviceProvider: {}, inputParameters: {}}
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

  validPlay() {
    let play = this.state.play;
    let isValid = (obj) =>  Object.keys(obj).length > 0;
    return isValid(play.aggregation) && isValid(play.serviceProvider);
  }

  renderAggregations() {
    let handleOnChange = (val) => {
      let entityId = val ? val.serviceProviders[0].entityId : undefined;
      this.updatePlayState({aggregation: val || {}, serviceProvider: {entityId: entityId}, inputParameters: {}})
    }

    let aggregations = this.state.aggregations;
    return (
      <div className={Utils.isEmpty(this.state.play.aggregation) ? styles.failure : styles.success}>
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
    let handleOnChange = (val) => this.updatePlayState({serviceProvider: val || {}})

    let play = this.state.play;
    let entityId = play.serviceProvider.entityId ? play.serviceProvider.entityId :
      play.aggregation.serviceProviders ? play.aggregation.serviceProviders[0].entityId : undefined
    return (
      <div className={Utils.isEmpty(entityId) ? styles.failure : styles.success}>
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

  renderInputParameters() {
    let handleOnChange = (e) => {
      this.state.play.inputParameters[e.target.name] = e.target.value;
      this.updatePlayState({inputParameters: this.state.play.inputParameters})
    }
    let uniqueRequiredAttributeNames = [];

    var attributes = this.state.play.aggregation.attributes;
    if (attributes) {
      //get all required attributes from the authorities linked to the attributes of the aggration
      let requiredAttributeNames = attributes
        .map((attribute) => attribute.attributeAuthorityId)
        .map((attributeAuthorityId) => this.state.authorities.find((authority) => authority.id === attributeAuthorityId).requiredInputAttributes)
        .reduce((attr1, attr2) => attr1.concat(attr2))
        .map((requiredInputAttribute) => requiredInputAttribute.name)
      uniqueRequiredAttributeNames = Array.from(new Set(requiredAttributeNames));
    }
    //we permit empty user attributes to see empty result
    return (
      <div className={Utils.isEmpty(this.state.play.inputParameters) ? styles.failure : styles.success}>
        <label>{i18n.t('playground.userAttributes')}</label>
        {uniqueRequiredAttributeNames.map((name) =>
          <div key={name} className={styles.user_attributes}>
            <label>{name}</label>
            <input className={styles.input} type="text" name={name} onChange={handleOnChange}
                   value={this.state.play.inputParameters[name]}/>
          </div>
        )}
      </div>
    )
  }

  renderActions() {
    return (
      <div className={styles.playground_actions}>
        <a className={this.validPlay() ? styles.button_submit_small : styles.button_submit_small_disabled} href="#"
           onClick={this.handleMe}>{i18n.t("playground.me")}</a>
        <a
          className={this.state.play.serviceProvider.entityId ? styles.button_submit_small : styles.button_submit_small_disabled}
          href="#" onClick={this.handleSchema}>{i18n.t("playground.schema")}</a>
        <a
          className={this.state.play.serviceProvider.entityId ? styles.button_submit_small : styles.button_submit_small_disabled}
          href="#" onClick={this.handleResourceType}>{i18n.t("playground.resource_type")}</a>
        <a className={styles.button_white} href="#"
           onClick={this.handleConfiguration}>{i18n.t("playground.service_provider_configuration")}</a>
        <a className={styles.button_cancel} href="#" onClick={this.handleCancel}>{i18n.t("playground.clear")}</a>
        <a className={this.validPlay() ? styles.button_full : styles.button_full_disabled} href="#"
           onClick={this.handleEBInternal}>
          <i className="fa fa-sitemap"></i>
          {i18n.t("playground.engine_block")}
        </a>

      </div>
    );
  }

  handleResult = (json) => {
    this.setState({result: json});
    document.body.scrollTop = document.documentElement.scrollTop = 0;
  }

  handleMe = (e) => {
    Utils.stop(e);
    API.getMe(this.handleResult, this.state.play.serviceProvider.entityId, this.state.play.inputParameters)
  };

  handleSchema = (e) => {
    Utils.stop(e);
    API.getSchema(this.handleResult, this.state.play.serviceProvider.entityId)
  };

  handleConfiguration = (e) => {
    Utils.stop(e);
    API.getServiceProviderConfiguration(this.handleResult)
  };

  handleResourceType = (e) => {
    Utils.stop(e);
    API.getResourceType(this.handleResult, this.state.play.serviceProvider.entityId)
  };

  handleCancel = (e) => {
    Utils.stop(e);
    this.setState({result: undefined, play: {aggregation: {}, serviceProvider: {}, inputParameters: {}}});
  };

  handleEBInternal = (e) => {
    Utils.stop(e);
    let inputParameters = this.state.play.inputParameters;
    let attributes = Object.keys(inputParameters).map((name) => {
      return {name: name, values: [inputParameters[name]]}
    });

    let userAttributes = {serviceProviderEntityId: this.state.play.serviceProvider.entityId, attributes: attributes}
    API.attributeAggregate(this.handleResult, userAttributes)
  };

  renderLeft() {
    return (
      <div>
        <section className={styles.header_title}>{i18n.t('playground.title')}</section>
        {this.renderAggregations()}
        {this.renderServiceProviders()}
        {this.renderInputParameters()}
        {this.renderActions()}
      </div>)
  }

  renderRight() {
    return this.state.result ? this.renderResult() : this.renderAbout()
  }

  renderResult() {
    let result = this.state.result;
    let [style, icon, status] = result.error ?
      [styles.playground_error_result, 'fa-remove', 'error'] :
      [styles.playground_result, 'fa-check', 'ok'];
    return (
      <div className={style}>
        <section>
          <i className={'fa ' + icon}></i>
          <article>
            <h1>{i18n.t('playground.result_status_' + status)}</h1>
            <em></em>
          </article>
          <p><i className="fa fa-file-o"></i>result.json</p>
        </section>
        <pre>
          <code dangerouslySetInnerHTML={{__html: Utils.prettyPrintJson(result) }}></code>
        </pre>
      </div>
    )
  }

  renderAbout() {
    return (
      <div className={styles.about}>
        <p className={styles.header_playground}>{i18n.t('playground.aboutTitle')}</p>
        <div className={styles.about_content}>
          <p>Select an aggregation, fill in the required attributes for the aggregation and preview the result
            of invoking one of the endpoints. For all endpoints see <Link
              to="/about">{i18n.t('navigation.about')}</Link>.
          </p>
        </div>
      </div>
    )
  }

  render() {
    return (
      <div className={styles.mod_container}>
        <div className={styles.mod_left_playground}>
          {this.renderLeft()}
        </div>
        <div className={styles.mod_right_playground}>
          {this.renderRight()}
        </div>

      </div>
    );
  }
}
