// Interpolation works as follows:
//
// Make a key with the translation and enclose the variable with {{}}
// ie "Hello {{name}}" Do not add any spaces around the variable name.
// Provide the values as: I18n.t("key", {name: "John Doe"})
import I18n from "i18n-js";

I18n.translations.nl = {
    code: "NL",
    name: "Nederlands",
    select_locale: "Selecteer Nederlands",

    boolean: {
        yes: "Ja",
        no: "Nee"
    },

    date: {
        month_names: ["Januari", "February", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "October", "November", "December"]
    },

    header: {
        title: "Accounts linking",
        links: {
            help_html: "<a href=\"https://openconext.org/teams\" target=\"_blank\">Help</a>",
            logout: "Logout",
            exit: "Exit"
        },
        role: "Role"
    },

    navigation: {
        accounts: "Accounts",
        playground: "Speeltuin",
        authorities: "Autoriteiten"
    },
    error_dialog: {
        title: "Onverwachte fout",
        body: "Dit is gênant. Er is een onverwachte fout opgetreden. De fout is gerapporteerd.",
        ok: "Sluiten"
    },

    confirmation_dialog: {
        title: "Bevestig a.u.b.",
        confirm: "Bevestig",
        cancel: "Annuleer",
        leavePage: "Wil je deze pagina verlaten?",
        leavePageSub: "Changes that you made will no be saved.",
        stay: "Blijf",
        leave: "Weg"
    },

    profile: {
        name: "Naam"
    },

    authority: {
        authorities: "Attribuut Autoriteiten",
        endpoint: "Service URL",
        timeOut: "Timeout (ms)",
        validationRegExp: "Formaat restrictie (e.g. reguliere expressie)",
        userName: "Naam",
        requiredInputAttributes: "Verplichte input attributen",
        attribute: "Output attribuut",
        name: "Naam",
        description: "Omschrijving",
        type: "Type",
        example: "Voorbeeld",
        pathParams: "Path parameters",
        requestParams: "Request parameters",
        mappings: "Mappings"
    },

    playground: {
        arp: "Attribute Release Policy",
        user_attributes: "User Attributes",
        attribute_name: "Attribute name",
        enabled: "Enabled",
        matching_rule: "Matching Rule",
        source: "Source",
        result_status_ok: "Ok",
        do_attribute_aggregation: "Attribute Aggregation",
        clear: "Clear"
    },

    not_found: {
        title: "Deze pagina kan niet worden gevonden.",
        description_html: "Probeer het later opnieuw of neem contact op met <a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>."
    },

    server_error: {
        title: "De AA applicatie is momenteel niet beschikbaar.",
        description_html: "Probeer het later opnieuw of neem contact op met <a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>."
    },

    logout: {
        title: "Logout succesvol.",
        description_html: "Je <strong>Moet</strong> je browser sluiten om het logout proces te voltooien."
    },

    footer: {
        surfnet_html: "<a href=\"https://www.surfnet.nl/nl\" target=\"_blank\">SURFnet</a>",
        terms_html: "<a href=\"https://wiki.surfnet.nl/display/conextsupport/Terms+of+Service+%28EN%29\" target=\"_blank\">Terms of Service</a>",
        contact_html: "<a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>"
    }

};

export default I18n.translations.nl;