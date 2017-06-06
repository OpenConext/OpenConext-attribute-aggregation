import React from "react";
import I18n from "i18n-js";
import start from "../base";
start();

expect.extend({
    toContainKey(translation, key) {
        const pass = (translation[key] !== undefined);
        return {
            message: () => `Expected ${key} to be present in ${JSON.stringify(translation)}`,
            pass: pass
        };
    },
});

test("All translations exists in EN and NL", () => {
    const contains = (translation, translationToVerify) => {
        Object.keys(translation).forEach(key => {
            expect(translationToVerify).toContainKey(key);
            const value = translation[key];
            if (typeof value === "object") {
                contains(value, translationToVerify[key])
            }
        });
    };
    contains(I18n.translations.en, I18n.translations.nl);
    contains(I18n.translations.nl, I18n.translations.en);

});