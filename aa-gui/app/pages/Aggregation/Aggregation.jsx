import styles from './_Aggregation.scss'

import React from 'react'
import update from 'react-addons-update'
import i18n from 'i18next'
import _ from 'lodash'
import moment from 'moment'
import Select from 'react-select'

import API from '../../util/API'
import Utils from '../../util/Utils'

export default class Aggregation extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {
      aggregation: {serviceProviders: [], attributes: []},
      serviceProviders: [],
      authorities: [],
      errors: {}
    };
    API.getServiceProviders((json) => this.setState({serviceProviders: json}));
    API.getAuthorityConfiguration((json) => this.setState({authorities: json.authorities}));

    let id = props.params.id;
    if (id !== 'new') {
      API.getAggregation(id, (json) => this.setState({aggregation: json}));
    }
  }

  componentWillMount() {
    this._mounted = true;
  }

  componentWillUnmount() {
    this._mounted = false;
  }

  componentWillReceiveProps(nextProps) {
    if (this._mounted) {
      let id = nextProps.params.id;
      if (id === 'new') {
        this.setState({aggregation: {serviceProviders: [], attributes: []}});
      }
    }
  }

  handleSubmit = (e) => {
    Utils.stop(e);
    //todd call to API
  };

  handleCancel = (e) => {
    Utils.stop(e);
    //add confirmation popup
    this.props.history.replace('/aggregations');
  };

  updateAggregationState(newPartialState) {
    var newState = update(this.state.aggregation, {$merge: newPartialState});
    this.setState({aggregation: newState})
  }

  renderName() {
    let handleOnChange = (e) => this.updateAggregationState({name: e.target.value})

    var aggregation = this.state.aggregation;
    return (
      <div className={Utils.isEmpty(aggregation.name) ? styles.failure : styles.success}>
        <label htmlFor='name'>{i18n.t('aggregation.name')}</label>
        <input className={styles.input} type="text" name="name" value={aggregation.name}
               onChange={handleOnChange}/>
      </div>
    );
  }

  renderServiceProviders() {
    let handleOnChange = (val) => this.updateAggregationState({serviceProviders: val || []})

    let aggregation = this.state.aggregation;
    return (
      <div className={Utils.isEmpty(aggregation.serviceProviders) ? styles.failure : styles.success}>
        <label htmlFor='serviceProviders'>{i18n.t('aggregation.serviceProviders')}</label>
        <Select
          name='serviceProviders'
          labelKey='name'
          valueKey='entityId'
          value={this.state.aggregation.serviceProviders.map((sp) => sp.entityId).join(',')}
          delimiter=','
          options={this.state.serviceProviders}
          onChange={handleOnChange}
          multi={true}
          placeholder='Select one or more ServiceProviders'
        />
      </div>
    );
  }

  handleRemoveAuthority = (authorityId) => (e) => {
    Utils.stop(e);
    let newAttributes = this.state.aggregation.attributes.filter((attribute) => attribute.attributeAuthorityId !== authorityId);
    this.updateAggregationState({attributes: newAttributes})
  };

  handleRemoveAttribute = (authorityId, attributeName) => (e) => {
    Utils.stop(e);
    let newAttributes = this.state.aggregation.attributes.filter((attribute) => attribute.attributeAuthorityId !== authorityId || attribute.name !== attributeName);
    this.updateAggregationState({attributes: newAttributes})
  };

  handleAuthorityChange = (e) => {
    let authorityId = e.target.value;
    let authority = this.state.authorities.filter((authority) => authority.id === authorityId)[0];
    let newAttributes = authority.attributes.map((attribute) => Object.assign({}, attribute));

    let mergedAttributes = update(this.state.aggregation.attributes, {$push: newAttributes});
    this.updateAggregationState({attributes: mergedAttributes})
  };

  renderAuthoritySelect(authorityOptions) {
    return Utils.isEmpty(authorityOptions) ? <div></div> :
      <div>
        <label htmlFor='attributes'>{i18n.t('aggregation.authority')}</label>
        <select className={styles.select} value="" onChange={this.handleAuthorityChange}>
          <option value="" disabled="disabled">{i18n.t("aggregation.new_authority")}</option>
          {
            authorityOptions.map((authority) => <option value={authority.id}
                                                        key={authority.id}>{authority.id}</option>)
          }
        </select>
      </div>
  }

  renderAttributes() {
    let attributes = this.state.aggregation.attributes;

    //we display the current authorities / attributes
    let attributesGroupedByAuthority = _.groupBy(attributes, 'attributeAuthorityId');
    let currentAuthorities = Object.keys(attributesGroupedByAuthority)

    //we need the authorities / attributes not yet linked to this aggregation
    let authorityOptions = this.state.authorities.filter((authority) => !currentAuthorities.includes(authority.id))

    return (
      <div className={Utils.isEmpty(attributes) ? styles.failure : styles.success}>
        {currentAuthorities.map((authorityId) => {
          return (
            <div key={authorityId} className={styles.authority}>
              <label htmlFor={authorityId}>{i18n.t('aggregation.authority')}</label>
              <input className={styles.input_disabled} type="text" name={authorityId} value={authorityId}
                     disabled="disabled"/>
              <a href="#" onClick={this.handleRemoveAuthority(authorityId)} className={styles.remove_authority}>
                <i className="fa fa-remove"></i>
              </a>
              <div className={styles.attributes}>
                <label>{i18n.t('aggregation.attributes')}</label>
                {attributesGroupedByAuthority[authorityId].map((attribute)=> {
                  return (
                    <div key={authorityId + '-' + attribute.name} className={styles.attribute}>
                      <input className={styles.input_disabled} type="text" name={authorityId + '-' + attribute.name}
                             value={attribute.name}
                             disabled="disabled"/>
                      <a href="#" onClick={this.handleRemoveAttribute(authorityId, attribute.name)}
                         className={styles.remove_attribute}>
                        <i className="fa fa-remove"></i>
                      </a>
                    </div>
                  )
                })}
              </div>
            </div>
          )
        })}
        {this.renderAuthoritySelect(authorityOptions)}
      </div>
    );
  }

  validAggregation() {
    var aggregation = this.state.aggregation;
    return !Utils.isEmpty(aggregation.name) && !Utils.isEmpty(aggregation.attributes) && !Utils.isEmpty(aggregation.serviceProviders)
  }

  renderActions() {
    let submitStyle = this.validAggregation() ? styles.button_submit : styles.button_submit_disabled;
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

