import React from "react";

import I18n from "i18n-js";

import {authorityConfiguration} from "../api";
import {isEmpty, stop} from "../utils/utils";

const attributeKeys = ["description", "type", "example"];

export default class AuthorityConfiguration extends React.PureComponent {

    constructor(props, context) {
        super(props, context);
        const authorities = [{requiredInputAttributes: [], attributes: []}];
        this.state = {
            authorities: authorities,
            selectedAuthority: authorities[0]
        };
    }

    componentWillMount = () => authorityConfiguration().then(configuration => this.setState(
        {
            authorities: configuration.authorities.sort((a1, a2) => a1.id.localeCompare(a2.id)),
            selectedAuthority: configuration.authorities[0]
        }
    ));

    handleShowAuthority = authority => e => {
        stop(e);
        this.setState({selectedAuthority: authority});
    };

    renderAuthorityLink(authority) {
        const currentAuthority = this.state.selectedAuthority && this.state.selectedAuthority.id === authority.id;
        const style = currentAuthority ? "authority_selected" : "authority_link";
        return currentAuthority ?
            <p key={authority.id + "p"} className={style}>{authority.id}</p> :
            <a key={authority.id + "a"} href="#" className={style}
               onClick={this.handleShowAuthority(authority)}>{authority.id}</a>;
    }

    renderAuthority() {
        const authority = this.state.selectedAuthority;
        return (
            <div className="authority_details">
                <section className="header">{authority.id}</section>
                <span>{I18n.t("authority.description")}</span>
                <p>{authority.description}</p>
                <span>{I18n.t("authority.endpoint")}</span>
                <p>{authority.endpoint}</p>
                <span>{I18n.t("authority.timeOut")}</span>
                <p>{authority.timeOut}</p>
                <span>{I18n.t("authority.validationRegExp")}</span>
                <p>{authority.validationRegExp}</p>
                <span>{I18n.t("authority.requiredInputAttributes")}</span>
                <ul>{authority.requiredInputAttributes.map(attr =>
                    <li key={authority.id + "-" + attr.name}>{attr.name}</li>
                )}
                </ul>
                {!isEmpty(authority.pathParams) &&
                    <div>
                        <span>{I18n.t("authority.pathParams")}</span>
                        <ul>
                            {authority.pathParams.map((param, index) =>
                                <li key={index}>{param.sourceAttribute}</li>
                            )}
                        </ul>
                    </div>}
                {!isEmpty(authority.requestParams) &&
                    <div>
                        <span>{I18n.t("authority.requestParams")}</span>
                        <ul>
                            {authority.requestParams.map((param, index) =>
                                <li key={index}>{param.name}</li>
                            )}
                        </ul>
                    </div>}
                {!isEmpty(authority.mappings) &&
                    <div>
                        <span>{I18n.t("authority.mappings")}</span>
                        <ul>
                            {authority.mappings.map((mapping, index) =>
                                <li key={index}>{mapping.responseKey + " => " + mapping.targetAttribute}</li>
                            )}
                        </ul>
                    </div>}
                {this.renderAttributes(authority.attributes)}
                <span/>
            </div>
        );
    }

    renderAttributes = attributes =>
        <div>
            {attributes.map(this.renderAttribute)}
        </div>;

    renderAttribute(attribute, index) {
        const valueToString = val => val !== undefined && val !== null ? val.toString() : "";
        return (
            <div key={attribute.attributeAuthorityId + "_" + attribute.name}>
                <section
                    className="attribute">{I18n.t("authority.attribute") + " #" + (index + 1)}<em>{attribute.name}</em>
                </section>
                {attributeKeys.map(key =>
                    <div key={attribute.name + "-" + key} className="attributeDetails">
                        <span>{I18n.t("authority." + key)}</span>
                        <p>{valueToString(attribute[key])}</p>
                    </div>)
                }
            </div>
        );
    }


    render() {
        return (
            <div className="authorities">
                <div className="left">
                    <section className="header">{I18n.t("authority.authorities")}</section>
                    {this.state.authorities.map(authority => this.renderAuthorityLink(authority))}
                </div>
                <div className="right">
                    {this.renderAuthority()}
                </div>

            </div>
        );
    }
}
