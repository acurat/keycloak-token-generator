import React, {FormEvent} from 'react';
import {Loader} from 'semantic-ui-react';
import FormCheckboxGroup from "../components/FormCheckboxGroup";

interface Props {
    environment: string | number | undefined;
    scopes: Array<string>;
    setScopes: (value: string[]) => void;
}

interface State {
    scopes: { [index: string]: string[] };
}

class ScopesSection extends React.Component<Props, State> {
    state: State = {
        scopes: {},
    };

    async componentDidMount() {
        const scopesInStore = await sessionStorage.getItem('scopes');
        let scopes;
        if (scopesInStore) {
            scopes = JSON.parse(scopesInStore);
        } else {
            scopes = await fetch('/ui/scopes').then((response) => response.json());
            sessionStorage.setItem('scopes', JSON.stringify(scopes));
        }
        this.setState({scopes});
    }

    onChange = (e: FormEvent<HTMLInputElement>,
                value: string | number | undefined,
                checked: boolean | undefined) => {

        const {scopes, setScopes} = this.props;
        setScopes(checked ? [...scopes, String(value)] :
            [...scopes.filter(s => s !== value)]);
    };

    render() {
        const {environment, scopes} = this.props;
        let scopesForEnv: string[] = [];
        if (environment) {
            scopesForEnv = this.state.scopes[environment];
        }

        return scopesForEnv ? (
            <FormCheckboxGroup
                label="Scopes"
                values={scopes}
                setValue={this.onChange}
                data={scopesForEnv}
            />
        ) : (
            <Loader active inline size="mini">
                Select scopes for token
            </Loader>
        );
    }
}

export default ScopesSection;
