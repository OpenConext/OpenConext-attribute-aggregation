export default {
  code: 'NL',
  lang: 'nl',
  name: 'Nederlands',
  selectLocale: 'Select English',
  header: {
    title: 'Attribuut Aggregatie',
    welcome: "Welkom {{name}}",
    links: {
      'helpHtml': '<a href="https://github.com/OpenConext/OpenConext-pdp#policy-limitations" target="_blank">Help</a>'
    }
  },
  navigation: {
    aggregations: 'Aggregaties',
    authorities: 'Autoriteiten',
    playground: 'Proeftuin',
    about: 'Info',
    aggregation: '+ Nieuwe Aggregatie'
  },
  aggregations: {
    name: 'Naam',
    serviceProviders: 'Service Providers',
    attributes: 'Attributen',
    actions: '',
    edit: 'Bewerk',
    delete: 'Verwijder',
    confirmation: 'Weet je zeker dat de aggregatie {{name}} wilt verwijderen?',
    deleted: 'Aggregatie {{name}} is verwijderd',
    no_found: 'Geen aggregaties gevonden',
    srConfiguredWarning: 'niet gemarkeerd in SR met attribute_aggregation_required',
    consentIsSkipped: 'consent wordt overgeslagen',
    searchPlaceHolder: 'Zoeken...'
  },
  authority: {
    authorities: 'Attribuut Autoriteiten',
    endpoint: 'Service URL',
    timeOut: 'Timeout (ms)',
    userName: 'Naam',
    requiredInputAttributes: 'Verplichte input attributen',
    attribute: 'Output attribuut',
    name: 'Naam',
    caseExact: 'Hoofdlettergevoelige',
    description: 'Omschrijving',
    multiValued: 'Meerdere waardes',
    mutability: 'Muteerbaar',
    required: 'Verplicht',
    returned: 'Oplevering',
    type: 'Type',
    uniqueness: 'Uniek'
  },
  aggregation : {
    name: 'Naam',
    serviceProviders: 'Service Provider(s)',
    update: 'Bewerk {{name}}',
    create: 'Nieuwe aggregatie',
    created: 'Aangemaakt door {{name}} op {{date}}',
    submit: 'Verstuur',
    cancel: 'Annuleer',
    authority: 'Attribuut Autoriteit',
    attributes: 'Attributen',
    new_authority: "Voeg een niewe autoriteit toe...",
    new_attribute: "Voeg een nieuw attribuut toe...",
    cancel_question: "Weet je zeker dat je deze pagina wilt verlaten?",
    name_already_exists: 'Een aggregatie met deze naam bestaat al',
    sp_already_linked: "Service Provider '{{serviceProvider}}' is al gekoppeld een aggregatie '{{aggregation}}'",
    skip_consent: "Sla consent over",
    serviceProvidersPlaceholder: "Selecteer 1 of meer Service Providers"
  },
  playground : {
    title: 'AGGREGATIE PROEFTUIN',
    aboutTitle: 'Hoe gebruik je de Proeftuin?',
    aggregations: 'Aggregaties',
    aggregationsInfo: "De geslecteerde aggregatie bepaalt welke Service Provider voor het 'Schema' endpoint wordt gebruikt en bepaalt ook de user attributen die nodig zijn voor de 'Me' en 'EB' endpoints",
    serviceProvider: 'Service Provider',
    userAttributes: 'User attributen',
    me: 'Me',
    schema: 'Schema',
    service_provider_configuration: 'Configuration',
    resource_type: 'Resources',
    clear: 'Clear',
    result_status_ok: 'Ok',
    result_status_error: 'Error',
    engine_block: 'Attribuut Aggregatie',
    serviceProviderPlaceholder: 'Selecteer een Service Provider',
    about: 'Selecteer een aggregatie, voer de verplichte attribuut waardes op voor voor de aggregatie en bekijk de resultaten door 1 van de endpoints aan te roepen.'
  },
  error: {
    exception_occurred: 'Er is een onverwachte fout opgetreden. Meer weten we niet.',
    exception_environment: 'Geconfigureerd om error details te tonen voor omgeving(en): {{profiles}}.',
    key: 'Naam',
    value: 'Waarde'
  },
  footer: {
    surfnetHtml: '<a href="http://www.surfnet.nl/en" target="_blank">SURFnet</a>',
    termsHtml: '<a href="https://wiki.surfnet.nl/display/conextsupport/Terms+of+Service+%28EN%29" target="_blank">Terms of Service</a>',
    contactHtml: '<a href="mailto:support@surfconext.nl">support@surfconext.nl</a>'
  }
}
