import React from 'react';
import { Header, Segment } from 'semantic-ui-react';
import jwtDecode from 'jwt-decode';
import Timer from '../components/Timer';
import TokenSegment from './TokenSegment';

interface Props {
  token: string;
  header: string;
}

const Token: React.FC<Props> = ({ token, header }: Props) => {
  const jwt: { exp: number } = jwtDecode(token);
  return (
    <>
      <Header as="h3">{header}</Header>
      <Timer value={jwt.exp} />
      <Segment.Group horizontal>
        <TokenSegment content={token} isJSON={false} />
        <TokenSegment content={jwt} isJSON={true} />
      </Segment.Group>
    </>
  );
};

export default Token;
