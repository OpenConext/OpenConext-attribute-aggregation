import React from "react";
import I18n from "i18n-js";
import PropTypes from "prop-types";
import {unmountComponentAtNode} from "react-dom";
import {Link} from "react-router-dom";
import logo from "../../images/logo@2x.png";
import LanguageSelector from "./language_selector";
import UserProfile from "./user_profile";
import {logOut} from "../api";

export default class Header extends React.PureComponent {

    constructor() {
        super();
        this.state = {
            dropDownActive: false
        };
    }

    renderProfileLink(currentUser) {
        return (
            <a className="welcome-link" onClick={this.handleToggle.bind(this)}>
                <i className="fa fa-user-circle-o"></i>
                {currentUser.username}
                {this.renderDropDownIndicator()}
            </a>
        );
    }

    renderDropDownIndicator() {
        return this.state.dropDownActive ? <i className="fa fa-caret-up"/> : <i className="fa fa-caret-down"/>;
    }

    renderDropDown(currentUser) {
        return this.state.dropDownActive ? <UserProfile currentUser={currentUser}/> : null;
    }


    renderExitLogout = () =>
        <li className="border-left"><a onClick={this.stop}>{I18n.t("header.links.logout")}</a>
        </li>;

    stop = e => {
        e.preventDefault();
        const node = document.getElementById("app");
        unmountComponentAtNode(node);
        logOut();
        window.location.href = "/Shibboleth.sso/Logout";
    };

    handleToggle(e) {
        e.preventDefault();
        e.stopPropagation();
        this.setState({dropDownActive: !this.state.dropDownActive});
    }

    render() {
        const currentUser = this.props.currentUser;
        return (
            <div className="header-container">
                <div className="header">
                    <Link to="/" className="logo"><img src={logo}/></Link>
                    <ul className="links">
                        <li className="title"><span>Teams</span></li>
                        <li className="profile"
                            tabIndex="1" onBlur={() => this.setState({dropDownActive: false})}>
                            {this.renderProfileLink(currentUser)}
                            {this.renderDropDown(currentUser)}
                        </li>
                        <li dangerouslySetInnerHTML={{__html: I18n.t("header.links.help_html")}}></li>
                        {this.renderExitLogout()}
                        <li>
                            <LanguageSelector />
                        </li>
                    </ul>
                </div>
            </div>
        );
    }

}

Header.propTypes = {
    currentUser: PropTypes.object.isRequired
};
