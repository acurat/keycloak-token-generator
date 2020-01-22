import React, { useState } from 'react';
import './App.css';
import {
  Container,
  Grid,
  Segment,
  Dimmer,
  Loader,
} from 'semantic-ui-react';
import GenerateTokens from './tokenize/GenerateTokens';
import { Tokens } from './utils/types';
import DisplayTokens from './tokenize/DisplayTokens';

const App: React.FC = () => {
  const [tokens, setTokens] = useState<Tokens | undefined>(undefined);
  const [loading, setLoading] = useState<boolean>(false);
  return (
    <Container fluid className="App">
      <h1> Keycloak Token Generator </h1>
      <Segment className="Site-content">
        {loading && (
          <Dimmer active inverted>
            <Loader size="small">Loading</Loader>
          </Dimmer>
        )}
        <Grid fluid columns={2} divided>
          <Grid.Column width={5}>
            <GenerateTokens setTokens={setTokens} setLoading={setLoading} />
          </Grid.Column>
          <Grid.Column verticalAlign="middle" width={11}>
            <DisplayTokens tokens={tokens} />
          </Grid.Column>
        </Grid>
      </Segment>
    </Container>
  );
};

export default App;
