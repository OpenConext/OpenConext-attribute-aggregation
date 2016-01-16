import i18n from 'i18next';
import LngDetector from 'i18next-browser-languagedetector';

import EN from '../locale/EN';
import NL from '../locale/NL';

let lang = localStorage.getItem('i18nextLng') || 'en';

i18n
  .use(LngDetector)
  .init({
    lng: lang,
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
