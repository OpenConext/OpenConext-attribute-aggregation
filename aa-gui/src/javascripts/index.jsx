import "../favicon.ico";
import "../stylesheets/application.scss";
import {polyfill} from "es6-promise";
import "isomorphic-fetch";
import "lodash";
import moment from "moment";

import React from "react";
import {render} from "react-dom";
import {BrowserRouter as Router, Redirect, Route, Switch} from "react-router-dom";
import I18n from "i18n-js";
import Cookies from "js-cookie";
import {getUser, reportError} from "./api";
import {getParameterByName} from "./utils/query-parameters";
import {isEmpty} from "./utils/utils";

import NotFound from "./pages/not_found";
import ServerError from "./pages/server_error";
import Footer from "./components/footer";
import Header from "./components/header";
import Flash from "./components/flash";
import Navigation from "./components/navigation";
import Accounts from "./pages/accounts";
import Playground from "./pages/playground";
import AuthorityConfiguration from "./pages/authority_configuration";
import ErrorDialog from "./components/error_dialog";

import "./locale/en";
import "./locale/nl";
polyfill();

const S4 = () => (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);

class App extends React.PureComponent {

    constructor(props, context) {
        super(props, context);
        this.state = {
            loading: true,
            currentUser: {person: {guest: true}},
            error: false,
            errorDialogOpen: false,
            errorDialogAction: () => {
                this.setState({errorDialogOpen: false});
            }
        };
        window.onerror = (msg, url, line, col, err) => {
            this.setState({errorDialogOpen: true});
            const info = err || {};
            const response = err.response || {};
            const error = {
                userAgent: navigator.userAgent,
                message: msg,
                url: url,
                line: line,
                col: col,
                error: info.message,
                stack: info.stack,
                targetUrl: response.url,
                status: response.status
            };
            reportError(error);
        };
    }

    handleBackendDown = () => {
        const location = window.location;
        const alreadyRetried = location.href.indexOf("guid") > -1;
        if (alreadyRetried) {
            window.location.href = `${location.protocol}//${location.hostname}${location.port ? ":" + location.port : ""}/error`;
        } else {
            //302 redirects from Shib are cached by the browser. We force a one-time reload
            const guid = (S4() + S4() + "-" + S4() + "-4" + S4().substr(0, 3) + "-" + S4() + "-" + S4() + S4() + S4()).toLowerCase();
            window.location.href = `${location.href}?guid=${guid}`;
        }
    };

    componentDidMount() {
        const location = window.location;
        if (location.href.indexOf("error") > -1) {
            this.setState({loading: false});
        } else {
            getUser()
                .catch(() => this.handleBackendDown())
                .then(currentUser => {
                    if (!currentUser || !currentUser.uid) {
                        this.handleBackendDown();
                    } else {
                        this.setState({loading: false, currentUser: currentUser});
                    }
                });
        }
    }

    render() {
        const {loading, errorDialogAction, errorDialogOpen} = this.state;

        if (loading) {
            return null; // render null when app is not ready yet
        }

        const {currentUser} = this.state;
        return (
            <Router>
                <div>
                    <div>
                        <Flash/>
                        <Header currentUser={currentUser}/>
                        <Navigation currentUser={currentUser}/>
                        <ErrorDialog isOpen={errorDialogOpen}
                                     close={errorDialogAction}/>
                    </div>
                    <Switch>
                        <Route exact path="/" render={() => <Redirect to="/accounts"/>}/>
                        <Route path="/accounts"
                               render={props => <Accounts currentUser={currentUser} {...props}/>}/>
                        <Route path="/authorities"
                               render={props => <AuthorityConfiguration {...props}/>}/>
                        <Route path="/playground"
                               render={props => <Playground {...props}/>}/>
                        <Route path="/error"
                               render={props => <ServerError {...props}/>}/>
                        <Route component={NotFound}/>
                    </Switch>
                    <Footer />
                </div>
            </Router>
        );
    }

}

(() => {
    // DetermineLanguage based on parameter, navigator and finally cookie
    let parameterByName = getParameterByName("lang", window.location.search);

    if (isEmpty(parameterByName)) {
        const lang = navigator.language.toLowerCase();
        parameterByName = lang.startsWith("en") ? "en" : lang.startsWith("nl") ? "nl" : undefined;
    }

    if (isEmpty(parameterByName)) {
        parameterByName = Cookies.get("lang");
        parameterByName = isEmpty(parameterByName) ? "en" : parameterByName;
    }

    I18n.locale = parameterByName;
    moment.locale(I18n.locale);
})();

render(<App />, document.getElementById("app"));