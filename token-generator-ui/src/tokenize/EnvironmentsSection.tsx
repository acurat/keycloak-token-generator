import React from 'react';
import FormRadioGroup from '../components/FormRadioGroup';

interface Props {
  environment: string | number | undefined;
  setEnvironment: (value: string | number | undefined) => void;
}

interface State {
  environments: string[];
}

class EnvironmentsSection extends React.Component<Props, State> {
  state: State = {
    environments: [],
  };

  async componentDidMount() {
    const envs = await sessionStorage.getItem('environments');
    let environments;
    if (envs) {
      environments = JSON.parse(envs);
    } else {
      environments = await fetch('/ui/environments').then((response) =>
        response.json(),
      );
      sessionStorage.setItem('environments', JSON.stringify(environments));
    }
    this.setState({ environments });
    this.props.setEnvironment(environments[0]);
  }

  render() {
    const { environment, setEnvironment } = this.props;
    const { environments } = this.state;

    return (
      <FormRadioGroup
        label="Environments"
        value={environment}
        setValue={setEnvironment}
        data={environments}
      />
    );
  }
}

export default EnvironmentsSection;
