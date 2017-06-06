import React from "react";
import I18n from "i18n-js";

export default function Footer() {
    return (
        <div className="footer">
            <div className="footer-inner">
            <span dangerouslySetInnerHTML={{__html: I18n.t("footer.surfnet_html")}}></span>
            <span dangerouslySetInnerHTML={{__html: I18n.t("footer.terms_html")}}></span>
            <span dangerouslySetInnerHTML={{__html: I18n.t("footer.contact_html")}}></span>
            </div>
        </div>
    );
}
