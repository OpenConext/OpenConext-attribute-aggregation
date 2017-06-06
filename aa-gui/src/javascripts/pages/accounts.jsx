import React from "react";
import PropTypes from "prop-types";
import ConfirmationDialog from "../components/confirmation_dialog";
import {accounts} from "../api";

export default class Accounts extends React.PureComponent {

    constructor(props) {
        super(props);
        this.state = {
            accounts: [],
            confirmationDialogOpen: false,
            confirmationDialogQuestion: "",
            confirmationDialogAction: () => false
        };
    }

    componentWillMount = () => accounts().then(accounts => this.setState({accounts: accounts}));

    confirmation = (question, action) => this.setState({
        confirmationDialogOpen: true,
        confirmationDialogQuestion: question,
        confirmationDialogAction: () => {
            this.cancelConfirmation();
            action();
        }
    });

    cancelConfirmation = () => this.setState({confirmationDialogOpen: false});

    renderAccount = (index, account) =>  <p key={index}>{account.name}</p>

    render() {
        const {accounts, confirmationDialogOpen, confirmationDialogAction, confirmationDialogQuestion} = this.state;
        return (
            <div className="accounts">
                <ConfirmationDialog isOpen={confirmationDialogOpen}
                                    cancel={this.cancelConfirmation}
                                    confirm={confirmationDialogAction}
                                    question={confirmationDialogQuestion}/>
                <div className="card">
                    {accounts.map((account, index) => this.renderAccount(index, account))}
                </div>
            </div>
        );
    }
}

Accounts.propTypes = {
    history: PropTypes.object.isRequired,
};

