import styles from './_Aggregations.scss';

import React from 'react';
import ReactTooltip from 'react-tooltip';
import i18n from 'i18next';
import API from '../../util/API';
import Flash from '../../components/Flash/Flash';

import Utils from '../../util/Utils';

export default class Aggregations extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {
      aggregations: [],
      filteredAggregations: [],
      sorted: {name: 'Name', order: 'down'}
    };

    API.getAggregations((json) => {
      var aggregations = json.sort((a, b) => a.name.localeCompare(b.name));
      this.setState({aggregations: aggregations, filteredAggregations: aggregations});
    });
  }

  handleShowAggregation = (aggregation) => (e) => {
    Utils.stop(e);
    this.props.history.replace('/aggregation/' + aggregation.id);
  };

  handleDeleteAggregation = (aggregation) => (e) => {
    stop(e);
    if (confirm(i18n.t("aggregations.confirmation", {name: aggregation.name}))) {
      API.deleteAggregation(aggregation.id, () =>
        API.getAggregations((json) => {
          this.setState({aggregations: json, flash: i18n.t('aggregations.deleted', {name: aggregation.name})});
        })
      )
    }
  };

  renderServiceProviders = (aggregation) => <div className={styles.attributes}>{aggregation.serviceProviders.map((sp) =>
    <p key={sp.name}>{sp.name !== undefined && sp.name !== null ? sp.name : sp.entityId}</p>)}</div>;

  renderAttributes = (aggregation) => <div className={styles.attributes}>{aggregation.attributes.map((attr) =>
    <p key={attr.name}>{attr.attributeAuthorityId} <i className="fa fa-arrow-right"></i> {attr.name}</p>)}</div>;

  renderActions = (aggregation) => (<div>
    <a href="#" onClick={this.handleShowAggregation(aggregation)}
       data-tip={i18n.t("aggregations.edit")}><ReactTooltip /> <i className="fa fa-edit"></i>
    </a>
    <a href="#" data-tip={i18n.t("aggregations.delete")} onClick={this.handleDeleteAggregation(aggregation)}>
      <ReactTooltip /> <i className="fa fa-remove"></i>
    </a>
  </div>);

  search = (e) => {
    let input = e.target.value;
    if (input === undefined || input === null || input.trim().length === 0) {
      this.setState({filteredAggregations: this.state.aggregations});
    } else {
      this.setState({filteredAggregations: this.filterAggregations(input.toLowerCase())});
    }
  };

  filterAggregations(input) {
    var ts = this.state.aggregations.filter((aggregation) =>
      aggregation.name.toLowerCase().includes(input)
      || aggregation.serviceProviders.filter((sp) => sp.name ? sp.name.toLowerCase().includes(input) : sp.entityId.toLowerCase().includes(input)).length > 0
      || aggregation.attributes.filter((attr) => attr.name.toLowerCase().includes(input) || attr.attributeAuthorityId.toLowerCase().includes(input)).length > 0
    );
    return ts
  }

  sort = (column, aggregations) => (e) => {
    Utils.stop(e);
    if (column.sortFunction === undefined) {
      return
    }
    var sortedAggregations = aggregations.sort(column.sortFunction);
    var newOrder = 'down';
    if (this.state.sorted.name === column.sort) {
      newOrder = this.state.sorted.order === 'down' ? 'up' : 'down';
      if (newOrder === 'up') {
        sortedAggregations = sortedAggregations.reverse();
      }
    }
    this.setState({filteredAggregations: sortedAggregations, sorted: {name: column.sort, order: newOrder}})
  };

  sortByName = (a, b) =>  a.name.localeCompare(b.name);
  sortByServiceProviders = (a, b) => this.serviceProviderName(a.serviceProviders[0]).localeCompare(this.serviceProviderName(b.serviceProviders[0]));
  sortByAttributes = (a, b) => {
    var aA = a.attributes[0];
    var bA = b.attributes[0];
    return aA.attributeAuthorityId !== bA.attributeAuthorityId ?
      aA.attributeAuthorityId.localeCompare(bA.attributeAuthorityId) : aA.name.localeCompare(bA.name)
  };
  serviceProviderName = (sp) => sp.name ? sp.name : sp.entityId;

  iconClassName(column) {
    var sorted = this.state.sorted;
    return sorted.name === column.sort ? 'fa fa-arrow-' + sorted.order + ' ' + styles.sorted : styles.to_sort;
  }

  renderEmptyAggregations() {
    return this.state.filteredAggregations.length === 0 ?
      <tr>
        <td><em className={styles.no_data}>No aggregations</em></td>
        <td></td>
      </tr> : <tr></tr>
  }

  render() {
    let columns = [
      {title: i18n.t('aggregations.name'), sort: 'Name', sortFunction: this.sortByName},
      {title: i18n.t('aggregations.serviceProviders'), sort: 'ServiceProviders', sortFunction: this.sortByServiceProviders },
      {title: i18n.t('aggregations.attributes'), sort: 'Attributes', sortFunction: this.sortByAttributes},
      {title: i18n.t('aggregations.actions')}
    ];
    return (
      <div>
        <Flash message={this.state.flash}/>
        <div className={styles.mod_container}>
          <input className={styles.input} placeholder=" Search..." type="text" onChange={this.search}/>
          <div className={styles.mod_center}>
            <table className={styles.table}>
              <thead>
              <tr>
                {columns.map((column) =>
                  <th key={column.title} onClick={this.sort(column, this.state.filteredAggregations)}>
                    {column.title}<i className={this.iconClassName(column)}></i>
                  </th>)}
              </tr>
              </thead>
              <tbody>
              {this.state.filteredAggregations.map((aggregation) =>
                <tr key={aggregation.name}>
                  <td>{aggregation.name}</td>
                  <td>{this.renderServiceProviders(aggregation)}</td>
                  <td>{this.renderAttributes(aggregation)}</td>
                  <td>{this.renderActions(aggregation)}</td>
                </tr>
              )}
              {this.renderEmptyAggregations()}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  }
}

