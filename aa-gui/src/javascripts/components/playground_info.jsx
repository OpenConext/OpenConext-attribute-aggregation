import React from "react";
import PropTypes from "prop-types";

export default function PlayGroundInfo({locale = "en"}) {
    return locale === "en" ?
        <div className="invitation_info">
            <p className="info">the email addresses of colleagues you would like to invite for this team.</p>
            <p className="by-email">You can invite one or more people by their email address.</p>
            <span className="by-file">To send multiple invitations, simply add more email
                addresses or upload a csv file with comma- separated email addresses.</span>
        </div> :
        <div className="invitation_info">
            <p className="info">Zoek en voeg de eamil adressen van je collega's toe die je wilt uitnodigen voor dit team.</p>
            <p className="by-email">Je kan één of meer personen uitnodigen door email adres.</p>
            <p className="by-file">Je kan meerdere uitnodigingen versturen door meerdere email adressen te selecteren
                of je kan een csv bestand met komma gescheiden emails uploaden.</p>
        </div>;

}

PlayGroundInfo.propTypes = {
    locale: PropTypes.string,
};

