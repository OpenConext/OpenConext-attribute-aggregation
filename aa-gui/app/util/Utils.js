class Utils {

  replacer = (match, pIndent, pKey, pVal, pEnd) => {
    var key = '<span style=color:#303F9F;>';
    var val = '<span class=color:#8BC34A;>';
    var str = '<span class=color:#CDDC39;>';
    var r = pIndent || '';
    if (pKey)
      r = r + key + pKey.replace(/[": ]/g, '') + '</span>: ';
    if (pVal)
      r = r + (pVal[0] == '"' ? str : val) + pVal + '</span>';
    return r + (pEnd || '');
  }

  stop(e) {
    if (e !== undefined && e !== null) {
      e.preventDefault();
      e.stopPropagation();
    }
  }

  isEmpty(obj) {
    if (obj === undefined || obj === null) {
      return true;
    }
    if (Array.isArray(obj)) {
      return obj.length === 0;
    }
    if (typeof obj === 'string') {
      return obj.trim().length === 0;
    }
    return false;
  }

  prettyPrintJson(obj) {
    let jsonLine = /^( *)("[\w]+": )?("[^"]*"|[\w.+-]*)?([,[{])?$/mg;
    return JSON.stringify(obj, null, 3)
      .replace(/&/g, '&amp;').replace(/\\"/g, '&quot;')
      .replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(jsonLine, this.replacer);
  }

}

export default new Utils()
