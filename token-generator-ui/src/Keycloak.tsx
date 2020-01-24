import React, {useState} from 'react';
import {Dimmer, Grid, Loader, Segment} from "semantic-ui-react";
import GenerateKeycloakTokens from "./tokenize/GenerateKeycloakTokens";
import DisplayTokens from "./tokenize/DisplayTokens";
import {Tokens} from "./utils/types";

interface Props {
}

const Keycloak: React.FC<Props> = () => {
    const [tokens, setTokens] = useState<Tokens | undefined>(undefined);
    const [loading, setLoading] = useState<boolean>(false);

    return (
        <Segment className="Site-content">
            {loading && (
                <Dimmer active inverted>
                    <Loader size="small">Loading</Loader>
                </Dimmer>
            )}
            <Grid fluid="true" columns={2} divided>
                <Grid.Column width={5}>
                    <GenerateKeycloakTokens setTokens={setTokens} setLoading={setLoading}/>
                </Grid.Column>
                <Grid.Column verticalAlign="middle" width={11}>
                    <DisplayTokens tokens={tokens}/>
                </Grid.Column>
            </Grid>

        </Segment>)
};

export default Keycloak;