import I18n from "i18n-js";
import en from "../locale/en";
import nl from "../locale/nl";

const start = () => {
    //we need to use them, otherwise the imports are deleted when organizing them
    expect(I18n).toBeDefined();
    expect(nl).toBeDefined();
    expect(en).toBeDefined();
    I18n.locale = "en";
};

test("Test suite must contain at least one test", () => {});

export default start;