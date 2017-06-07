export function stop(e) {
    if (e !== undefined && e !== null) {
        e.preventDefault();
        e.stopPropagation();
    }
}

export function isEmpty(obj) {
    if (obj === undefined || obj === null) {
        return true;
    }
    if (Array.isArray(obj)) {
        return obj.length === 0;
    }
    if (typeof obj === "string") {
        return obj.trim().length === 0;
    }
    if (typeof obj === "object") {
        return Object.keys(obj).length === 0;
    }
    return false;
}

export function prettyPrintJson(obj) {
    const replacer = (match, pIndent, pKey, pVal, pEnd) => {
        const key = "<span style=color:#051CB3;>";
        const val = "<span style=color:#8BC34A;>";
        const str = "<span style=color:#005600;>";
        let r = pIndent || "";
        if (pKey)
            r = r + key + pKey.replace(/[": ]/g, "") + "</span>: ";
        if (pVal)
            r = r + (pVal[0] === "" ? str : val) + pVal + "</span>";
        return r + (pEnd || "");
    };

    const jsonLine = /^( *)("[\w]+": )?("[^"]*"|[\w.+-]*)?([,[{])?$/mg;
    return JSON.stringify(obj, null, 3)
        .replace(/&/g, "&amp;").replace(/\\"/g, "&quot;")
        .replace(/</g, "&lt;").replace(/>/g, "&gt;")
        .replace(jsonLine, replacer);
}
