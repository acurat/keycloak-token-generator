import React from 'react';
import FormRadioGroup from '../components/FormRadioGroup';
import { Loader } from 'semantic-ui-react';

interface Props {
  environment: string | number | undefined;
  client: string | number | undefined;
  setClient: (value: string | number | undefined) => void;
}

interface State {
  clients: { [index: string]: string[] };
}

class ClientsSection extends React.Component<Props, State> {
  state: State = {
    clients: {},
  };

  async componentDidMount() {
    const clientsInStore = await sessionStorage.getItem('clients');
    let clients;
    if (clientsInStore) {
      clients = JSON.parse(clientsInStore);
    } else {
      clients = await fetch('/ui/clients').then((response) => response.json());
      sessionStorage.setItem('clients', JSON.stringify(clients));
    }
    this.setState({ clients });
  }

  render() {
    const { environment, client, setClient } = this.props;
    let clientsForEnv: string[] = [];
    if (environment) {
      clientsForEnv = this.state.clients[environment];
    }

    return clientsForEnv ? (
      <FormRadioGroup
        label="Clients"
        value={client}
        setValue={setClient}
        data={clientsForEnv}
      />
    ) : (
      <Loader active inline size="mini">
        Select an environment
      </Loader>
    );
  }
}

export default ClientsSection;
