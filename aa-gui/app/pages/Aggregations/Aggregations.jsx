import styles from './_Aggregations.scss';

import React from 'react';
import ReactDom from 'react-dom';
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

    this.fetchAggregations();
  }

  fetchAggregations() {
    API.getAggregations((json) => {
      var aggregations = json.sort((a, b) => a.name.localeCompare(b.name));
      this.setState({aggregations: aggregations, filteredAggregations: aggregations});
    });
  }

  componentDidUpdate = () => document.body.scrollTop = document.documentElement.scrollTop = 0;

  handleShowAggregation = (aggregation) => (e) => {
    Utils.stop(e);
    this.props.history.replace('/aggregation/' + aggregation.id);
  };

  handleDeleteAggregation = (aggregation) => (e) => {
    Utils.stop(e);
    if (confirm(i18n.t("aggregations.confirmation", {name: aggregation.name}))) {
      API.deleteAggregation(aggregation.id, () => this.fetchAggregations())
    }
  };

  renderServiceProviders = (aggregation) => <div className={styles.attributes}>{aggregation.serviceProviders.map((sp) =>
    <p key={sp.entityId}>{sp.name !== undefined && sp.name !== null ? sp.name : sp.entityId}</p>)}</div>;

  renderAttributes = (aggregation) => <div className={styles.attributes}>{aggregation.attributes.map((attr) =>
    <p key={attr.attributeAuthorityId + '-' + attr.name}>{attr.name} <em>from</em> {attr.attributeAuthorityId}
    </p>)}</div>;

  renderActions = (aggregation) => (<div className={styles.actions}>
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
    return this.state.aggregations.filter((aggregation) =>
      aggregation.name.toLowerCase().includes(input)
      || aggregation.serviceProviders.filter((sp) => sp.name ? sp.name.toLowerCase().includes(input) : sp.entityId.toLowerCase().includes(input)).length > 0
      || aggregation.attributes.filter((attr) => attr.name.toLowerCase().includes(input) || attr.attributeAuthorityId.toLowerCase().includes(input)).length > 0
    )
  }

  sort = (column, aggregations) => (e) => {
    Utils.stop(e);
    if (column.sortFunction === undefined) {
      return
    }
    let sortedAggregations = aggregations.sort(column.sortFunction);
    let newOrder = 'down';
    if (this.state.sorted.name === column.sort) {
      newOrder = this.state.sorted.order === 'down' ? 'up' : 'down';
      if (newOrder === 'up') {
        sortedAggregations = sortedAggregations.reverse();
        sortedAggregations.forEach((agg) => agg.attributes.reverse());
        sortedAggregations.forEach((agg) => agg.serviceProviders.reverse());
      }
    }
    this.setState({filteredAggregations: sortedAggregations, sorted: {name: column.sort, order: newOrder}})
  };

  sortByName = (a, b) =>  a.name.localeCompare(b.name);
  sortByServiceProviders = (a, b) => {
    let aSP = a.serviceProviders.sort((sp1, sp2) => this.serviceProviderName(sp1).localeCompare(this.serviceProviderName(sp2)))[0]
    let bSP = b.serviceProviders.sort((sp1, sp2) => this.serviceProviderName(sp1).localeCompare(this.serviceProviderName(sp2)))[0]
    this.serviceProviderName(aSP).localeCompare(this.serviceProviderName(bSP))
  }
  sortByAttributes = (a, b) => {
    let aA = a.attributes.sort((a1, a2)=> a1.name.localeCompare(a2.name))[0];
    let bA = b.attributes.sort((b1, b2)=> b1.name.localeCompare(b2.name))[0];
    return aA.name !== bA.name ?
      aA.name.localeCompare(bA.name) : aA.attributeAuthorityId.localeCompare(bA.attributeAuthorityId)
  };
  serviceProviderName = (sp) => sp.name ? sp.name : sp.entityId;

  iconClassName(column) {
    var sorted = this.state.sorted;
    return sorted.name === column.sort ? 'fa fa-arrow-' + sorted.order + ' ' + styles.sorted : styles.to_sort;
  }

  renderAggregationsTable() {
    let columns = [
      {title: i18n.t('aggregations.name'), sort: 'Name', sortFunction: this.sortByName},
      {
        title: i18n.t('aggregations.serviceProviders'),
        sort: 'ServiceProviders',
        sortFunction: this.sortByServiceProviders
      },
      {title: i18n.t('aggregations.attributes'), sort: 'Attributes', sortFunction: this.sortByAttributes},
      {title: i18n.t('aggregations.actions')}
    ];
    if (this.state.filteredAggregations.length !== 0) {
      return (<table className={styles.table}>
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

        </tbody>
      </table>)
    } else {
      return <div><em className={styles.no_data}>{i18n.t('aggregations.no_found')}</em></div>
    }
  }

  render() {
    return (
      <div>
        <Flash message={this.state.flash}/>
        <div className={styles.mod_container}>
          <div className={styles.search_container}>
            <i className="fa fa-search"></i>
            <input placeholder="Search..." type="text" onChange={this.search}/>
          </div>
          {this.renderAggregationsTable()}
        </div>
      </div>
    );
  }
}

