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

export { copyToClipboard };
