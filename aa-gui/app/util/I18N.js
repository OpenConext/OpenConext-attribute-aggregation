import i18n from 'i18next';
import LngDetector from 'i18next-browser-languagedetector';

import EN from '../locale/EN';
import NL from '../locale/NL';

i18n
  .use(LngDetector)
  .init({
    lng: 'en',
    fallbackLng: 'nl',
    debug: true,
    interpolation: {
      escapeValue: false // not needed for react!!
    },
    resources: {
      en: {
        translation: EN
      },
      nl: {
        translation: NL
      }
    }
  });

export default i18n;
