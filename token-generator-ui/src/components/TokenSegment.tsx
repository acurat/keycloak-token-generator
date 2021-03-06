import React, {useEffect, useState} from 'react';
import { Icon, Label, Segment } from 'semantic-ui-react';
import JSONPretty from 'react-json-pretty';
import { copyToClipboard } from '../utils/globals';

interface Props {
  content: any;
  isJSON: boolean;
  fullScreen?: boolean;

}

const TokenSegment: React.FC<Props> = ({ content, isJSON, fullScreen }: Props) => {
  // use State for copy text
  const [copyText, setCopyText] = useState('Copy');

  // use Effect to reset copy text after 3 seconds
  useEffect(() => {
    const timeOut = setInterval(() => {
      setCopyText('Copy');
    }, 3000);
    return () => {
      clearInterval(timeOut);
    };
  }, [copyText]);

  const onClickCallback = () => {
    const success = copyToClipboard(isJSON ? JSON.stringify(content) : content);
    if (success) {
      setCopyText('Copied');
    }
  };

  return (
    <Segment className={`Segment ${fullScreen ? 'Segment-full' : ''}`}>
      <Label as="a" attached={'top right'} onClick={onClickCallback}>
        {copyText}&nbsp; <Icon name="copy" />
      </Label>
      {isJSON ? (
        <JSONPretty id="json-pretty" json={content}></JSONPretty>
      ) : (
        <div>{content}</div>
      )}
    </Segment>
  );
};

export default TokenSegment;
