import React from 'react';
import { Message } from 'semantic-ui-react';
import { Tokens } from '../utils/types';
import Token from '../components/Token';

interface Props {
  tokens: Tokens | undefined;
}

const DisplayTokens: React.FC<Props> = ({ tokens }: Props) => (
  <div style={{ minHeight: '150px' }}>
    {tokens ? (
      <>
        {tokens.accessToken && (
          <Token token={tokens.accessToken} header="Access Token" />
        )}
        {tokens.refreshToken && (
          <Token token={tokens.refreshToken} header="Refresh Token" />
        )}
        {tokens.idToken && (
            <Token token={tokens.idToken} header="ID Token" />
        )}
      </>
    ) : (
      <Message>No tokens to display.</Message>
    )}
  </div>
);

export default DisplayTokens;
