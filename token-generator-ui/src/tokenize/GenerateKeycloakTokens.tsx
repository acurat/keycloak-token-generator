import React, { ChangeEvent, Dispatch } from 'react';
import { Button, Form } from 'semantic-ui-react';
import ClientsSection from './ClientsSection';
import EnvironmentsSection from './EnvironmentsSection';
import { Tokens } from '../utils/types';
import ScopesSection from "./ScopesSection";

interface Props {
  setTokens: Dispatch<Tokens | undefined>;
  setLoading: Dispatch<boolean>;
}

interface State {
  environment: string | number | undefined;
  client: string | number | undefined;
  username: string;
  error: string | undefined;
  scopes: string[];
}

class GenerateKeycloakTokens extends React.Component<Props, State> {
  state = {
    environment: undefined,
    client: undefined,
    username: '',
    error: undefined,
    scopes: ['openid']
  };
  setEnvironment = (value: string | number | undefined) => {
    this.setState({
      environment: value,
    });
  };

  setClient = (value: string | number | undefined) => {
    this.setState({
      client: value,
    });
  };

  setUsername = (event: ChangeEvent<HTMLInputElement>) => {
    this.setState({
      username: event.target.value,
      error: undefined,
    });
  };

  setScopes = (scopes: string[]) => {
    this.setState({
      scopes,
    });
  };

  resetState = () => {
    this.setState({
      error: undefined,
    })
  };

  submit = async () => {
    this.resetState();
    const { environment, client, username, scopes } = this.state;
    const scopesString = scopes.join(" ");
    const { setTokens, setLoading } = this.props;
    try {
      setLoading(true);
      const response = await fetch(
        `/token?environment=${environment}&clientId=${client}&username=${username}&scope=${scopesString}`,
      ).then((response) => response.json());
      setTokens({
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
        idToken: response.idToken,
      });
    } catch (e) {
      console.error(e);
      setTokens(undefined);
      this.setState({
        error: 'Error encountered',
      });
    } finally {
      setLoading(false);
    }
  };

  render() {
    const { environment, client, username, error, scopes } = this.state;
    const valid = environment && client && username;
    return (
      <Form onSubmit={this.submit}>
        <Form.Field>
          <EnvironmentsSection
            environment={environment}
            setEnvironment={this.setEnvironment}
          />
        </Form.Field>
        <Form.Field>
          <ClientsSection
            environment={environment}
            client={client}
            setClient={this.setClient}
          />
        </Form.Field>
        <Form.Field>
          <label>Username</label>
          <Form.Input
            placeholder="T-id or driver-id or agent-id. Ex: m18220"
            onChange={this.setUsername}
            value={username}
            error={
              error && { content: 'Check the username', pointing: 'above' }
            }
          />
        </Form.Field>
        <Form.Field>
          <ScopesSection
              environment={environment}
              scopes={scopes}
              setScopes={this.setScopes}
          />
        </Form.Field>
        <Button type="submit" active={valid} disabled={!valid}>
          Submit
        </Button>
      </Form>
    );
  }
}

export default GenerateKeycloakTokens;
