import {replaceQueryParameter, getParameterByName} from "../../utils/query-parameters";

test("Replace query parameters", () => {
    const replaced = replaceQueryParameter("?test=bogus", "test", "value");
    expect(replaced).toBe("?test=value");
});

test("Replace query parameters preserve existing", () => {
    const replaced = replaceQueryParameter("?test=bogus&name=x", "test", "value");
    expect(replaced).toBe("?name=x&test=value");
});

test("Replace query parameters", () => {
    const replaced = replaceQueryParameter("", "test", "value");
    expect(replaced).toBe("?test=value");
});

test("Parameter by name", () => {
   expect("value", getParameterByName("name", "?name=value"))
});

test("Parameter by name not exists", () => {
    expect("value", getParameterByName("", undefined))
});