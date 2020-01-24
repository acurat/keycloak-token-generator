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

const isString = (value: any) => typeof value === 'string' || value instanceof String;
const isNumber = (value: any) => typeof value === 'number' || value instanceof Number;
const isDate = (value: any) => typeof value === 'object' || value instanceof Date;

export { copyToClipboard, isNumber, isString, isDate };
