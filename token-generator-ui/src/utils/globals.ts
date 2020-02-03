import {SupportedTypes} from "./types";

const copyToClipboard = (text: string) => {
  if (
    document.queryCommandSupported &&
    document.queryCommandSupported('copy')
  ) {
    const textarea = document.createElement('textarea');
    textarea.textContent = text;
    textarea.style.position = 'fixed';
    document.body.appendChild(textarea);
    textarea.select();
    try {
      return document.execCommand('copy');
    } catch (ex) {
      console.warn('Copy to clipboard failed.', ex);
      return false;
    } finally {
      document.body.removeChild(textarea);
    }
  }
};


const getDefaultValue = (type: string) => {
  let defaultValue;
  switch (type) {
    case SupportedTypes.Number:
      defaultValue = 0;
      break;
    case SupportedTypes.Date:
      defaultValue = Date.now();
      break;
    case SupportedTypes.Object:
      defaultValue = JSON.stringify({});
      break;
    default:
      defaultValue = '';
  }
  return defaultValue;
};

const getTommorrowsDate = () => {
  const date = new Date();
  return date.setDate(date.getDate() + 1);
};

const isString = (value: any) => value && typeof value === 'string' || value instanceof String;
const isNumber = (value: any) => !isNaN(value);
const isDate = (value: any) => typeof value === 'object' || value instanceof Date;
const isJSON = (value: string) => {
  try {
    const o = JSON.parse(value);
      return true;
    }
  catch (e) { }
  return false;
};

export { copyToClipboard, getDefaultValue, isNumber, isString, isDate, isJSON, getTommorrowsDate };
