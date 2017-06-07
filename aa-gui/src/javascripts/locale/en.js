// Interpolation works as follows:
//
// Make a key with the translation and enclose the variable with {{}}
// ie "Hello {{name}}" Do not add any spaces around the variable name.
// Provide the values as: I18n.t("key", {name: "John Doe"})
import I18n from "i18n-js";

I18n.translations.en = {
    code: "EN",
    name: "English",
    select_locale: "Select English",

    boolean: {
        yes: "Yes",
        no: "No"
    },

    date: {
        month_names: ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]
    },

    header: {
        title: "Attribute Aggregation",
        links: {
            help_html: "<a href=\"https://openconext.org/\" target=\"_blank\">Help</a>",
            logout: "Logout",
            exit: "Exit"
        },
        role: "Role"
    },

    navigation: {
        accounts: "Accounts",
        playground: "Playground",
        authorities: "Authorities"
    },

    error_dialog: {
        title: "Unexpected error",
        body: "This is embarrassing. An unexpected error has occurred. It has been logged and reported.",
        ok: "Close"
    },

    confirmation_dialog: {
        title: "Please confirm",
        confirm: "Confirm",
        cancel: "Cancel",
        leavePage: "Do you want to leave this page?",
        leavePageSub: "Changes that you made will no be saved.",
        stay: "Stay",
        leave: "Leave"
    },

    profile: {
        name: "Name"
    },

    authority: {
        authorities: "Attribute Authorities",
        endpoint: "Endpoint",
        timeOut: "Timeout (ms)",
        userName: "Username",
        requiredInputAttributes: "Required input attributes",
        attribute: "Output attribute",
        name: "Name",
        description: "Description",
        type: "Type",
        example: "Example"
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
        title: "The requested page could not be found.",
        description_html: "Please try again later or contact <a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>."
    },

    server_error: {
        title: "The AA application is currently unavailable.",
        description_html: "Please try again later or contact <a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>."
    },

    logout: {
        title: "Logout completed successfully.",
        description_html: "You <strong>MUST</strong> close your browser to complete the logout process."
    },

    footer: {
        surfnet_html: "<a href=\"https://www.surfnet.nl/en\" target=\"_blank\">SURFnet</a>",
        terms_html: "<a href=\"https://wiki.surfnet.nl/display/conextsupport/Terms+of+Service+%28EN%29\" target=\"_blank\">Terms of Service</a>",
        contact_html: "<a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>"
    }

};

export default I18n.translations.en;
