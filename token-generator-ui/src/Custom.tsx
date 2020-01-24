import React, {useEffect, useState} from 'react';
import {Button, Dimmer, Grid, Label, Loader, Segment} from "semantic-ui-react";
import DisplayTokens from "./tokenize/DisplayTokens";
import {Keys, Tokens} from "./utils/types";
import GenerateCustomTokens from "./tokenize/GenerateCustomTokens";
import ModalComponent from "./components/ModalComponent";
import TokenSegment from "./components/TokenSegment";


interface Props {
}

const Custom: React.FC<Props> = () => {
    const [tokens, setTokens] = useState<Tokens | undefined>(undefined);
    const [loading, setLoading] = useState<boolean>(false);
    const [open, setOpen] = useState<boolean>(false);
    const [keys, setKeys] = useState<Keys>({});
    const [type, setType] = useState<string>("");

    useEffect(() => {
        const fetchKeys = async () => {
            const keysInStore = await sessionStorage.getItem('keys');
            let keys;
            if (keysInStore) {
                keys = JSON.parse(keysInStore);
            } else {
                keys = await fetch('/jwt/keys').then((response) => response.json());
                sessionStorage.setItem('keys', JSON.stringify(keys));
            }
            setKeys(keys);
        };
        fetchKeys().then();
    }, []);

    const show = (type: string) => () => {
        setType(type);
        setOpen(true);
    };

    const close = () => setOpen(false);

    return (
        <Segment className="Site-content">
            {loading && (
                <Dimmer active inverted>
                    <Loader size="small">Loading</Loader>
                </Dimmer>
            )}
            <Segment>
                <ModalComponent open={open} onClose={close}>
                    <TokenSegment content={keys[type]} isJSON={type === 'jwk'}
                                  fullScreen={true}/>
                </ModalComponent>
                <Label as='a' ribbon>
                    RSA256 Public Key
                </Label>
                <Button onClick={show('jwk')}>
                    <Button.Content>JWK format</Button.Content>
                </Button>
                <Button onClick={show('pem')}>
                    <Button.Content>PEM encoded format</Button.Content>
                </Button>
            </Segment>
            <Grid fluid="true" columns={2} divided>
                <Grid.Column width={5}>
                    <GenerateCustomTokens setTokens={setTokens} setLoading={setLoading}/>
                </Grid.Column>
                <Grid.Column verticalAlign="middle" width={11}>
                    <DisplayTokens tokens={tokens}/>
                </Grid.Column>
            </Grid>
        </Segment>)
};

export default Custom;