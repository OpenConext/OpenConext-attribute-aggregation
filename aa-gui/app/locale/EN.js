export default {
  code: 'EN',
  lang: 'en',
  name: 'English',
  selectLocale: 'Select English',
  header: {
    title: 'Attribute Aggregation',
    welcome: "Welcome {{name}}",
    links: {
      'helpHtml': '<a href="https://github.com/OpenConext/OpenConext-pdp#policy-limitations" target="_blank">Help</a>'
    }
  },
  navigation: {
    aggregations: 'Aggregations',
    authorities: 'Authorities',
    playground: 'Playground',
    about: 'About',
    aggregation: '+ New Aggregation'
  },
  aggregations: {
    name: 'Name',
    serviceProviders: 'Service Providers',
    attributes: 'Attributes',
    actions: '',
    edit: 'Edit',
    delete: 'Delete',
    confirmation: 'Are you sure you want to delete aggregation {{name}}?',
    deleted: 'Successfully deleted aggregation {{name}}',
    no_found: 'No aggregations found'
  },
  authority: {
    authorities: 'Attribute Authorities',
    endpoint: 'Endpoint',
    timeOut: 'Timeout (ms)',
    userName: 'Username',
    requiredInputAttributes: 'Required input attributes',
    attribute: 'Attribute',
    name: 'Name',
    caseExact: 'Case insensitive',
    description: 'Description',
    multiValued: 'Multi valued',
    mutability: 'Mutability',
    required: 'Required',
    returned: 'Returned',
    type: 'Type',
    uniqueness: 'Uniqueness'
  },
  aggregation : {
    name: 'Name',
    serviceProviders: 'Service Provider(s)',
    update: 'Update {{name}}',
    create: 'Create new aggregation',
    created: 'Created by {{name}} on {{date}}',
    submit: 'Submit',
    cancel: 'Cancel',
    authority: 'Attribute Authority',
    attributes: 'Attribute(s)',
    new_authority: "Add a new authority...",
    new_attribute: "Add new attribute....",
    cancel: "Cancel",
    cancel_question: "Are you sure you want to leave this page?",
    name_already_exists: 'An aggregation with this name already exists',
    sp_already_linked: "Service Provider '{{serviceProvider}}' is already linked to aggregation '{{aggregation}}'"
  },
  playground : {
    title: 'AGGREGATION PLAYGROUND',
    aboutTitle: 'How to use the Playground?',
    aggregations: 'Aggregations',
    aggregationsInfo: "The selected aggregation will determine the Service Provider for the 'Schema' endpoint and will also determine the user attributes needed for the 'Me' and 'EB' endpoints",
    serviceProvider: 'Service Provider',
    userAttributes: 'User attributes',
    me: 'Me',
    schema: 'Schema',
    service_provider_configuration: 'Configuration',
    resource_type: 'Resources',
    clear: 'Clear',
    result_status_ok: 'Ok',
    result_status_error: 'Error',
    engine_block: 'Internal EngineBlock call'
  },
  footer: {
    surfnetHtml: '<a href="http://www.surfnet.nl/en" target="_blank">SURFnet</a>',
    termsHtml: '<a href="https://wiki.surfnet.nl/display/conextsupport/Terms+of+Service+%28EN%29" target="_blank">Terms of Service</a>',
    contactHtml: '<a href="mailto:support@surfconext.nl">support@surfconext.nl</a>'
  }
}
